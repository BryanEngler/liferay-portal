/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.search.spi.model.index.contributor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.account.service.AccountEntryOrganizationRelLocalService;
import com.liferay.object.internal.search.spi.model.index.contributor.manager.ObjectEntryModelContributorManager;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryVersion;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.io.Serializable;

import java.util.Collections;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(
	property = "indexer.class.name=com.liferay.object.model.ObjectEntryVersion",
	service = ModelDocumentContributor.class
)
public class ObjectEntryVersionModelDocumentContributor
	implements ModelDocumentContributor<ObjectEntryVersion> {

	@Override
	public void contribute(
		Document document, ObjectEntryVersion objectEntryVersion) {

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			objectEntryVersion.getObjectEntryId());

		if (objectEntry != null) {
			ObjectEntry clonedObjectEntry = (ObjectEntry)objectEntry.clone();

			JSONObject contentJSONObject = _getContentJSONObject(
				objectEntryVersion);

			clonedObjectEntry.setUserId(objectEntryVersion.getUserId());
			clonedObjectEntry.setObjectEntryFolderId(
				contentJSONObject.getLong("objectEntryFolderId"));
			clonedObjectEntry.setVersion(objectEntryVersion.getVersion());
			clonedObjectEntry.setStatus(objectEntryVersion.getStatus());
			clonedObjectEntry.setValues(_getValues(contentJSONObject));

			ObjectEntryModelContributorManager
				objectEntryModelContributorManager =
					new ObjectEntryModelContributorManager(
						_accountEntryOrganizationRelLocalService,
						_objectDefinitionLocalService,
						_objectEntryFolderLocalService,
						_objectFieldLocalService, _objectFolderService);

			objectEntryModelContributorManager.contribute(
				document, clonedObjectEntry);
		}

		document.add(
			_getField(
				Field.VERSION, true,
				String.valueOf(objectEntryVersion.getVersion())));
		document.addKeyword(
			"objectEntryVersionId",
			objectEntryVersion.getObjectEntryVersionId());
	}

	private JSONObject _getContentJSONObject(
		ObjectEntryVersion objectEntryVersion) {

		try {
			return _jsonFactory.createJSONObject(
				objectEntryVersion.getContent());
		}
		catch (JSONException jsonException) {
			if (_log.isDebugEnabled()) {
				_log.debug(jsonException);
			}
		}

		return _jsonFactory.createJSONObject();
	}

	private Field _getField(String name, boolean sortable, String value) {
		Field field = new Field(name, value);

		field.setSortable(sortable);

		return field;
	}

	private Map<String, Serializable> _getValues(JSONObject jsonObject) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			JSONObject propertiesJSONObject = jsonObject.getJSONObject(
				"properties");

			if (JSONUtil.isEmpty(propertiesJSONObject)) {
				return Collections.emptyMap();
			}

			return objectMapper.readValue(
				propertiesJSONObject.toString(),
				new TypeReference<Map<String, Serializable>>() {
				});
		}
		catch (JsonProcessingException jsonProcessingException) {
			if (_log.isDebugEnabled()) {
				_log.debug(jsonProcessingException);
			}
		}

		return Collections.emptyMap();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryVersionModelDocumentContributor.class);

	@Reference
	private AccountEntryOrganizationRelLocalService
		_accountEntryOrganizationRelLocalService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private ObjectFolderLocalService _objectFolderService;

}