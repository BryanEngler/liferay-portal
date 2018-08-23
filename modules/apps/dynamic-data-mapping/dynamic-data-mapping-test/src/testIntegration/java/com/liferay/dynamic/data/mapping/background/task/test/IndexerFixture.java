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

package com.liferay.dynamic.data.mapping.background.task.test;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.IndexWriterHelperUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleThreadLocal;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lucas Marques de Paula
 */
public class IndexerFixture {

	public IndexerFixture(Class<?> clazz) {
		_indexer = IndexerRegistryUtil.getIndexer(clazz);
	}

	public void deleteDocuments(Document[] documents)
		throws PortalException, SearchException {

		Collection<String> uids = Stream.of(
			documents
		).map(
			document -> document.getUID()
		).collect(
			Collectors.toSet()
		);

		IndexWriterHelperUtil.deleteDocuments(
			_indexer.getSearchEngineId(), _getCompanyId(), uids, true);
	}

	public Hits search(String keywords) throws Exception {
		SearchContext searchContext = new SearchContext();

		searchContext.setKeywords(keywords);
		searchContext.setLike(true);
		searchContext.setCompanyId(_getCompanyId());
		searchContext.setLocale(LocaleThreadLocal.getSiteDefaultLocale());

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setSelectedFieldNames(StringPool.STAR);

		return _indexer.search(searchContext);
	}

	private long _getCompanyId() throws PortalException {
		return TestPropsValues.getCompanyId();
	}

	private final Indexer<?> _indexer;

}