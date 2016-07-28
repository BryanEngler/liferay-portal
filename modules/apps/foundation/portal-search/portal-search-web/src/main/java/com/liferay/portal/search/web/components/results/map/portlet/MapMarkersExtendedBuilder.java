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

package com.liferay.portal.search.web.components.results.map.portlet;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.security.permission.ResourceActions;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author André de Oliveira
 */
public class MapMarkersExtendedBuilder {

	public MapMarkersExtendedBuilder(
		Locale locale, ResourceActions resourceActions) {

		_locale = locale;
		_resourceActions = resourceActions;
	}

	public String buildMapMarkersJSON(List<Document> documents) {
		JSONArray locations = JSONFactoryUtil.createJSONArray();

		documents.stream().flatMap(this::getMapMarkers).forEach(locations::put);

		return locations.toString();
	}

	protected String getFormattedDateString(String createDateString) {
		SimpleDateFormat simpleDateFormatInput = new SimpleDateFormat(
			"yyyyMMddHHmmss");
		SimpleDateFormat simpleDateFormatOutput = new SimpleDateFormat(
			"MMM dd yyyy, h:mm a");

		try {
			Date formattedDate = simpleDateFormatInput.parse(createDateString);

			return simpleDateFormatOutput.format(formattedDate);
		}
		catch (Exception e) {
			return "(creation date unavailable)";
		}
	}

	protected JSONObject getMapMarker(
		GeoLocationPoint geoLocationPoint, String assetTypeName,
		String formattedDate, String summary, String title, String userName) {

		JSONObject jObj = JSONFactoryUtil.createJSONObject();

		jObj.put("assetTypeName", assetTypeName);
		jObj.put("date", formattedDate);
		jObj.put("lat", geoLocationPoint.getLatitude());
		jObj.put("lng", geoLocationPoint.getLongitude());
		jObj.put("summary", summary);
		jObj.put("title", title);
		jObj.put("userName", userName);

		return jObj;
	}

	protected Stream<JSONObject> getMapMarkers(Document document) {
		String className = document.get(Field.ENTRY_CLASS_NAME);
		String title = document.get(Field.TITLE);
		String summary = document.get(Field.CONTENT);
		String userName = document.get(Field.USER_NAME);
		String createDateString = document.get(Field.CREATE_DATE);

		String assetTypeName = _resourceActions.getModelResource(
			_locale, className);
		String formattedDateString = getFormattedDateString(createDateString);

		Stream<Field> fields = document.getFields().values().stream();

		Stream<GeoLocationPoint> geoLocationPoints = fields.map(
			Field::getGeoLocationPoint).filter(Objects::nonNull);

		return geoLocationPoints.map(
			geoLocationPoint -> getMapMarker(
				geoLocationPoint, assetTypeName, formattedDateString, summary,
				title, userName));
	}

	private final Locale _locale;
	private final ResourceActions _resourceActions;

}