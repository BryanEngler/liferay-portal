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

package com.liferay.portal.search.test.util.filter;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.filter.TermFilter;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * @author André de Oliveira
 * @author Eric Yan
 */
public abstract class BaseTermFilterTestCase extends BaseIndexingTestCase {

	@Test
	public void testKeywordField() throws Exception {
		String fieldName = Field.FOLDER_ID;
		String value = "One";

		addDocument(DocumentCreationHelpers.singleKeyword(fieldName, value));

		TermFilter termFilter = new TermFilter(fieldName, value);

		assertSearch(termFilter, fieldName, Arrays.asList(value));
	}

	@Test
	public void testLuceneSpecialCharacters() throws Exception {
		String fieldName = Field.FOLDER_ID;
		String value = "One\\+-!():^[]\"{}~*?|&/Two";

		addDocument(DocumentCreationHelpers.singleKeyword(fieldName, value));

		TermFilter termFilter = new TermFilter(fieldName, value);

		assertSearch(termFilter, fieldName, Arrays.asList(value));
	}

	@Test
	public void testSolrSpecialCharacters() throws Exception {
		String fieldName = Field.FOLDER_ID;
		String value = "One\\+-!():^[]\"{}~*?|&/; Two";

		addDocument(DocumentCreationHelpers.singleKeyword(fieldName, value));

		TermFilter termFilter = new TermFilter(fieldName, value);

		assertSearch(termFilter, fieldName, Arrays.asList(value));
	}

	@Test
	public void testSpaces() throws Exception {
		String fieldName = Field.FOLDER_ID;
		String value = "One Two";

		addDocument(DocumentCreationHelpers.singleKeyword(fieldName, value));

		TermFilter termFilter = new TermFilter(fieldName, value);

		assertSearch(termFilter, fieldName, Arrays.asList(value));
	}

	protected void assertSearch(
			Filter filter, String fieldName, List<String> expectedValues)
		throws Exception {

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			() -> doAssertSearch(filter, fieldName, expectedValues));
	}

	protected Void doAssertSearch(
			Filter filter, String fieldName, List<String> expectedValues)
		throws Exception {

		SearchContext searchContext = createSearchContext();

		Hits hits = search(
			searchContext,
			booleanQuery -> setPreBooleanFilter(filter, booleanQuery));

		DocumentsAssert.assertValues(
			(String)searchContext.getAttribute("queryString"), hits.getDocs(),
			fieldName, expectedValues);

		return null;
	}

	protected void setPreBooleanFilter(Filter filter, Query query) {
		BooleanFilter booleanFilter = new BooleanFilter();

		booleanFilter.add(filter, BooleanClauseOccur.MUST);

		query.setPreBooleanFilter(booleanFilter);
	}

}