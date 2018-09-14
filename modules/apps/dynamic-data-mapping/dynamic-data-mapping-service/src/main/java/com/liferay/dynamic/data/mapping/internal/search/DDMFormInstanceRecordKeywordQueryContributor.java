/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.internal.search;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.query.QueryHelper;
import com.liferay.portal.search.spi.model.query.contributor.KeywordQueryContributor;
import com.liferay.portal.search.spi.model.query.contributor.helper.KeywordQueryContributorHelper;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Raymond Augé
 * @author Zsigmond Rab
 * @author Hugo Huijser
 * @author Marco Leo
 * @author Luan Maoski
 * @author Lucas Marques
 */
@Component(
	immediate = true,
	property = "indexer.class.name=com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord",
	service = KeywordQueryContributor.class
)
public class DDMFormInstanceRecordKeywordQueryContributor
	implements KeywordQueryContributor {

	@Override
	public void contribute(
		String keywords, BooleanQuery booleanQuery,
		KeywordQueryContributorHelper keywordQueryContributorHelper) {

		SearchContext searchContext =
			keywordQueryContributorHelper.getSearchContext();

		queryHelper.addSearchTerm(
			booleanQuery, searchContext, Field.USER_NAME, false);

		try {
			addContentSearchTerm(booleanQuery, searchContext);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to execute " +
						"DDMFormInstanceRecordKeywordQueryContributor");
			}
		}
	}

	protected void addContentSearchTerm(
			BooleanQuery searchQuery, SearchContext searchContext)
		throws Exception {

		Locale locale = searchContext.getLocale();

		StringBundler sb = new StringBundler(3);

		sb.append("ddmContent");
		sb.append(StringPool.UNDERLINE);
		sb.append(LocaleUtil.toLanguageId(locale));

		queryHelper.addSearchTerm(
			searchQuery, searchContext, sb.toString(), false);
	}

	@Reference
	protected QueryHelper queryHelper;

	private static final Log _log = LogFactoryUtil.getLog(
		DDMFormInstanceRecordKeywordQueryContributor.class);

}