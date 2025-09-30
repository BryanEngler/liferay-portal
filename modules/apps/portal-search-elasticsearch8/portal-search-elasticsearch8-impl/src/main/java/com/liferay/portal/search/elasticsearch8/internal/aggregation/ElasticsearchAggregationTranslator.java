/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.aggregation;

import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.AverageAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder.ContainerBuilder;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.aggregation.AggregationTranslator;
import com.liferay.portal.search.aggregation.AggregationVisitor;
import com.liferay.portal.search.aggregation.FieldAggregation;
import com.liferay.portal.search.aggregation.bucket.ChildrenAggregation;
import com.liferay.portal.search.aggregation.bucket.DateHistogramAggregation;
import com.liferay.portal.search.aggregation.bucket.DateRangeAggregation;
import com.liferay.portal.search.aggregation.bucket.DiversifiedSamplerAggregation;
import com.liferay.portal.search.aggregation.bucket.FilterAggregation;
import com.liferay.portal.search.aggregation.bucket.FiltersAggregation;
import com.liferay.portal.search.aggregation.bucket.GeoDistanceAggregation;
import com.liferay.portal.search.aggregation.bucket.GeoHashGridAggregation;
import com.liferay.portal.search.aggregation.bucket.GlobalAggregation;
import com.liferay.portal.search.aggregation.bucket.HistogramAggregation;
import com.liferay.portal.search.aggregation.bucket.MissingAggregation;
import com.liferay.portal.search.aggregation.bucket.NestedAggregation;
import com.liferay.portal.search.aggregation.bucket.RangeAggregation;
import com.liferay.portal.search.aggregation.bucket.ReverseNestedAggregation;
import com.liferay.portal.search.aggregation.bucket.SamplerAggregation;
import com.liferay.portal.search.aggregation.bucket.SignificantTermsAggregation;
import com.liferay.portal.search.aggregation.bucket.SignificantTextAggregation;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.aggregation.metrics.AvgAggregation;
import com.liferay.portal.search.aggregation.metrics.CardinalityAggregation;
import com.liferay.portal.search.aggregation.metrics.ExtendedStatsAggregation;
import com.liferay.portal.search.aggregation.metrics.GeoBoundsAggregation;
import com.liferay.portal.search.aggregation.metrics.GeoCentroidAggregation;
import com.liferay.portal.search.aggregation.metrics.MaxAggregation;
import com.liferay.portal.search.aggregation.metrics.MinAggregation;
import com.liferay.portal.search.aggregation.metrics.PercentileRanksAggregation;
import com.liferay.portal.search.aggregation.metrics.PercentilesAggregation;
import com.liferay.portal.search.aggregation.metrics.PercentilesMethod;
import com.liferay.portal.search.aggregation.metrics.ScriptedMetricAggregation;
import com.liferay.portal.search.aggregation.metrics.StatsAggregation;
import com.liferay.portal.search.aggregation.metrics.SumAggregation;
import com.liferay.portal.search.aggregation.metrics.TopHitsAggregation;
import com.liferay.portal.search.aggregation.metrics.ValueCountAggregation;
import com.liferay.portal.search.aggregation.metrics.WeightedAvgAggregation;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket.DateHistogramAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket.DateRangeAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket.FilterAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket.FiltersAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket.GeoDistanceAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket.HistogramAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket.RangeAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket.SignificantTermsAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket.SignificantTextAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket.TermsAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.metrics.ScriptedMetricAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.metrics.TopHitsAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.metrics.WeightedAvgAggregationTranslator;
import com.liferay.portal.search.elasticsearch8.internal.util.SetterUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(
	property = "search.engine.impl=Elasticsearch",
	service = AggregationTranslator.class
)
public class ElasticsearchAggregationTranslator
	implements AggregationTranslator
		<co.elastic.clients.elasticsearch._types.aggregations.Aggregation>,
			   AggregationVisitor
				   <co.elastic.clients.elasticsearch._types.aggregations.
					   Aggregation> {

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation
		translate(Aggregation aggregation) {

		return aggregation.accept(this);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation
		visit(AvgAggregation avgAggregation) {

		co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder
			aggregationBuilder =
				new co.elastic.clients.elasticsearch._types.aggregations.
					Aggregation.Builder();

		AverageAggregation.Builder averageAggregationBuilder =
			AggregationBuilders.avg();

		averageAggregationBuilder.field(avgAggregation.getField());

		SetterUtil.setNotNullFieldValue(
			averageAggregationBuilder::missing, avgAggregation.getMissing());
		SetterUtil.setNotNullScript(
			averageAggregationBuilder::script, avgAggregation.getScript());

		return _translateChildAggregations(
			avgAggregation,
			aggregationBuilder.avg(averageAggregationBuilder.build()));
	}

	private co.elastic.clients.elasticsearch._types.aggregations.Aggregation
		_translateChildAggregations(
			Aggregation aggregation, ContainerBuilder containerBuilder) {

		for (Aggregation childAggregation :
				aggregation.getChildrenAggregations()) {

			containerBuilder.aggregations(
				childAggregation.getName(), translate(childAggregation));
		}

		for (PipelineAggregation pipelineAggregation :
				aggregation.getPipelineAggregations()) {

			containerBuilder.aggregations(
				pipelineAggregation.getName(),
				_pipelineAggregationTranslator.translate(pipelineAggregation));
		}

		return containerBuilder.build();
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation
		visit(CardinalityAggregation cardinalityAggregation) {

		co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder
			aggregationBuilder =
				new co.elastic.clients.elasticsearch._types.aggregations.
					Aggregation.Builder();

		co.elastic.clients.elasticsearch._types.aggregations.
			CardinalityAggregation.Builder cardinalityAggregationBuilder =
				AggregationBuilders.cardinality();

		cardinalityAggregationBuilder.field(cardinalityAggregation.getField());

		SetterUtil.setNotNullFieldValue(
			cardinalityAggregationBuilder::missing,
			cardinalityAggregation.getMissing());
		SetterUtil.setNotNullInteger(
			cardinalityAggregationBuilder::precisionThreshold,
			cardinalityAggregation.getPrecisionThreshold());
		SetterUtil.setNotNullScript(
			cardinalityAggregationBuilder::script,
			cardinalityAggregation.getScript());

		return _translateChildAggregations(
			cardinalityAggregation,
			aggregationBuilder.cardinality(
				cardinalityAggregationBuilder.build()));
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(ChildrenAggregation childrenAggregation) {
		return _baseFieldAggregationTranslator.translate(
			baseMetricsAggregation -> new ChildrenAggregationBuilder(
				baseMetricsAggregation.getName(),
				childrenAggregation.getChildType()),
			childrenAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		DateHistogramAggregation dateHistogramAggregation) {

		return _assemble(
			_dateHistogramAggregationTranslator.translate(
				dateHistogramAggregation),
			dateHistogramAggregation);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(DateRangeAggregation dateRangeAggregation) {
		return _dateRangeAggregationTranslator.translate(
			dateRangeAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		DiversifiedSamplerAggregation diversifiedSamplerAggregation) {

		DiversifiedAggregationBuilder diversifiedAggregationBuilder =
			_baseFieldAggregationTranslator.translate(
				baseMetricsAggregation ->
					AggregationBuilders.diversifiedSampler(
						diversifiedSamplerAggregation.getName()),
				diversifiedSamplerAggregation, this,
				_pipelineAggregationTranslator);

		if (diversifiedSamplerAggregation.getExecutionHint() != null) {
			diversifiedAggregationBuilder.executionHint(
				diversifiedSamplerAggregation.getExecutionHint());
		}

		if (diversifiedSamplerAggregation.getMaxDocsPerValue() != null) {
			diversifiedAggregationBuilder.maxDocsPerValue(
				diversifiedSamplerAggregation.getMaxDocsPerValue());
		}

		if (diversifiedSamplerAggregation.getShardSize() != null) {
			diversifiedAggregationBuilder.shardSize(
				diversifiedSamplerAggregation.getShardSize());
		}

		return diversifiedAggregationBuilder;
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		ExtendedStatsAggregation extendedStatsAggregation) {

		ExtendedStatsAggregationBuilder extendedStatsAggregationBuilder =
			_baseFieldAggregationTranslator.translate(
				baseMetricsAggregation -> AggregationBuilders.extendedStats(
					baseMetricsAggregation.getName()),
				extendedStatsAggregation, this, _pipelineAggregationTranslator);

		if (extendedStatsAggregation.getSigma() != null) {
			extendedStatsAggregationBuilder.sigma(
				extendedStatsAggregation.getSigma());
		}

		return extendedStatsAggregationBuilder;
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(FilterAggregation filterAggregation) {
		return _filterAggregationTranslator.translate(
			filterAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(FiltersAggregation filtersAggregation) {
		return _filtersAggregationTranslator.translate(
			filtersAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(GeoBoundsAggregation geoBoundsAggregation) {
		GeoBoundsAggregationBuilder geoBoundsAggregationBuilder =
			_baseFieldAggregationTranslator.translate(
				baseMetricsAggregation -> AggregationBuilders.geoBounds(
					geoBoundsAggregation.getName()),
				geoBoundsAggregation, this, _pipelineAggregationTranslator);

		if (geoBoundsAggregation.getWrapLongitude() != null) {
			geoBoundsAggregationBuilder.wrapLongitude(
				geoBoundsAggregation.getWrapLongitude());
		}

		return geoBoundsAggregationBuilder;
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		GeoCentroidAggregation geoCentroidAggregation) {

		return _baseFieldAggregationTranslator.translate(
			baseMetricsAggregation -> AggregationBuilders.geoCentroid(
				geoCentroidAggregation.getName()),
			geoCentroidAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		GeoDistanceAggregation geoDistanceAggregation) {

		return _geoDistanceAggregationTranslator.translate(
			geoDistanceAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		GeoHashGridAggregation geoHashGridAggregation) {

		GeoGridAggregationBuilder geoGridAggregationBuilder =
			_baseFieldAggregationTranslator.translate(
				baseMetricsAggregation -> AggregationBuilders.geohashGrid(
					geoHashGridAggregation.getName()),
				geoHashGridAggregation, this, _pipelineAggregationTranslator);

		if (geoHashGridAggregation.getPrecision() != null) {
			geoGridAggregationBuilder.precision(
				geoHashGridAggregation.getPrecision());
		}

		if (geoHashGridAggregation.getShardSize() != null) {
			geoGridAggregationBuilder.shardSize(
				geoHashGridAggregation.getShardSize());
		}

		if (geoHashGridAggregation.getSize() != null) {
			geoGridAggregationBuilder.size(geoHashGridAggregation.getSize());
		}

		return geoGridAggregationBuilder;
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(GlobalAggregation globalAggregation) {
		return _assemble(
			AggregationBuilders.global(globalAggregation.getName()),
			globalAggregation);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(HistogramAggregation histogramAggregation) {
		return _histogramAggregationTranslator.translate(
			histogramAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(MaxAggregation maxAggregation) {
		return _baseFieldAggregationTranslator.translate(
			baseMetricsAggregation -> AggregationBuilders.max(
				baseMetricsAggregation.getName()),
			maxAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(MinAggregation minAggregation) {
		return _baseFieldAggregationTranslator.translate(
			baseMetricsAggregation -> AggregationBuilders.min(
				baseMetricsAggregation.getName()),
			minAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(MissingAggregation missingAggregation) {
		return _baseFieldAggregationTranslator.translate(
			baseMetricsAggregation -> AggregationBuilders.missing(
				baseMetricsAggregation.getName()),
			missingAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(NestedAggregation nestedAggregation) {
		return _assemble(
			AggregationBuilders.nested(
				nestedAggregation.getName(), nestedAggregation.getPath()),
			nestedAggregation);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		PercentileRanksAggregation percentileRanksAggregation) {

		PercentileRanksAggregationBuilder percentileRanksAggregationBuilder =
			_baseFieldAggregationTranslator.translate(
				baseMetricsAggregation -> AggregationBuilders.percentileRanks(
					baseMetricsAggregation.getName(),
					percentileRanksAggregation.getValues()),
				percentileRanksAggregation, this,
				_pipelineAggregationTranslator);

		if (percentileRanksAggregation.getCompression() != null) {
			percentileRanksAggregationBuilder.compression(
				percentileRanksAggregation.getCompression());
		}

		if (percentileRanksAggregation.getHdrSignificantValueDigits() != null) {
			percentileRanksAggregationBuilder.numberOfSignificantValueDigits(
				percentileRanksAggregation.getHdrSignificantValueDigits());
		}

		if (percentileRanksAggregation.getKeyed() != null) {
			percentileRanksAggregationBuilder.keyed(
				percentileRanksAggregation.getKeyed());
		}

		if (percentileRanksAggregation.getPercentilesMethod() != null) {
			PercentilesMethod percentilesMethod =
				percentileRanksAggregation.getPercentilesMethod();

			percentileRanksAggregationBuilder.method(
				org.elasticsearch.search.aggregations.metrics.PercentilesMethod.
					valueOf(percentilesMethod.name()));
		}

		return percentileRanksAggregationBuilder;
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		PercentilesAggregation percentilesAggregation) {

		PercentilesAggregationBuilder percentilesAggregationBuilder =
			_baseFieldAggregationTranslator.translate(
				baseMetricsAggregation -> AggregationBuilders.percentiles(
					baseMetricsAggregation.getName()),
				percentilesAggregation, this, _pipelineAggregationTranslator);

		if (percentilesAggregation.getCompression() != null) {
			percentilesAggregationBuilder.compression(
				percentilesAggregation.getCompression());
		}

		if (percentilesAggregation.getHdrSignificantValueDigits() != null) {
			percentilesAggregationBuilder.numberOfSignificantValueDigits(
				percentilesAggregation.getHdrSignificantValueDigits());
		}

		if (percentilesAggregation.getKeyed() != null) {
			percentilesAggregationBuilder.keyed(
				percentilesAggregation.getKeyed());
		}

		double[] percents = percentilesAggregation.getPercents();

		if (percents != null) {
			percentilesAggregationBuilder.percentiles(percents);
		}

		if (percentilesAggregation.getPercentilesMethod() != null) {
			PercentilesMethod percentilesMethod =
				percentilesAggregation.getPercentilesMethod();

			percentilesAggregationBuilder.method(
				org.elasticsearch.search.aggregations.metrics.PercentilesMethod.
					valueOf(percentilesMethod.name()));
		}

		return percentilesAggregationBuilder;
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(RangeAggregation rangeAggregation) {
		return _rangeAggregationTranslator.translate(
			rangeAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		ReverseNestedAggregation reverseNestedAggregation) {

		ReverseNestedAggregationBuilder reverseNestedAggregationBuilder =
			AggregationBuilders.reverseNested(
				reverseNestedAggregation.getName());

		if (reverseNestedAggregation.getPath() != null) {
			reverseNestedAggregationBuilder.path(
				reverseNestedAggregation.getPath());
		}

		return _assemble(
			reverseNestedAggregationBuilder, reverseNestedAggregation);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(SamplerAggregation samplerAggregation) {
		SamplerAggregationBuilder samplerAggregationBuilder =
			AggregationBuilders.sampler(samplerAggregation.getName());

		if (samplerAggregation.getShardSize() != null) {
			samplerAggregationBuilder.shardSize(
				samplerAggregation.getShardSize());
		}

		return _assemble(samplerAggregationBuilder, samplerAggregation);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		ScriptedMetricAggregation scriptedMetricAggregation) {

		return _scriptedMetricAggregationTranslator.translate(
			scriptedMetricAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		SignificantTermsAggregation significantTermsAggregation) {

		return _assemble(
			_significantTermsAggregationTranslator.translate(
				significantTermsAggregation),
			significantTermsAggregation);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		SignificantTextAggregation significantTextAggregation) {

		return _significantTextAggregationTranslator.translate(
			significantTextAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(StatsAggregation statsAggregation) {
		return _baseFieldAggregationTranslator.translate(
			baseMetricsAggregation -> AggregationBuilders.stats(
				baseMetricsAggregation.getName()),
			statsAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(SumAggregation sumAggregation) {
		return _baseFieldAggregationTranslator.translate(
			baseMetricsAggregation -> AggregationBuilders.sum(
				baseMetricsAggregation.getName()),
			sumAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(TermsAggregation termsAggregation) {
		return _assemble(
			_termsAggregationTranslator.translate(termsAggregation),
			termsAggregation);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(TopHitsAggregation topHitsAggregation) {
		return _topHitsAggregationTranslator.translate(
			topHitsAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		ValueCountAggregation valueCountAggregation) {

		return _baseFieldAggregationTranslator.translate(
			baseMetricsAggregation -> AggregationBuilders.count(
				baseMetricsAggregation.getName()),
			valueCountAggregation, this, _pipelineAggregationTranslator);
	}

	@Override
	public co.elastic.clients.elasticsearch._types.aggregations.Aggregation visit(
		WeightedAvgAggregation weightedAvgAggregation) {

		return _weightedAvgAggregationTranslator.translate(
			weightedAvgAggregation, this, _pipelineAggregationTranslator);
	}

	private <A extends co.elastic.clients.elasticsearch._types.aggregations.Aggregation> A _assemble(
		A elasticsearchAggregation, Aggregation aggregation) {

		AggregationBuilderAssemblerImpl aggregationBuilderAssemblerImpl =
			_aggregationBuilderAssemblerFactory.getAggregationBuilderAssembler(
				this);

		return aggregationBuilderAssemblerImpl.assembleAggregation(
			elasticsearchAggregation, aggregation);
	}

	private <VSAB extends ValuesSourceAggregationBuilder> VSAB _assemble(
		VSAB valuesSourceAggregationBuilder,
		FieldAggregation fieldAggregation) {

		AggregationBuilderAssemblerImpl aggregationBuilderAssemblerImpl =
			_aggregationBuilderAssemblerFactory.getAggregationBuilderAssembler(
				this);

		return aggregationBuilderAssemblerImpl.assembleFieldAggregation(
			valuesSourceAggregationBuilder, fieldAggregation);
	}

	@Reference
	private AggregationBuilderAssemblerFactory
		_aggregationBuilderAssemblerFactory;

	private final BaseFieldAggregationTranslator
		_baseFieldAggregationTranslator = new BaseFieldAggregationTranslator();

	@Reference
	private DateHistogramAggregationTranslator
		_dateHistogramAggregationTranslator;

	@Reference
	private DateRangeAggregationTranslator _dateRangeAggregationTranslator;

	@Reference
	private FilterAggregationTranslator _filterAggregationTranslator;

	@Reference
	private FiltersAggregationTranslator _filtersAggregationTranslator;

	@Reference
	private GeoDistanceAggregationTranslator _geoDistanceAggregationTranslator;

	@Reference
	private HistogramAggregationTranslator _histogramAggregationTranslator;

	@Reference(target = "(search.engine.impl=Elasticsearch)")
	private PipelineAggregationTranslator<PipelineAggregationBuilder>
		_pipelineAggregationTranslator;

	@Reference
	private RangeAggregationTranslator _rangeAggregationTranslator;

	@Reference
	private ScriptedMetricAggregationTranslator
		_scriptedMetricAggregationTranslator;

	@Reference
	private SignificantTermsAggregationTranslator
		_significantTermsAggregationTranslator;

	@Reference
	private SignificantTextAggregationTranslator
		_significantTextAggregationTranslator;

	@Reference
	private TermsAggregationTranslator _termsAggregationTranslator;

	@Reference
	private TopHitsAggregationTranslator _topHitsAggregationTranslator;

	@Reference
	private WeightedAvgAggregationTranslator _weightedAvgAggregationTranslator;

}