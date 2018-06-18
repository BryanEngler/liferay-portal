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

import com.liferay.portal.kernel.search.facet.collector.DefaultTermCollector;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.solr.common.util.SimpleOrderedMap;

/**
 * @author Raymond Augé
 */
public class SolrFacetFieldCollector implements FacetCollector {

	public SolrFacetFieldCollector(String fieldName, Object bucketList) {
		_fieldName = fieldName;

		_counts.putAll(getCounts((SimpleOrderedMap)bucketList));
	}

	@Override
	public String getFieldName() {
		return _fieldName;
	}

	@Override
	public TermCollector getTermCollector(String term) {
		int count = 0;

		try {
			count = _counts.get(term);
		}
		catch (Exception e) {
		}

		return new DefaultTermCollector(term, count);
	}

	@Override
	public List<TermCollector> getTermCollectors() {
		if (_termCollectors != null) {
			return _termCollectors;
		}

		List<TermCollector> termCollectors = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : _counts.entrySet()) {
			int count = entry.getValue();

			TermCollector termCollector = new DefaultTermCollector(
				entry.getKey(), count);

			termCollectors.add(termCollector);
		}

		_termCollectors = termCollectors;

		return _termCollectors;
	}

	protected Map<String, Integer> getCounts(SimpleOrderedMap bucketList) {
		Map<String, Integer> bucketValueCountsMap = new HashMap<>();

		List<SimpleOrderedMap> buckets = (ArrayList)bucketList.get("buckets");

		Stream<SimpleOrderedMap> stream = buckets.stream();

		bucketValueCountsMap = stream.collect(
			Collectors.toMap(
				bucket -> (String)bucket.get("val"),
				bucket -> (Integer)bucket.get("count")));

		return bucketValueCountsMap;
	}

	private final Map<String, Integer> _counts = new LinkedHashMap<>();
	private final String _fieldName;
	private List<TermCollector> _termCollectors;

}