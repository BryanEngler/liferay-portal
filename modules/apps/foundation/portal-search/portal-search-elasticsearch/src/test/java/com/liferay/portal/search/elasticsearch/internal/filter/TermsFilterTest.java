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

package com.liferay.portal.search.elasticsearch.internal.filter;

import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.filter.TermsFilter;
import com.liferay.portal.search.elasticsearch.internal.ElasticsearchIndexingFixture;
import com.liferay.portal.search.elasticsearch.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch.internal.facet.SimpleFacetTest;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * @author André de Oliveira
 */
public class TermsFilterTest extends BaseIndexingTestCase {

	@Test
	public void test() throws Exception {
		addDocument("One");
		addDocument("Two");
		addDocument("Three");

		TermsFilter termsFilter = new TermsFilter(_FIELD_NAME);

		termsFilter.addValues("Two", "Three");

		assertSearch(termsFilter, Arrays.asList("Two", "Three"));
	}

	protected void addDocument(String value) throws Exception {
		addDocument(DocumentCreationHelpers.singleText(_FIELD_NAME, value));
	}

	protected void assertSearch(Filter filter, List<String> expectedValues)
		throws Exception {

		SearchContext searchContext = createSearchContext();

		Query query = getDefaultQuery();

		BooleanFilter booleanFilter = new BooleanFilter();

		booleanFilter.add(filter);
		query.setPreBooleanFilter(booleanFilter);

		IdempotentRetryAssert.retryAssert(
			5, TimeUnit.SECONDS,
			() -> {
				Hits hits = search(searchContext, query);

				DocumentsAssert.assertValues(
					(String)searchContext.getAttribute("queryString"),
					hits.getDocs(), _FIELD_NAME, expectedValues);

				return null;
			});
	}

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		return new ElasticsearchIndexingFixture(
			new ElasticsearchFixture(SimpleFacetTest.class.getSimpleName()),
			BaseIndexingTestCase.COMPANY_ID);
	}

	private static final String _FIELD_NAME = "FIELD";

}