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

import com.liferay.portal.kernel.search.Field;

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

	protected void addFilteredAggregations(
		SearchRequestBuilder searchRequestBuilder, String fieldName,
		AbstractAggregationBuilder subaggregationBuilder,
		Map<String, List<QueryBuilder>> filterAggregationQueryBuildersMap) {

		BoolQueryBuilder filterBoolQueryBuilder = QueryBuilders.boolQuery();

		for (Map.Entry<String, List<QueryBuilder>> entry :
				filterAggregationQueryBuildersMap.entrySet()) {

			String filterFieldName = entry.getKey();

			if (!fieldName.equals(filterFieldName)) {
				List<QueryBuilder> filterAggregationQueryBuilders =
					filterAggregationQueryBuildersMap.get(filterFieldName);

				if (filterFieldName.equals(Field.MODIFIED_DATE)) {
					BoolQueryBuilder modifiedBoolQueryBuilder =
						QueryBuilders.boolQuery();

					for (QueryBuilder filterAggregationQueryBuilder :
							filterAggregationQueryBuilders) {

						modifiedBoolQueryBuilder.should(
							filterAggregationQueryBuilder);
					}

					filterBoolQueryBuilder.must(modifiedBoolQueryBuilder);
				}
				else {
					for (QueryBuilder filterAggregationQueryBuilder :
							filterAggregationQueryBuilders) {

						filterBoolQueryBuilder.must(
							filterAggregationQueryBuilder);
					}
				}
			}
		}

		if (!filterBoolQueryBuilder.hasClauses()) {
			searchRequestBuilder.addAggregation(subaggregationBuilder);

			return;
		}

		FilterAggregationBuilder filterAggregationBuilder =
			new FilterAggregationBuilder(fieldName);

		filterAggregationBuilder.filter(filterBoolQueryBuilder);

		filterAggregationBuilder.subAggregation(subaggregationBuilder);

		searchRequestBuilder.addAggregation(filterAggregationBuilder);
	}

}