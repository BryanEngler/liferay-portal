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

package com.liferay.users.admin.indexer.test;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.search.test.util.HitsAssert;

import java.util.Locale;
import java.util.Objects;

/**
 * @author Luan Maoski
 */
public class UserIndexerFixture {

	public UserIndexerFixture(Indexer<User> indexer) {
		_indexer = indexer;
	}

	public SearchContext getSearchContext(String keywords, Locale locale)
		throws Exception {

		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(TestPropsValues.getCompanyId());
		searchContext.setKeywords(keywords);
		searchContext.setLocale(Objects.requireNonNull(locale));

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setSelectedFieldNames(StringPool.STAR);

		return searchContext;
	}

	public Document searchOnlyOne(String keywords) throws Exception {
		return searchOnlyOne(keywords, Locale.US);
	}

	public Document searchOnlyOne(String keywords, Locale locale)
		throws Exception {

		Hits search = search(getSearchContext(keywords, locale));

		return HitsAssert.assertOnlyOne(search);
	}

	protected long getUserId() throws Exception {
		return TestPropsValues.getUserId();
	}

	protected void reindex(long companyId) throws Exception {
		String[] ids = {String.valueOf(companyId)};

		_indexer.reindex(ids);
	}

	protected Hits search(SearchContext searchContext) throws Exception {
		return _indexer.search(searchContext);
	}

	private final Indexer<User> _indexer;

}