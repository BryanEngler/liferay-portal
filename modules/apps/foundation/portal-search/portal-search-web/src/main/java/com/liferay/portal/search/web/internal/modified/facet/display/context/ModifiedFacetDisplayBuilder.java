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

package com.liferay.portal.search.web.internal.modified.facet.display.context;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringPool;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Stream;

/**
 * @author Lino Alves
 */
public class ModifiedFacetDisplayBuilder implements Serializable {

	public ModifiedFacetDisplayContext build() {
		ModifiedFacetDisplayContext modifiedFacetDisplayContext =
			new ModifiedFacetDisplayContext();

		modifiedFacetDisplayContext.setCalendarDisplayContext(
			buildCalendarDisplayContext());
		modifiedFacetDisplayContext.setCustomRangeTermDisplayContext(
			buildCustomRangeTermDisplayContext());
		modifiedFacetDisplayContext.setDefaultTermDisplayContext(
			buildDefaultTermDisplay());
		modifiedFacetDisplayContext.setNothingSelected(isNothingSelected());
		modifiedFacetDisplayContext.setParameterName(_parameterName);
		modifiedFacetDisplayContext.setTermDisplayContexts(
			buildTermDisplayContexts());

		return modifiedFacetDisplayContext;
	}

	public void setFacet(Facet facet) {
		_facet = facet;
	}

	public void setLocale(Locale locale) {
		_locale = locale;
	}

	public void setParameterName(String paramName) {
		_parameterName = paramName;
	}

	public void setParameterValues(String... parameterValues) {
		_selectedRanges = Arrays.asList(
			Objects.requireNonNull(parameterValues));
	}

	public void setRangesJSONArray(JSONArray rangesJSONArray) {
		_rangesJSONArray = rangesJSONArray;
	}

	public void setTimeZone(TimeZone timeZone) {
		_timeZone = timeZone;
	}

	protected ModifiedFacetCalendarDisplayContext
		buildCalendarDisplayContext() {

		ModifiedFacetCalendarDisplayBuilder
			modifiedFacetCalendarDisplayBuilder =
				new ModifiedFacetCalendarDisplayBuilder();

		Stream<String> selectedRangesStream = _selectedRanges.stream();

		selectedRangesStream.filter(
			s -> s.startsWith(StringPool.OPEN_CURLY_BRACE)
		).findAny(
		).ifPresent(
			modifiedFacetCalendarDisplayBuilder::setRangeString
		);

		modifiedFacetCalendarDisplayBuilder.setLocale(_locale);
		modifiedFacetCalendarDisplayBuilder.setTimeZone(_timeZone);

		return modifiedFacetCalendarDisplayBuilder.build();
	}

	protected ModifiedFacetTermDisplayContext
		buildCustomRangeTermDisplayContext() {

		boolean selected = true;

		Map<String, Object> data = new HashMap<>();

		FacetCollector facetCollector = _facet.getFacetCollector();

		TermCollector termCollector = null;

		if (selected) {
			String term = "";

			termCollector = facetCollector.getTermCollector(term);
		}

		ModifiedFacetTermDisplayContext customRangeTermDisplayContext =
			buildTermDisplay(
				StringPool.BLANK, StringPool.BLANK, selected, data,
				getFrequency(termCollector));

		return customRangeTermDisplayContext;
	}

	protected ModifiedFacetTermDisplayContext
		buildDefaultTermDisplay() {

		Map<String, Object> data = new HashMap<>();

		data.put("selection", 0);
		data.put("value", StringPool.BLANK);

		boolean selected = true;
		int frequency = 0;

		String label = _facet.getFacetConfiguration().getLabel();

		ModifiedFacetTermDisplayContext defaultTermDisplayContext =
			buildTermDisplay(label, label, selected, data, frequency);

		return defaultTermDisplayContext;
	}

	protected ModifiedFacetTermDisplayContext buildTermDisplay(
		String dataTermId, String label, boolean selected,
		Map<String, Object> data, int frequency) {

		ModifiedFacetTermDisplayContext modifiedSearchFacetTermDisplayContext =
			new ModifiedFacetTermDisplayContext();

		modifiedSearchFacetTermDisplayContext.setFrequency(frequency);
		modifiedSearchFacetTermDisplayContext.setLabel(label);
		modifiedSearchFacetTermDisplayContext.setRange(dataTermId);
		modifiedSearchFacetTermDisplayContext.setSelected(selected);

		return modifiedSearchFacetTermDisplayContext;
	}

	protected List<ModifiedFacetTermDisplayContext> buildTermDisplayContexts() {
		List<ModifiedFacetTermDisplayContext>
			modifiedSearchFacetTermDisplayContexts = new ArrayList<>();

		FacetCollector facetCollector = _facet.getFacetCollector();

		int index = 0;

		for (int i = 0; i < _rangesJSONArray.length(); i++) {
			JSONObject rangeJSONObject = _rangesJSONArray.getJSONObject(i);

			String label = rangeJSONObject.getString("label");
			String range = rangeJSONObject.getString("range");

			index = i + 1;

			boolean selected = _selectedRanges.contains(label);

			Map<String, Object> data = new HashMap<>();

			data.put("selection", index);
			data.put("value", HtmlUtil.escape(range));

			TermCollector termCollector = null;

			if (facetCollector != null) {
				termCollector = facetCollector.getTermCollector(range);
			}

			ModifiedFacetTermDisplayContext
				modifiedSearchFacetTermDisplayContext = buildTermDisplay(
					label, label, selected, data, getFrequency(termCollector));

			modifiedSearchFacetTermDisplayContexts.add(
				modifiedSearchFacetTermDisplayContext);
		}

		return modifiedSearchFacetTermDisplayContexts;
	}

	protected int getFrequency(TermCollector termCollector) {
		if (termCollector != null) {
			return termCollector.getFrequency();
		}

		return 0;
	}

	protected boolean isNothingSelected() {
		return _selectedRanges.isEmpty();
	}

	private Facet _facet;
	private Locale _locale;
	private String _parameterName;
	private JSONArray _rangesJSONArray;
	private List<String> _selectedRanges = Collections.emptyList();
	private TimeZone _timeZone;

}