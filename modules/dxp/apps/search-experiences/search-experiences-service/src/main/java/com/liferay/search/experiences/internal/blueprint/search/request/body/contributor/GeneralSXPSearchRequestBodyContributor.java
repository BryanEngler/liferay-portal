/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.blueprint.search.request.body.contributor;

import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.TimeZoneUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.asset.ModelIdentifier;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.internal.blueprint.parameter.SXPParameterData;
import com.liferay.search.experiences.rest.dto.v1_0.Configuration;
import com.liferay.search.experiences.rest.dto.v1_0.GeneralConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author André de Oliveira
 */
public class GeneralSXPSearchRequestBodyContributor
	implements SXPSearchRequestBodyContributor {

	@Override
	public void contribute(
		Configuration configuration, SearchRequestBuilder searchRequestBuilder,
		SXPParameterData sxpParameterData) {

		GeneralConfiguration generalConfiguration =
			configuration.getGeneralConfiguration();

		if (generalConfiguration == null) {
			return;
		}

		if (ArrayUtil.isNotEmpty(
				generalConfiguration.getClauseContributorsExcludes())) {

			searchRequestBuilder.withSearchContext(
				searchContext -> searchContext.setAttribute(
					"search.full.query.clause.contributors.excludes",
					StringUtil.merge(
						generalConfiguration.getClauseContributorsExcludes())));
		}

		if (ArrayUtil.isNotEmpty(
				generalConfiguration.getClauseContributorsIncludes())) {

			searchRequestBuilder.withSearchContext(
				searchContext -> searchContext.setAttribute(
					"search.full.query.clause.contributors.includes",
					StringUtil.merge(
						generalConfiguration.getClauseContributorsIncludes())));
		}

		if (generalConfiguration.getEmptySearchEnabled() != null) {
			searchRequestBuilder.emptySearchEnabled(
				generalConfiguration.getEmptySearchEnabled());
		}

		if (generalConfiguration.getExplain() != null) {
			searchRequestBuilder.explain(generalConfiguration.getExplain());
		}

		if (generalConfiguration.getIncludeResponseString() != null) {
			searchRequestBuilder.includeResponseString(
				generalConfiguration.getIncludeResponseString());
		}

		if (!Validator.isBlank(generalConfiguration.getQueryString())) {
			searchRequestBuilder.queryString(
				generalConfiguration.getQueryString());
		}

		if (ArrayUtil.isNotEmpty(
				generalConfiguration.getSearchableAssetTypes())) {

			String[] searchableAssetTypes =
				generalConfiguration.getSearchableAssetTypes();

			Set<String> classNamesSet = new HashSet<>();

			for (String searchableAssetType : searchableAssetTypes) {
				ModelIdentifier modelIdentifier = _getModelIdentifier(
					searchableAssetType);

				classNamesSet.add(modelIdentifier.getClassName());
			}

			String[] classNames = classNamesSet.toArray(new String[0]);

			searchRequestBuilder.entryClassNames(classNames);
			searchRequestBuilder.modelIndexerClassNames(classNames);

			if (FeatureFlagManagerUtil.isEnabled("LPS-129412")) {
				HashMap<String, List<ModelIdentifier>> modelIdentifiersMap =
					new HashMap<>();

				for (String searchableAssetType : searchableAssetTypes) {
					ModelIdentifier modelIdentifier = _getModelIdentifier(
						searchableAssetType);

					if (!modelIdentifier.hasERCInfo()) {
						continue;
					}

					String className = modelIdentifier.getClassName();

					List<ModelIdentifier> modelIdentifiers;

					if (modelIdentifiersMap.containsKey(className)) {
						modelIdentifiers = modelIdentifiersMap.get(className);

						modelIdentifiers.add(modelIdentifier);
					}
					else {
						modelIdentifiers = new ArrayList<>();

						modelIdentifiers.add(modelIdentifier);
					}

					modelIdentifiersMap.put(className, modelIdentifiers);
				}

				searchRequestBuilder.withSearchContext(
					searchContext -> searchContext.setAttribute(
						"modelIdentifiersMap", modelIdentifiersMap));
			}
		}

		if (!Validator.isBlank(generalConfiguration.getLanguageId())) {
			searchRequestBuilder.locale(
				LocaleUtil.fromLanguageId(
					generalConfiguration.getLanguageId()));
		}

		if (!Validator.isBlank(generalConfiguration.getTimeZoneId())) {
			searchRequestBuilder.withSearchContext(
				searchContext -> searchContext.setTimeZone(
					TimeZoneUtil.getTimeZone(
						generalConfiguration.getTimeZoneId())));
		}
	}

	private ModelIdentifier _getModelIdentifier(String searchableAssetType) {
		String[] modelIdentifierParts = StringUtil.split(
			searchableAssetType, "&&");

		ModelIdentifier modelIdentifier = new ModelIdentifier() {
						@Override
						public String getClassName() {
							return null;
						}

						@Override
						public String getEntityERC() {
							return null;
						}

						@Override
						public String getGroupERC() {
							return null;
						}

						@Override
						public boolean hasERCInfo() {
							return false;
						}

						@Override
						public void setClassName(String className) {
						}

						@Override
						public void setEntityERC(String entityERC) {
						}

						@Override
						public void setGroupERC(String groupERC) {
						}
		};//replace with builder

		modelIdentifier.setClassName(modelIdentifierParts[0]); //possible out of bounds?

		if (modelIdentifierParts.length <= 1) {
			return modelIdentifier;
		}

		modelIdentifier.setEntityERC(modelIdentifierParts[2]);
		modelIdentifier.setGroupERC(modelIdentifierParts[1]);

		return modelIdentifier;
	}

	@Override
	public String getName() {
		return "generalConfiguration";
	}

}