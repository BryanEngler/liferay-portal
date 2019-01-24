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

package com.liferay.portal.search.elasticsearch6.internal.groupby;

import com.liferay.portal.kernel.search.GroupBy;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.search.elasticsearch6.internal.ElasticsearchIndexingFixture;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch6.internal.connection.LiferayIndexCreator;
import com.liferay.portal.search.test.util.groupby.BaseGroupByTestCase;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author André de Oliveira
 * @author Tibor Lipusz
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
						toMap("five", "5|3"), hits, indexingTestHelper));
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
						toMap("five", "5|3"), hits, indexingTestHelper));
			});
	}

	@Test
	public void testGroupByTermsSortsCountAscKeyAsc() throws Exception {
		List<String> orderedResults = new ArrayList<>();

		orderedResults.add("one|2|2");
		orderedResults.add("two|2|2");
		orderedResults.add("three|3|3");

		assertGroupByTermsSortsCountDescKeyDesc(orderedResults, false, false);
	}

	@Test
	public void testGroupByTermsSortsCountAscKeyDesc() throws Exception {
		List<String> orderedResults = new ArrayList<>();

		orderedResults.add("two|2|2");
		orderedResults.add("one|2|2");
		orderedResults.add("three|3|3");

		assertGroupByTermsSortsCountDescKeyDesc(orderedResults, false, true);
	}

	@Test
	public void testGroupByTermsSortsCountDescKeyAsc() throws Exception {
		List<String> orderedResults = new ArrayList<>();

		orderedResults.add("three|3|3");
		orderedResults.add("one|2|2");
		orderedResults.add("two|2|2");

		assertGroupByTermsSortsCountDescKeyDesc(orderedResults, true, false);
	}

	@Test
	public void testGroupByTermsSortsCountDescKeyDesc() throws Exception {
		List<String> orderedResults = new ArrayList<>();

		orderedResults.add("three|3|3");
		orderedResults.add("two|2|2");
		orderedResults.add("one|2|2");

		assertGroupByTermsSortsCountDescKeyDesc(orderedResults, true, true);
	}

	@Test
	public void testGroupByTermsSortsDefault() throws Exception {
		List<String> orderedResults = new ArrayList<>();

		orderedResults.add("three|3|3");
		orderedResults.add("one|2|2");
		orderedResults.add("two|2|2");

		indexTermsSortsDuplicates();

		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.define(
					searchContext -> {
						GroupBy groupBy = new GroupBy(GROUP_FIELD);

						searchContext.setGroupBy(groupBy);
					});

				indexingTestHelper.search();

				indexingTestHelper.verify(
					hits -> assertGroupsOrdered(
						orderedResults, hits, indexingTestHelper));
			});
	}

	protected void assertGroupByTermsSortsCountDescKeyDesc(
			List<String> orderedResults, boolean countDesc, boolean keyDesc)
		throws Exception {

		indexTermsSortsDuplicates();

		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.define(
					searchContext -> {
						Sort[] sorts = new Sort[2];

						sorts[0] = new Sort("_count", countDesc);
						sorts[1] = new Sort("_key", keyDesc);

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
		ElasticsearchFixture elasticsearchFixture1 = new ElasticsearchFixture(
			getClass());

		return new ElasticsearchIndexingFixture() {
			{
				companyId = BaseIndexingTestCase.COMPANY_ID;
				elasticsearchFixture = elasticsearchFixture1;
				indexCreator = new LiferayIndexCreator(elasticsearchFixture1);
			}
		};
	}

	protected void indexTermsSortsDuplicates() {
		indexDuplicates("one", 2);
		indexDuplicates("two", 2);
		indexDuplicates("three", 3);
	}

}