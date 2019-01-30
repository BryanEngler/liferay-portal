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

package com.liferay.portal.search.test.util.stats;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Stats;
import com.liferay.portal.search.stats.StatsResults;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Miguel Angelo Caldas Gallindo
 */
public abstract class BaseStatisticsTestCase extends BaseIndexingTestCase {

	@Test
	public void testGetStats() throws Exception {
		addDocuments(31);

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					assertStats();

					return null;
				}

			});
	}

	@Test
	public void testStatsAfterSearch() throws Exception {
		doTestPresentAfter(IndexingTestHelper::search, true);
	}

	@Test
	public void testStatsAfterSearchCount() throws Exception {
		doTestPresentAfter(IndexingTestHelper::searchCount, false);
	}

	protected static String toString(StatsResults statsResults) {
		StringBundler sb = new StringBundler(21);

		sb.append("{cardinality=");
		sb.append(statsResults.getCardinality());
		sb.append(", count=");
		sb.append(statsResults.getCount());
		sb.append(", field=");
		sb.append(statsResults.getField());
		sb.append(", max=");
		sb.append(statsResults.getMax());
		sb.append(", mean=");
		sb.append(statsResults.getMean());
		sb.append(", min=");
		sb.append(statsResults.getMin());
		sb.append(", missing=");
		sb.append(statsResults.getMissing());
		sb.append(", standardDeviation=");
		sb.append(statsResults.getStandardDeviation());
		sb.append(", sum=");
		sb.append(statsResults.getSum());
		sb.append(", sumOfSquares=");
		sb.append(statsResults.getSumOfSquares());
		sb.append("}");

		return sb.toString();
	}

	protected void addDocuments(int count) throws Exception {
		final String field = STAT_FIELD;

		for (int i = 1; i <= count; i++) {
			addDocument(DocumentCreationHelpers.singleNumberSortable(field, i));
		}
	}

	protected void assertStats() throws Exception {
		String field = STAT_SORTABLE_FIELD;

		SearchContext searchContext = createSearchContext();

		Stats stats = new Stats();

		stats.setCount(true);
		stats.setField(field);
		stats.setMax(true);
		stats.setMean(true);
		stats.setMin(true);
		stats.setSum(true);
		stats.setSumOfSquares(true);

		searchContext.addStats(stats);

		HashMap<String, Boolean> statsCardinalityMap = new HashMap<>();

		statsCardinalityMap.put(field, true);

		searchContext.setAttribute("statsCardinalityMap", statsCardinalityMap);

		Hits hits = search(searchContext);

		Map<String, com.liferay.portal.kernel.search.StatsResults>
			legacyStatsResultsMap = hits.getStatsResults();

		Assert.assertNotNull(legacyStatsResultsMap);

		Map<String, StatsResults> statsResultsMap =
			(Map<String, StatsResults>)searchContext.getAttribute(
				"stats.results.map");

		Assert.assertNotNull(statsResultsMap);

		StatsResults statsResults = statsResultsMap.get(field);

		Assert.assertNotNull(statsResults);

		StatsResults expectedStatsResults = new StatsResults(field);

		expectedStatsResults.setCardinality(31);
		expectedStatsResults.setCount(31);
		expectedStatsResults.setMax(31);
		expectedStatsResults.setMean(16);
		expectedStatsResults.setMin(1);
		expectedStatsResults.setSum(496);
		expectedStatsResults.setSumOfSquares(10416);

		Assert.assertEquals(
			toString(expectedStatsResults), toString(statsResults));
	}

	protected void assertStatsResultsMap(
		Map<String, StatsResults> statsResultsMap, boolean present) {

		if (present) {
			Assert.assertNotNull(statsResultsMap);

			StatsResults statsResults = statsResultsMap.get(
				STATS_RESULTS_FIELD);

			Assert.assertEquals(1, statsResults.getCardinality());
		}
		else {
			Assert.assertNull(statsResultsMap);
		}
	}

	protected void doTestPresentAfter(
		Consumer<IndexingTestHelper> consumer, boolean present) {

		addDocument(
			document -> {
			});

		com.liferay.portal.search.stats.Stats stats =
			new com.liferay.portal.search.stats.Stats();

		stats.setField(STATS_RESULTS_FIELD);
		stats.setCardinality(true);

		Map<String, com.liferay.portal.search.stats.Stats> statsMap =
			new HashMap<>();

		statsMap.put(stats.getField(), stats);

		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.defineRequest(
					searchRequestBuilder -> {
						searchRequestBuilder.statsMap(
							statsMap
						);
					});

				consumer.accept(indexingTestHelper);

				indexingTestHelper.verifyContext(
					searchContext -> {
						Map<String, StatsResults> statsResultsMap =
							(Map<String, StatsResults>)
								searchContext.getAttribute("stats.results.map");

						assertStatsResultsMap(statsResultsMap, present);
					});

				indexingTestHelper.verifyResponse(
					searchResponse -> {
						Map<String, StatsResults> statsResultsMap =
							searchResponse.getStatsResultsMap();

						assertStatsResultsMap(statsResultsMap, present);
					});
			});
	}

	protected static final String STAT_FIELD = Field.PRIORITY;

	protected static final String STAT_SORTABLE_FIELD =
		STAT_FIELD + "_Number_sortable";

	protected static final String STATS_RESULTS_FIELD = Field.ENTRY_CLASS_NAME;

}