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

import com.liferay.portal.kernel.search.filter.GeoDistanceRangeFilter;
import com.liferay.portal.kernel.search.geolocation.GeoDistance;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.search.elasticsearch.filter.GeoDistanceRangeFilterTranslator;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, service = GeoDistanceRangeFilterTranslator.class)
public class GeoDistanceRangeFilterTranslatorImpl
	implements GeoDistanceRangeFilterTranslator {

	@Override
	public QueryBuilder translate(
		GeoDistanceRangeFilter geoDistanceRangeFilter) {

		GeoLocationPoint pinGeoLocationPoint =
			geoDistanceRangeFilter.getPinGeoLocationPoint();

		//GeoDistanceRangeQueryBuilder has been removed
		//https://www.elastic.co/guide/en/elasticsearch/reference/current/
		//query-dsl-geo-distance-range-query.html
		GeoDistanceQueryBuilder geoDistanceQueryBuilder =
			new GeoDistanceQueryBuilder(geoDistanceRangeFilter.getField());

		GeoPoint geoPoint = new GeoPoint(
			pinGeoLocationPoint.getLatitude(),
			pinGeoLocationPoint.getLongitude());

		geoDistanceQueryBuilder.point(geoPoint);

		GeoDistance geoDistance =
			geoDistanceRangeFilter.getUpperBoundGeoDistance();

		//no lower bound any more. ranges can be acquired through pagination or
		//aggregation instead
		//https://www.elastic.co/guide/en/elasticsearch/reference/current/
		//search-aggregations-bucket-geodistance-aggregation.html

		geoDistanceQueryBuilder.distance(
			String.valueOf(geoDistance.getDistance()),
			DistanceUnit.fromString(
				String.valueOf(geoDistance.getDistanceUnit())));

		return geoDistanceQueryBuilder;
	}

}