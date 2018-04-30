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

import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;

/**
 * @author Bryan Engler
 */
public abstract class BaseFacetProcessor
	implements FacetProcessor<SearchRequestBuilder> {

	protected void addFilteredAggregation(
		SearchRequestBuilder searchRequestBuilder, Facet facet,
		AbstractAggregationBuilder subaggregationBuilder,
		Map<String, List<QueryBuilder>> filterAggregationQueryBuildersMap) {

		String facetAggregationName = getAggregationName(facet);

		BoolQueryBuilder filterBoolQueryBuilder = QueryBuilders.boolQuery();

		for (Map.Entry<String, List<QueryBuilder>> entry :
				filterAggregationQueryBuildersMap.entrySet()) {

			String filterAggregationName = entry.getKey();

			if (!filterAggregationName.equals(facetAggregationName)) {
				List<QueryBuilder> filterAggregationQueryBuilders =
					entry.getValue();

				for (QueryBuilder filterAggregationQueryBuilder :
						filterAggregationQueryBuilders) {

					filterBoolQueryBuilder.must(filterAggregationQueryBuilder);
				}
			}
		}

		if (!filterBoolQueryBuilder.hasClauses()) {
			searchRequestBuilder.addAggregation(subaggregationBuilder);

			return;
		}

		FilterAggregationBuilder filterAggregationBuilder =
			new FilterAggregationBuilder(
				facetAggregationName, filterBoolQueryBuilder);

		filterAggregationBuilder.subAggregation(subaggregationBuilder);

		searchRequestBuilder.addAggregation(filterAggregationBuilder);
	}

	protected String getAggregationName(Facet facet) {
		FacetConfiguration facetConfiguration = facet.getFacetConfiguration();

		JSONObject data = facetConfiguration.getData();

		return data.getString("aggregationName", facet.getFieldName());
	}

}