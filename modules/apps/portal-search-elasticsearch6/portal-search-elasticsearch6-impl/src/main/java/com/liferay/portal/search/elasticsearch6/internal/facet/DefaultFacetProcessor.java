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

package com.liferay.portal.search.elasticsearch6.internal.facet;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 * @author Milen Dyankov
 */
@Component(
	immediate = true, property = "class.name=DEFAULT",
	service = FacetProcessor.class
)
public class DefaultFacetProcessor extends BaseFacetProcessor {

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

		TermsAggregationBuilder termsAggregationBuilder =
			AggregationBuilders.terms(fieldName);

		termsAggregationBuilder.field(fieldName);

		JSONObject data = facetConfiguration.getData();

		int minDocCount = data.getInt("frequencyThreshold");

		if (minDocCount > 0) {
			termsAggregationBuilder.minDocCount(minDocCount);
		}

		int size = data.getInt("maxTerms");

		if (size > 0) {
			termsAggregationBuilder.size(size);
		}

		if (filterAggregationQueryBuildersMap.isEmpty()) {
			searchRequestBuilder.addAggregation(termsAggregationBuilder);

			return;
		}

		addFilteredAggregations(
			searchRequestBuilder, fieldName, termsAggregationBuilder,
			filterAggregationQueryBuildersMap);
	}

}