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
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.search.facet.util.RangeParserUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch.facet.FacetProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 * @author Milen Dyankov
 * @author Tibor Lipusz
 */
@Component(
	immediate = true,
	property = {"class.name=com.liferay.portal.kernel.search.facet.RangeFacet"},
	service = FacetProcessor.class
)
public class RangeFacetProcessor extends BaseFacetProcessor {

	@Override
	public void processFacet(
		SearchRequestBuilder searchRequestBuilder, Facet facet) {

		processFacet(searchRequestBuilder, facet, new HashMap<>());
	}

	@Override
	public void processFacet(
		SearchRequestBuilder searchRequestBuilder, Facet facet,
		Map<String, List<QueryBuilder>> filterAggregationQueryBuildersMap) {

		FacetConfiguration facetConfiguration = facet.getFacetConfiguration();

		String fieldName = facetConfiguration.getFieldName();

		DefaultRangeBuilder defaultRangeBuilder = new DefaultRangeBuilder(
			fieldName);

		defaultRangeBuilder.field(fieldName);

		addConfigurationRanges(facetConfiguration, defaultRangeBuilder);

		addCustomRange(facet, defaultRangeBuilder);

		if (!defaultRangeBuilder.hasRanges()) {
			return;
		}

		JSONObject data = facetConfiguration.getData();

		boolean filterAggregations = data.getBoolean(
			"filterAggregations", true);

		if (filterAggregationQueryBuildersMap.isEmpty() ||
			!filterAggregations) {

			searchRequestBuilder.addAggregation(defaultRangeBuilder);

			return;
		}

		addFilteredAggregations(
			searchRequestBuilder, fieldName, defaultRangeBuilder,
			filterAggregationQueryBuildersMap);
	}

	protected void addConfigurationRanges(
		FacetConfiguration facetConfiguration,
		DefaultRangeBuilder defaultRangeBuilder) {

		JSONObject jsonObject = facetConfiguration.getData();

		JSONArray jsonArray = jsonObject.getJSONArray("ranges");

		if (jsonArray == null) {
			return;
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject rangeJSONObject = jsonArray.getJSONObject(i);

			String rangeString = rangeJSONObject.getString("range");

			String[] range = RangeParserUtil.parserRange(rangeString);

			defaultRangeBuilder.addRange(range[0], range[1]);
		}
	}

	protected void addCustomRange(
		Facet facet, DefaultRangeBuilder defaultRangeBuilder) {

		SearchContext searchContext = facet.getSearchContext();

		String rangeString = GetterUtil.getString(
			searchContext.getAttribute(facet.getFieldId()));

		if (Validator.isNull(rangeString)) {
			return;
		}

		String[] range = RangeParserUtil.parserRange(rangeString);

		defaultRangeBuilder.addRange(range[0], range[1]);
	}

}