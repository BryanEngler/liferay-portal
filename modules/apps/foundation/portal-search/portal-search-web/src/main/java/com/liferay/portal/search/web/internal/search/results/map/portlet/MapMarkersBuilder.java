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

package com.liferay.portal.search.web.internal.search.results.map.portlet;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.search.web.internal.result.display.builder.SearchResultSummaryDisplayBuilder;
import com.liferay.portal.search.web.internal.result.display.context.SearchResultSummaryDisplayContext;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author André de Oliveira
 */
public class MapMarkersBuilder {

	public String buildMapMarkersJSON(List<Document> documents) {
		JSONArray locations = JSONFactoryUtil.createJSONArray();

		documents.stream().flatMap(this::getMapMarkers).forEach(locations::put);

		return locations.toString();
	}

	public void setHighlightEnabled(boolean highlightEnabled) {
		_highlightEnabled = highlightEnabled;
	}

	public void setHighlights(String[] highlights) {
		_highlights = highlights;
	}

	public void setLocale(Locale locale) {
		_locale = locale;
	}

	public void setRenderRequest(RenderRequest renderRequest) {
		_renderRequest = renderRequest;
	}

	public void setRenderResponse(RenderResponse renderResponse) {
		_renderResponse = renderResponse;
	}

	protected JSONObject getMapMarker(
		GeoLocationPoint geoLocationPoint, String title, String summary) {

		JSONObject jObj = JSONFactoryUtil.createJSONObject();

		jObj.put("lat", geoLocationPoint.getLatitude());
		jObj.put("lng", geoLocationPoint.getLongitude());
		jObj.put("summary", summary);
		jObj.put("title", title);

		return jObj;
	}

	protected Stream<JSONObject> getMapMarkers(Document document) {
		SearchResultSummaryDisplayContext searchResultSummaryDisplayContext =
			getSummary(document);

		String title = searchResultSummaryDisplayContext.getHighlightedTitle();
		String content = searchResultSummaryDisplayContext.getContent();

		Stream<Field> fields = document.getFields().values().stream();

		Stream<GeoLocationPoint> geoLocationPoints = fields.map(
			Field::getGeoLocationPoint).filter(Objects::nonNull);

		return geoLocationPoints.map(
			geoLocationPoint -> getMapMarker(geoLocationPoint, title, content));
	}

	protected SearchResultSummaryDisplayContext getSummary(Document document) {
		SearchResultSummaryDisplayBuilder searchResultSummaryBuilder =
			new SearchResultSummaryDisplayBuilder();

		searchResultSummaryBuilder.setAbridged(true);
		searchResultSummaryBuilder.setDocument(document);
		searchResultSummaryBuilder.setHighlightEnabled(_highlightEnabled);
		searchResultSummaryBuilder.setLocale(_locale);
		searchResultSummaryBuilder.setQueryTerms(_highlights);
		searchResultSummaryBuilder.setRenderRequest(_renderRequest);
		searchResultSummaryBuilder.setRenderResponse(_renderResponse);

		try {
			return searchResultSummaryBuilder.build();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean _highlightEnabled;
	private String[] _highlights;
	private Locale _locale;
	private RenderRequest _renderRequest;
	private RenderResponse _renderResponse;

}