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

package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.search.web.internal.result.display.context.SearchResultSummaryDisplayContext;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

/**
 * @author André de Oliveira
 */
public class SearchResultsMapGeolocationUtil {

	public static void addLocation(
		HttpServletRequest request, Document document,
		SearchResultSummaryDisplayContext summary) {

		Optional<GeoLocationPoint> geoLocationPointOptional =
			findGeoLocationPoint(document);

		geoLocationPointOptional.ifPresent(
			geoLocationPoint -> {
				JSONArray locations = getLocations(request);

				locations.put(buildLocationJSON(summary, geoLocationPoint));

				setLocations(request, locations);
			});
	}

	public static String getLocationsAsJSON(HttpServletRequest request) {
		JSONArray locations = getLocations(request);

		return locations.toString();
	}

	protected static JSONObject buildLocationJSON(
		SearchResultSummaryDisplayContext summary,
		GeoLocationPoint geoLocation) {

		double lat = geoLocation.getLatitude();
		double lng = geoLocation.getLongitude();

		JSONObject jObj = JSONFactoryUtil.createJSONObject();

		jObj.put("lat", lat);
		jObj.put("lng", lng);

		jObj.put("summary", summary.getContent());
		jObj.put("title", summary.getHighlightedTitle());

		return jObj;
	}

	protected static Optional<GeoLocationPoint> findGeoLocationPoint(
		Document document) {

		Stream<Field> fields = document.getFields().values().stream();

		Stream<GeoLocationPoint> geoLocationPoints = fields.map(
			Field::getGeoLocationPoint).filter(Objects::nonNull);

		return geoLocationPoints.findFirst();
	}

	protected static JSONArray getLocations(HttpServletRequest request) {
		JSONArray locations = (JSONArray)request.getAttribute(
			"search.jsp-geolocation");

		if (locations != null) {
			return locations;
		}

		return JSONFactoryUtil.createJSONArray();
	}

	protected static void setLocations(
		HttpServletRequest request, JSONArray locations) {

		request.setAttribute("search.jsp-geolocation", locations);
	}

}