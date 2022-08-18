/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.internal.upgrade.v1_4_0;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.rest.dto.v1_0.util.ElementDefinitionUtil;
import com.liferay.search.experiences.rest.dto.v1_0.util.ElementInstanceUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Bryan Engler
 */
public class SXPUpgradeProcess extends UpgradeProcess {

	public SXPUpgradeProcess(
		AssetCategoryLocalService assetCategoryLocalService) {

		_assetCategoryLocalService = assetCategoryLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_upgradeSXPElement();

		_upgradeSXPBlueprint();
	}

	private void _putNameAndId(JSONArray jsonArray, String id)
		throws Exception {

		AssetCategory assetCategory =
			_assetCategoryLocalService.fetchAssetCategory(Long.valueOf(id));

		String name = id;

		if ((assetCategory != null) &&
			!Validator.isBlank(assetCategory.getName())) {

			name = assetCategory.getName();
		}

		jsonArray.put(
			JSONFactoryUtil.createJSONObject(
				StringBundler.concat(
					"{\"name\":\"", name, "\",\"id\":\"", id, "\"}")));
	}

	private String _updateElementDefinitionJSON(String elementDefinitionJSON)
		throws Exception {

		JSONObject elementDefinitionJSONObject =
			JSONFactoryUtil.createJSONObject(elementDefinitionJSON);

		JSONObject uiConfigurationJSONObject =
			elementDefinitionJSONObject.getJSONObject("uiConfiguration");

		JSONArray fieldSetsJSONArray = uiConfigurationJSONObject.getJSONArray(
			"fieldSets");

		for (int i = 0; i < fieldSetsJSONArray.length(); i++) {
			Object fieldSet = fieldSetsJSONArray.get(i);

			if (fieldSet instanceof JSONObject) {
				JSONObject fieldSetJSONObject = (JSONObject)fieldSet;

				JSONArray fieldsJSONArray = fieldSetJSONObject.getJSONArray(
					"fields");

				for (int j = 0; j < fieldsJSONArray.length(); j++) {
					Object field = fieldsJSONArray.get(j);

					if (field instanceof JSONObject) {
						JSONObject fieldJSONObject = (JSONObject)field;

						String name = fieldJSONObject.getString("name");

						if (name.equals("asset_category_id") ||
							name.equals("asset_category_ids")) {

							String type = fieldJSONObject.getString("type");

							if (type.equals("multiselect")) {
								fieldJSONObject.put(
									"typeOptions",
									JSONFactoryUtil.createJSONObject(
										"{\"format\":\"array\"}"));
							}

							fieldJSONObject.put("type", "categorySelector");

							break;
						}
					}
				}
			}
		}

		return elementDefinitionJSONObject.toString();
	}

	private String _updateElementInstancesJSON(String elementInstancesJSON)
		throws Exception {

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray(
			elementInstancesJSON);

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			JSONObject sxpElementJSONObject = jsonObject.getJSONObject(
				"sxpElement");

			String externalReferenceCode = sxpElementJSONObject.getString(
				"externalReferenceCode");

			if (!ArrayUtil.contains(
					_EXTERNAL_REFERENCE_CODES, externalReferenceCode)) {

				continue;
			}

			sxpElementJSONObject.put(
				"elementDefinition",
				JSONFactoryUtil.createJSONObject(
					_updateElementDefinitionJSON(
						sxpElementJSONObject.getString("elementDefinition"))));

			JSONObject uiConfigurationValuesJSONObject =
				jsonObject.getJSONObject("uiConfigurationValues");

			String key = "asset_category_id";

			if (externalReferenceCode.equals("BOOST_CONTENTS_IN_A_CATEGORY")) {
				key = "asset_category_ids";
			}

			Object valueObject = uiConfigurationValuesJSONObject.get(key);

			JSONArray newAssetCategoryIdsJSONArray =
				JSONFactoryUtil.createJSONArray();

			if (valueObject instanceof JSONArray) {
				JSONArray assetCategoryIdsJSONArray = (JSONArray)valueObject;

				for (int j = 0; j < assetCategoryIdsJSONArray.length(); j++) {
					Object assetCategoryIdObject =
						assetCategoryIdsJSONArray.get(j);

					if (assetCategoryIdObject instanceof JSONObject) {
						JSONObject assetCategoryIdJSONObject =
							(JSONObject)assetCategoryIdObject;

						_putNameAndId(
							newAssetCategoryIdsJSONArray,
							assetCategoryIdJSONObject.getString("value"));
					}
				}
			}
			else {
				_putNameAndId(
					newAssetCategoryIdsJSONArray, String.valueOf(valueObject));
			}

			uiConfigurationValuesJSONObject.put(
				key, newAssetCategoryIdsJSONArray);
		}

		return jsonArray.toString();
	}

	private void _upgradeSXPBlueprint() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select sxpBlueprintId, elementInstancesJSON from " +
					"SXPBlueprint");
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update SXPBlueprint set elementInstancesJSON = ? where " +
						"sxpBlueprintId = ?")) {

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					String elementInstancesJSON = resultSet.getString(
						"elementInstancesJSON");

					try {
						ElementInstanceUtil.toElementInstances(
							elementInstancesJSON);
					}
					catch (RuntimeException runtimeException) {
						_log.error(
							StringBundler.concat(
								"Search experiences blueprint with ID ",
								String.valueOf(
									resultSet.getLong("sxpBlueprintId")),
								" contains corrupted element instances JSON"));

						continue;
					}

					preparedStatement2.setString(
						1, _updateElementInstancesJSON(elementInstancesJSON));

					preparedStatement2.setLong(
						2, resultSet.getLong("sxpBlueprintId"));

					preparedStatement2.addBatch();
				}

				preparedStatement2.executeBatch();
			}
		}
	}

	private void _upgradeSXPElement() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select sxpElementId, elementDefinitionJSON from ",
					"SXPElement where externalReferenceCode in ('",
					StringUtil.merge(_EXTERNAL_REFERENCE_CODES, "','"), "')"));
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update SXPElement set elementDefinitionJSON = ? where " +
						"sxpElementId = ?")) {

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					String elementDefinitionJSON = resultSet.getString(
						"elementDefinitionJSON");

					try {
						ElementDefinitionUtil.toElementDefinition(
							elementDefinitionJSON);
					}
					catch (RuntimeException runtimeException) {
						_log.error(
							StringBundler.concat(
								"Search experiences element with ID ",
								String.valueOf(
									resultSet.getLong("sxpElementId")),
								" contains corrupted element definition JSON"));

						continue;
					}

					preparedStatement2.setString(
						1, _updateElementDefinitionJSON(elementDefinitionJSON));

					preparedStatement2.setLong(
						2, resultSet.getLong("sxpElementId"));

					preparedStatement2.addBatch();
				}

				preparedStatement2.executeBatch();
			}
		}
	}

	private static final String[] _EXTERNAL_REFERENCE_CODES = {
		"BOOST_CONTENTS_IN_A_CATEGORY",
		"BOOST_CONTENTS_IN_A_CATEGORY_BY_KEYWORD_MATCH",
		"BOOST_CONTENTS_IN_A_CATEGORY_FOR_A_PERIOD_OF_TIME",
		"BOOST_CONTENTS_IN_A_CATEGORY_FOR_USER_SEGMENTS",
		"BOOST_CONTENTS_IN_A_CATEGORY_FOR_GUEST_USERS",
		"BOOST_CONTENTS_IN_A_CATEGORY_FOR_NEW_USER_ACCOUNTS",
		"BOOST_CONTENTS_IN_A_CATEGORY_FOR_THE_TIME_OF_DAY",
		"HIDE_CONTENTS_IN_A_CATEGORY",
		"HIDE_CONTENTS_IN_A_CATEGORY_FOR_GUEST_USERS"
	};

	private static final Log _log = LogFactoryUtil.getLog(
		SXPUpgradeProcess.class);

	private final AssetCategoryLocalService _assetCategoryLocalService;

}