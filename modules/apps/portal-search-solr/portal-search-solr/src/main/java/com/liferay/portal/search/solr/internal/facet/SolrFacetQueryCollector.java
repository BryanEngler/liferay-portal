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

package com.liferay.portal.search.solr.internal.facet;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.DefaultTermCollector;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.util.SimpleOrderedMap;

/**
 * @author Raymond Augé
 */
public class SolrFacetQueryCollector implements FacetCollector {

	public SolrFacetQueryCollector(
		Facet facet, Map<String, SimpleOrderedMap> responseFacetsMap) {

		_fieldName = facet.getFieldName();

		Map<String, String> rangeMap = _getRangeMap(facet);

		for (Map.Entry<String, SimpleOrderedMap> entry :
				responseFacetsMap.entrySet()) {

			String facetName = entry.getKey();

			if (!facetName.startsWith(_fieldName)) {
				continue;
			}

			SimpleOrderedMap value = entry.getValue();

			int count = (int)value.get("count");

			String[] nameLabelArray = StringUtil.split(facetName, "_");

			String label = nameLabelArray[1];

			_addCount(label, rangeMap, count);
		}
	}

	@Override
	public String getFieldName() {
		return _fieldName;
	}

	@Override
	public TermCollector getTermCollector(String term) {
		return new DefaultTermCollector(
			term, GetterUtil.getInteger(_counts.get(term)));
	}

	@Override
	public List<TermCollector> getTermCollectors() {
		if (_termCollectors != null) {
			return _termCollectors;
		}

		List<TermCollector> termCollectors = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : _counts.entrySet()) {
			Integer count = entry.getValue();

			TermCollector termCollector = new DefaultTermCollector(
				entry.getKey(), count.intValue());

			termCollectors.add(termCollector);
		}

		_termCollectors = termCollectors;

		return _termCollectors;
	}

	private void _addCount(
		String label, Map<String, String> rangeMap, int count) {

		if (label.equals("custom-range")) {
			_counts.put("custom-range", count);

			return;
		}

		_counts.put(rangeMap.get(label), count);
	}

	private Map<String, String> _getRangeMap(Facet facet) {
		FacetConfiguration facetConfiguration = facet.getFacetConfiguration();

		JSONObject jsonObject = facetConfiguration.getData();

		JSONArray jsonArray = jsonObject.getJSONArray("ranges");

		if (jsonArray == null) {
			return Collections.emptyMap();
		}

		Map<String, String> rangeMap = new HashMap<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject rangeJSONObject = jsonArray.getJSONObject(i);

			String label = rangeJSONObject.getString("label");
			String range = rangeJSONObject.getString("range");

			rangeMap.put(label, range);
		}

		return rangeMap;
	}

	private final Map<String, Integer> _counts = new HashMap<>();
	private final String _fieldName;
	private List<TermCollector> _termCollectors;

}