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

package com.liferay.portal.search.elasticsearch.internal.facet;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.search.facet.util.RangeParserUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch.facet.FacetProcessor;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.search.aggregations.bucket.range.geodistance.GeoDistanceBuilder;

import org.osgi.service.component.annotations.Component;

/**
 * @author Raymond Augé
 */
@Component(
	immediate = true,
	property = {
		"class.name=com.liferay.portal.search.facet.internal.geolocation.GeoDistanceFacet"
	},
	service = FacetProcessor.class
)
public class GeoDistanceFacetProcessor
	implements FacetProcessor<SearchRequestBuilder> {

	@Override
	public void processFacet(
		SearchRequestBuilder searchRequestBuilder, Facet facet) {

		FacetConfiguration facetConfiguration = facet.getFacetConfiguration();

		JSONObject jsonObject = facetConfiguration.getData();

		InternalGeoDistanceBuilder internalGeoDistanceBuilder =
			new InternalGeoDistanceBuilder(facetConfiguration.getFieldName());

		internalGeoDistanceBuilder.field(getGeoLocationFieldName(jsonObject));
		internalGeoDistanceBuilder.point(getCenterPoint(jsonObject));
		internalGeoDistanceBuilder.unit(DistanceUnit.METERS);

		addConfigurationRanges(facetConfiguration, internalGeoDistanceBuilder);

		if (internalGeoDistanceBuilder.hasRanges()) {
			searchRequestBuilder.addAggregation(internalGeoDistanceBuilder);
		}
	}

	protected void addConfigurationRanges(
		FacetConfiguration facetConfiguration,
		InternalGeoDistanceBuilder geoDistanceBuilder) {

		JSONObject jsonObject = facetConfiguration.getData();

		JSONArray jsonArray = jsonObject.getJSONArray("ranges");

		if (jsonArray == null) {
			return;
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject rangeJSONObject = jsonArray.getJSONObject(i);

			String rangeString = rangeJSONObject.getString("range");

			String[] range = RangeParserUtil.parserRange(rangeString);

			geoDistanceBuilder.addRange(
				rangeString, Double.valueOf(range[0]),
				Double.valueOf(range[1]));
		}
	}

	protected GeoPoint getCenterPoint(JSONObject jsonObject) {
		String centerPoint = jsonObject.getString("GEODISTANCE-CENTER-POINT");

		if (Validator.isNull(centerPoint)) {
			throw new IllegalArgumentException("Center Point is required");
		}

		return new GeoPoint(centerPoint);
	}

	protected String getGeoLocationFieldName(JSONObject jsonObject) {
		String geoLocationFieldName = jsonObject.getString(
			"GEOLOCATION-FIELD-NAME");

		if (Validator.isNull(geoLocationFieldName)) {
			throw new IllegalArgumentException(
				"Geolocation Field Name is required");
		}

		return geoLocationFieldName;
	}

	protected static class InternalGeoDistanceBuilder
		extends GeoDistanceBuilder {

		public InternalGeoDistanceBuilder(String name) {
			super(name);
		}

		@Override
		public InternalGeoDistanceBuilder addRange(
			String key, double from, double to) {

			super.addRange(key, from, to);

			_hasRanges = true;

			return this;
		}

		public boolean hasRanges() {
			return _hasRanges;
		}

		private boolean _hasRanges;

	}

}