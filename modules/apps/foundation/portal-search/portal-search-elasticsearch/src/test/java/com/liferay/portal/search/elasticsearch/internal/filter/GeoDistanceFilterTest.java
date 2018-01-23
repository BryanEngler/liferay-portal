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

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.GeoDistanceFilter;
import com.liferay.portal.kernel.search.geolocation.GeoDistance;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.search.elasticsearch.internal.ElasticsearchIndexingFixture;
import com.liferay.portal.search.elasticsearch.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch.internal.connection.LiferayIndexCreator;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Bryan Engler
 */
public class GeoDistanceFilterTest extends BaseIndexingTestCase {

	@Test
	public void testGeoDistanceFilter() throws Exception {
		addDocument(
			DocumentCreationHelpers.singleGeoLocation(
				FIELD, 33.9987, -117.8129));

		addDocument(
			DocumentCreationHelpers.singleGeoLocation(
				FIELD, 34.0003, -117.8127));

		assertGeoDistanceSearch(250.0, 1);
	}

	protected void assertGeoDistanceSearch(Double distance, int expected)
		throws Exception {

		GeoDistanceFilter geoDistanceFilter = new GeoDistanceFilter(
			FIELD, EPICENTER, new GeoDistance(distance));

		SearchContext searchContext = createSearchContext();

		Query query = getDefaultQuery();

		BooleanFilter geoDistanceBooleanFilter = new BooleanFilter();

		geoDistanceBooleanFilter.add(
			geoDistanceFilter, BooleanClauseOccur.MUST);

		query.setPreBooleanFilter(geoDistanceBooleanFilter);

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					Hits hits = search(searchContext, query);

					Assert.assertEquals(
						hits.toString(), expected, hits.getLength());

					return null;
				}

			});
	}

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		ElasticsearchFixture elasticsearchFixture = new ElasticsearchFixture(
			GeoDistanceFilterTest.class.getSimpleName());

		return new ElasticsearchIndexingFixture(
			elasticsearchFixture, BaseIndexingTestCase.COMPANY_ID,
			new LiferayIndexCreator(elasticsearchFixture));
	}

	protected static final GeoLocationPoint EPICENTER = new GeoLocationPoint(
		33.9977, -117.8145);

	protected static final String FIELD = Field.GEO_LOCATION + "_geolocation";

}