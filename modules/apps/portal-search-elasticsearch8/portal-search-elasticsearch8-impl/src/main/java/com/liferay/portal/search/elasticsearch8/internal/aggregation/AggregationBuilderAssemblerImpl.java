/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.aggregation;

import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.aggregation.AggregationTranslator;
import com.liferay.portal.search.aggregation.FieldAggregation;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregationTranslator;


/**
 * @author André de Oliveira
 */
public class AggregationBuilderAssemblerImpl {

	public AggregationBuilderAssemblerImpl(
		AggregationTranslator<co.elastic.clients.elasticsearch._types.aggregations.Aggregation> aggregationTranslator,
		PipelineAggregationTranslator<PipelineAggregationBuilder>
			pipelineAggregationTranslator) {

		_aggregationTranslator = aggregationTranslator;
		_pipelineAggregationTranslator = pipelineAggregationTranslator;
	}

	public <A extends co.elastic.clients.elasticsearch._types.aggregations.Aggregation> A assembleAggregation(
		A elasticsearchAggregation, Aggregation aggregation) {

		_baseAggregationTranslator.translate(
			elasticsearchAggregation, aggregation, _aggregationTranslator,
			_pipelineAggregationTranslator);

		return elasticsearchAggregation;
	}

	public <VSAB extends ValuesSourceAggregationBuilder> VSAB
		assembleFieldAggregation(
			VSAB valuesSourceAggregationBuilder,
			FieldAggregation fieldAggregation) {

		_baseFieldAggregationTranslator.translate(
			baseMetricsAggregation -> valuesSourceAggregationBuilder,
			fieldAggregation, _aggregationTranslator,
			_pipelineAggregationTranslator);

		return valuesSourceAggregationBuilder;
	}

	private final AggregationTranslator<co.elastic.clients.elasticsearch._types.aggregations.Aggregation>
		_aggregationTranslator;
	private final BaseAggregationTranslator _baseAggregationTranslator =
		new BaseAggregationTranslator();
	private final BaseFieldAggregationTranslator
		_baseFieldAggregationTranslator = new BaseFieldAggregationTranslator();
	private final PipelineAggregationTranslator<PipelineAggregationBuilder>
		_pipelineAggregationTranslator;

}