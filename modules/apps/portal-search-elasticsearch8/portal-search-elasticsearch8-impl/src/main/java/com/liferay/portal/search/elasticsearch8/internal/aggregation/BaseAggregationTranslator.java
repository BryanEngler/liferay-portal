/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.aggregation;

import co.elastic.clients.elasticsearch._types.aggregations.PipelineAggregationBase;

import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.aggregation.AggregationTranslator;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregationTranslator;

/**
 * @author Michael C. Han
 */
public class BaseAggregationTranslator {

	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation
		translate(
			co.elastic.clients.elasticsearch._types.aggregations.Aggregation
				elasticsearchAggregation,
			Aggregation aggregation,
			AggregationTranslator
				<co.elastic.clients.elasticsearch._types.aggregations.
					Aggregation> aggregationTranslator,
			PipelineAggregationTranslator<PipelineAggregationBase>
				pipelineAggregationTranslator) {

		for (Aggregation childAggregation :
				aggregation.getChildrenAggregations()) {

			elasticsearchAggregation.subAggregation(
				aggregationTranslator.translate(childAggregation));
		}

		for (PipelineAggregation pipelineAggregation :
				aggregation.getPipelineAggregations()) {

			elasticsearchAggregation.subAggregation(
				pipelineAggregationTranslator.translate(pipelineAggregation));
		}

		return elasticsearchAggregation;
	}

}