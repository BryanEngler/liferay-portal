/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.search.spi.model.query.contributor;

import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectViewLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.localization.SearchLocalizationHelper;
import com.liferay.portal.search.spi.model.query.contributor.KeywordQueryContributor;
import com.liferay.portal.search.spi.model.query.contributor.helper.KeywordQueryContributorHelper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(
	property = "indexer.class.name=com.liferay.object.model.ObjectEntryVersion",
	service = KeywordQueryContributor.class
)
public class ObjectEntryVersionKeywordQueryContributor
	implements KeywordQueryContributor {

	@Override
	public void contribute(
		String keywords, BooleanQuery booleanQuery,
		KeywordQueryContributorHelper keywordQueryContributorHelper) {

		SearchContext searchContext =
			keywordQueryContributorHelper.getSearchContext();

		try {
			ObjectEntryKeywordQueryContributor
				objectEntryKeywordQueryContributor =
					new ObjectEntryKeywordQueryContributor(
						_objectDefinitionLocalService.getObjectDefinition(
							GetterUtil.getLong(
								searchContext.getAttribute(
									"objectDefinitionId"))),
						_objectFieldLocalService, _objectViewLocalService,
						_searchLocalizationHelper);

			objectEntryKeywordQueryContributor.contribute(
				keywords, booleanQuery, keywordQueryContributorHelper);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryVersionKeywordQueryContributor.class.getName());

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private ObjectViewLocalService _objectViewLocalService;

	@Reference
	private SearchLocalizationHelper _searchLocalizationHelper;

}