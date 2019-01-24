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

package com.liferay.portal.search.solr7.internal.groupby;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.GroupBy;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.search.solr7.internal.SolrIndexingFixture;
import com.liferay.portal.search.test.util.groupby.BaseGroupByTestCase;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author Miguel Angelo Caldas Gallindo
 * @author Tibor Lipusz
 * @author André de Oliveira
 */
public class GroupByTest extends BaseGroupByTestCase {

	@Test
	public void testGroupByDocsSizeDefault() throws Exception {
		indexDuplicates("five", 5);

		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.define(
					searchContext -> searchContext.setGroupBy(
						new GroupBy(GROUP_FIELD)));

				indexingTestHelper.search();

				indexingTestHelper.verify(
					hits -> assertGroups(
						toMap("five", "5|1"), hits, indexingTestHelper));
			});
	}

	@Test
	public void testGroupByDocsSizeZero() throws Exception {
		indexDuplicates("five", 5);

		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.define(
					searchContext -> {
						GroupBy groupBy = new GroupBy(GROUP_FIELD);

						groupBy.setSize(0);

						searchContext.setGroupBy(groupBy);
					});

				indexingTestHelper.search();

				indexingTestHelper.verify(
					hits -> assertGroups(
						toMap("five", "5|1"), hits, indexingTestHelper));
			});
	}

	@Test
	public void testGroupByTermsSortsScoreFieldAsc() throws Exception {
		assertGroupByTermsSortsScoreField(false);
	}

	@Test
	public void testGroupByTermsSortsScoreFieldDesc() throws Exception {
		assertGroupByTermsSortsScoreField(true);
	}

	@Test
	public void testGroupByTermsSortsSortFieldAsc() throws Exception {
		List<String> orderedResults = new ArrayList<>();

		orderedResults.add("one|1|1");
		orderedResults.add("two|2|1");
		orderedResults.add("three|3|1");

		assertGroupByTermsSortsSortField(orderedResults, false);
	}

	@Test
	public void testGroupByTermsSortsSortFieldDesc() throws Exception {
		List<String> orderedResults = new ArrayList<>();

		orderedResults.add("three|3|1");
		orderedResults.add("two|2|1");
		orderedResults.add("one|1|1");

		assertGroupByTermsSortsSortField(orderedResults, true);
	}

	protected void assertGroupByTermsSortsScoreField(boolean desc)
		throws Exception {

		indexTermsSortsDuplicates();

		List<String> orderedResults = new ArrayList<>();

		orderedResults.add("three|3|1");
		orderedResults.add("two|2|1");
		orderedResults.add("one|1|1");

		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.define(
					searchContext -> {
						Sort[] sorts = new Sort[1];

						sorts[0] = new Sort(
							"scoreField", Sort.SCORE_TYPE, desc);

						searchContext.setAttribute("groupByTermsSorts", sorts);

						GroupBy groupBy = new GroupBy(GROUP_FIELD);

						searchContext.setGroupBy(groupBy);
					});

				BooleanQueryImpl booleanQuery = new BooleanQueryImpl();

				booleanQuery.addExactTerm(SORT_FIELD, "3");
				booleanQuery.addExactTerm(SORT_FIELD, "2");

				booleanQuery.add(getDefaultQuery(), BooleanClauseOccur.MUST);

				indexingTestHelper.setQuery(booleanQuery);

				indexingTestHelper.search();

				indexingTestHelper.verify(
					hits -> assertGroupsOrdered(
						orderedResults, hits, indexingTestHelper));
			});
	}

	protected void assertGroupByTermsSortsSortField(
			List<String> orderedResults, boolean desc)
		throws Exception {

		indexTermsSortsDuplicates();

		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.define(
					searchContext -> {
						Sort[] sorts = new Sort[1];

						sorts[0] = new Sort(SORT_FIELD, Sort.STRING_TYPE, desc);

						searchContext.setAttribute("groupByTermsSorts", sorts);

						GroupBy groupBy = new GroupBy(GROUP_FIELD);

						searchContext.setGroupBy(groupBy);
					});

				indexingTestHelper.search();

				indexingTestHelper.verify(
					hits -> assertGroupsOrdered(
						orderedResults, hits, indexingTestHelper));
			});
	}

	@Override
	protected IndexingFixture createIndexingFixture() {
		return new SolrIndexingFixture();
	}

	protected void indexTermsSortsDuplicates() {
		indexDuplicates("one", 1);
		indexDuplicates("two", 2);
		indexDuplicates("three", 3);
	}

}