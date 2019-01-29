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

package com.liferay.portal.search.elasticsearch6.internal.aggregation.pipeline;

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.aggregation.pipeline.AvgBucketPipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.CumulativeSumPipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.DerivativePipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.ExtendedStatsBucketPipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.MaxBucketPipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.MinBucketPipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.PercentilesBucketPipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregationTranslator;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregationVisitor;
import com.liferay.portal.search.aggregation.pipeline.SerialDiffPipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.StatsBucketPipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.SumBucketPipelineAggregation;

import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.percentile.PercentilesBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.stats.extended.ExtendedStatsBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.cumulativesum.CumulativeSumPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.derivative.DerivativePipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.serialdiff.SerialDiffPipelineAggregationBuilder;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 */
@Component(
	property = "search.engine.impl=Elasticsearch",
	service = {
		PipelineAggregationTranslator.class, PipelineAggregationVisitor.class
	}
)
public class ElasticsearchPipelineAggregationVisitor
	implements PipelineAggregationTranslator<PipelineAggregationBuilder>,
			   PipelineAggregationVisitor<PipelineAggregationBuilder> {

	@Override
	public PipelineAggregationBuilder translate(
		PipelineAggregation pipelineAggregation) {

		return pipelineAggregation.accept(this);
	}

	@Override
	public PipelineAggregationBuilder visit(
		AvgBucketPipelineAggregation avgBucketPipelineAggregation) {

		return _bucketMetricsPipelineAggregationTranslator.translate(
			bucketMetricsPipelineAggregation ->
				PipelineAggregatorBuilders.avgBucket(
					bucketMetricsPipelineAggregation.getName(),
					bucketMetricsPipelineAggregation.getBucketsPath()),
			avgBucketPipelineAggregation);
	}

	@Override
	public PipelineAggregationBuilder visit(
		CumulativeSumPipelineAggregation cumulativeSumPipelineAggregation) {

		CumulativeSumPipelineAggregationBuilder
			cumulativeSumPipelineAggregationBuilder =
				PipelineAggregatorBuilders.cumulativeSum(
					cumulativeSumPipelineAggregation.getName(),
					cumulativeSumPipelineAggregation.getBucketsPath());

		if (cumulativeSumPipelineAggregation.getFormat() != null) {
			cumulativeSumPipelineAggregationBuilder.format(
				cumulativeSumPipelineAggregation.getFormat());
		}

		return cumulativeSumPipelineAggregationBuilder;
	}

	@Override
	public PipelineAggregationBuilder visit(
		DerivativePipelineAggregation derivativePipelineAggregation) {

		DerivativePipelineAggregationBuilder
			derivativePipelineAggregationBuilder =
				PipelineAggregatorBuilders.derivative(
					derivativePipelineAggregation.getName(),
					derivativePipelineAggregation.getBucketsPath());

		if (derivativePipelineAggregation.getFormat() != null) {
			derivativePipelineAggregationBuilder.format(
				derivativePipelineAggregation.getFormat());
		}

		if (derivativePipelineAggregation.getGapPolicy() != null) {
			derivativePipelineAggregationBuilder.gapPolicy(
				_gapPolicyTranslator.translate(
					derivativePipelineAggregation.getGapPolicy()));
		}

		if (derivativePipelineAggregation.getUnit() != null) {
			derivativePipelineAggregationBuilder.unit(
				derivativePipelineAggregation.getUnit());
		}

		return derivativePipelineAggregationBuilder;
	}

	@Override
	public PipelineAggregationBuilder visit(
		ExtendedStatsBucketPipelineAggregation
			extendedStatsBucketPipelineAggregation) {

		ExtendedStatsBucketPipelineAggregationBuilder
			extendedStatsBucketPipelineAggregationBuilder =
				_bucketMetricsPipelineAggregationTranslator.translate(
					bucketMetricsPipelineAggregation ->
						PipelineAggregatorBuilders.extendedStatsBucket(
							bucketMetricsPipelineAggregation.getName(),
							bucketMetricsPipelineAggregation.getBucketsPath()),
					extendedStatsBucketPipelineAggregation);

		if (extendedStatsBucketPipelineAggregation.getSigma() != null) {
			extendedStatsBucketPipelineAggregationBuilder.sigma(
				extendedStatsBucketPipelineAggregation.getSigma());
		}

		return extendedStatsBucketPipelineAggregationBuilder;
	}

	@Override
	public PipelineAggregationBuilder visit(
		MaxBucketPipelineAggregation maxBucketPipelineAggregation) {

		return _bucketMetricsPipelineAggregationTranslator.translate(
			bucketMetricsPipelineAggregation ->
				PipelineAggregatorBuilders.maxBucket(
					bucketMetricsPipelineAggregation.getName(),
					bucketMetricsPipelineAggregation.getBucketsPath()),
			maxBucketPipelineAggregation);
	}

	@Override
	public PipelineAggregationBuilder visit(
		MinBucketPipelineAggregation minBucketPipelineAggregation) {

		return _bucketMetricsPipelineAggregationTranslator.translate(
			bucketMetricsPipelineAggregation ->
				PipelineAggregatorBuilders.minBucket(
					bucketMetricsPipelineAggregation.getName(),
					bucketMetricsPipelineAggregation.getBucketsPath()),
			minBucketPipelineAggregation);
	}

	@Override
	public PipelineAggregationBuilder visit(
		PercentilesBucketPipelineAggregation
			percentilesBucketPipelineAggregation) {

		PercentilesBucketPipelineAggregationBuilder
			percentilesBucketPipelineAggregationBuilder =
				_bucketMetricsPipelineAggregationTranslator.translate(
					bucketMetricsPipelineAggregation ->
						PipelineAggregatorBuilders.percentilesBucket(
							bucketMetricsPipelineAggregation.getName(),
							bucketMetricsPipelineAggregation.getBucketsPath()),
					percentilesBucketPipelineAggregation);

		if (!ArrayUtil.isEmpty(
				percentilesBucketPipelineAggregation.getPercents())) {

			percentilesBucketPipelineAggregationBuilder.percents(
				percentilesBucketPipelineAggregation.getPercents());
		}

		return percentilesBucketPipelineAggregationBuilder;
	}

	@Override
	public PipelineAggregationBuilder visit(
		SerialDiffPipelineAggregation serialDiffPipelineAggregation) {

		SerialDiffPipelineAggregationBuilder
			serialDiffPipelineAggregationBuilder =
				PipelineAggregatorBuilders.diff(
					serialDiffPipelineAggregation.getName(),
					serialDiffPipelineAggregation.getBucketsPath());

		if (serialDiffPipelineAggregation.getFormat() != null) {
			serialDiffPipelineAggregationBuilder.format(
				serialDiffPipelineAggregation.getFormat());
		}

		if (serialDiffPipelineAggregation.getGapPolicy() != null) {
			serialDiffPipelineAggregationBuilder.gapPolicy(
				_gapPolicyTranslator.translate(
					serialDiffPipelineAggregation.getGapPolicy()));
		}

		if (serialDiffPipelineAggregation.getLag() != null) {
			serialDiffPipelineAggregationBuilder.lag(
				serialDiffPipelineAggregation.getLag());
		}

		return serialDiffPipelineAggregationBuilder;
	}

	@Override
	public PipelineAggregationBuilder visit(
		StatsBucketPipelineAggregation statsBucketPipelineAggregation) {

		return _bucketMetricsPipelineAggregationTranslator.translate(
			bucketMetricsPipelineAggregation ->
				PipelineAggregatorBuilders.statsBucket(
					bucketMetricsPipelineAggregation.getName(),
					bucketMetricsPipelineAggregation.getBucketsPath()),
			statsBucketPipelineAggregation);
	}

	@Override
	public PipelineAggregationBuilder visit(
		SumBucketPipelineAggregation sumBucketPipelineAggregation) {

		return _bucketMetricsPipelineAggregationTranslator.translate(
			bucketMetricsPipelineAggregation ->
				PipelineAggregatorBuilders.sumBucket(
					bucketMetricsPipelineAggregation.getName(),
					bucketMetricsPipelineAggregation.getBucketsPath()),
			sumBucketPipelineAggregation);
	}

	private final BucketMetricsPipelineAggregationTranslator
		_bucketMetricsPipelineAggregationTranslator =
			new BucketMetricsPipelineAggregationTranslator();
	private final GapPolicyTranslator _gapPolicyTranslator =
		new GapPolicyTranslator();

}