package com.liferay.portal.search.web.internal.search.results.map.portlet;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MapMarkersBuilder {

	public String buildMapMarkersJSON(List<Document> documents) {
		JSONArray locations = JSONFactoryUtil.createJSONArray();

		documents.stream().flatMap(this::getMapMarkers).forEach(locations::put);

		return locations.toString();
	}

	protected JSONObject getMapMarker(
		GeoLocationPoint geoLocationPoint, String title, String summary) {

		JSONObject jObj = JSONFactoryUtil.createJSONObject();

		jObj.put("lat", geoLocationPoint.getLatitude());
		jObj.put("lng", geoLocationPoint.getLongitude());
		jObj.put("title", title);
		jObj.put("summary", summary);

		return jObj;
	}

	protected Stream<JSONObject> getMapMarkers(Document document) {
		String title = document.get(Field.TITLE);
		String summary = document.get(Field.CONTENT);

		return document.getFields().values().stream()
			.map(Field::getGeoLocationPoint).filter(Objects::nonNull)
			.map(
				geoLocationPoint-> getMapMarker(
					geoLocationPoint, title, summary));
	}

}