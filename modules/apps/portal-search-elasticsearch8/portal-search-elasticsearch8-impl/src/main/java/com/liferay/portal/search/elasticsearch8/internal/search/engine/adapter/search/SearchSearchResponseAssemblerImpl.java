/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.search.engine.adapter.search;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.JsonData;

import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.AggregationResultTranslator;
import com.liferay.portal.search.aggregation.AggregationResults;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregation;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregationResultTranslator;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.AggregationResultTranslatorFactory;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.ElasticsearchAggregationResultTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.ElasticsearchAggregationResultsTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.ElasticsearchPipelineAggregationResultTranslator;
import com.liferay.portal.search.elasticsearch8.internal.aggregation.PipelineAggregationResultTranslatorFactory;
import com.liferay.portal.search.elasticsearch8.internal.hits.HitsMetadataTranslator;
import com.liferay.portal.search.elasticsearch8.internal.search.response.SearchHitDocumentTranslator;
import com.liferay.portal.search.elasticsearch8.internal.search.response.SearchResponseTranslator;
import com.liferay.portal.search.elasticsearch8.internal.stats.StatsTranslator;
import com.liferay.portal.search.elasticsearch8.internal.util.SetterUtil;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.geolocation.GeoBuilders;
import com.liferay.portal.search.groupby.GroupByResponseFactory;
import com.liferay.portal.search.highlight.HighlightFieldBuilderFactory;
import com.liferay.portal.search.hits.SearchHitBuilderFactory;
import com.liferay.portal.search.hits.SearchHitsBuilderFactory;
import com.liferay.portal.search.legacy.stats.StatsRequestBuilderFactory;
import com.liferay.portal.search.legacy.stats.StatsResultsTranslator;
import com.liferay.portal.search.searcher.SearchTimeValue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(service = SearchSearchResponseAssembler.class)
public class SearchSearchResponseAssemblerImpl
	implements AggregationResultTranslatorFactory,
			   PipelineAggregationResultTranslatorFactory,
			   SearchSearchResponseAssembler {

	@Override
	public void assemble(
		String searchRequestString, SearchResponse searchResponse,
		SearchSearchRequest searchSearchRequest,
		SearchSearchResponse searchSearchResponse) {

		_commonSearchResponseAssembler.assemble(
			searchSearchRequest, searchSearchResponse, searchRequestString,
			searchResponse);

		_addAggregations(
			searchResponse, searchSearchResponse, searchSearchRequest);
		setCount(searchResponse, searchSearchResponse);
		_setScrollId(searchResponse, searchSearchResponse);
		_setSearchHits(
			searchResponse, searchSearchResponse, searchSearchRequest);
		_setSearchTimeValue(searchResponse, searchSearchResponse);

		_searchResponseTranslator.populate(
			searchSearchResponse, searchResponse, searchSearchRequest);
	}

	@Override
	public AggregationResultTranslator createAggregationResultTranslator(
		Aggregate aggregate) {

		return new ElasticsearchAggregationResultTranslator(
			aggregate, _aggregationResults, _geoBuilders,
			new HitsMetadataTranslator(
				_documentBuilderFactory, _geoBuilders,
				_highlightFieldBuilderFactory, _searchHitBuilderFactory,
				_searchHitsBuilderFactory));
	}

	@Override
	public PipelineAggregationResultTranslator
		createPipelineAggregationResultTranslator(Aggregate aggregate) {

		return new ElasticsearchPipelineAggregationResultTranslator(
			aggregate, _aggregationResults);
	}

	@Activate
	protected void activate() {
		_searchResponseTranslator = new SearchResponseTranslator(
			_groupByResponseFactory, _searchHitDocumentTranslator,
			_statsRequestBuilderFactory, _statsResultsTranslator,
			_statsTranslator);
	}

	protected void setCount(
		SearchResponse searchResponse,
		SearchSearchResponse searchSearchResponse) {

		HitsMetadata<JsonData> hitsMetadata = searchResponse.hits();

		TotalHits totalHits = hitsMetadata.total();

		searchSearchResponse.setCount(totalHits.value());
	}

	private void _addAggregations(
		SearchResponse searchResponse,
		SearchSearchResponse searchSearchResponse,
		SearchSearchRequest searchSearchRequest) {

		Map<String, Aggregate> aggregates = searchResponse.aggregations();

		if (MapUtil.isEmpty(aggregates)) {
			return;
		}

		Map<String, Aggregation> aggregationsMap =
			searchSearchRequest.getAggregationsMap();

		Map<String, PipelineAggregation> pipelineAggregationsMap =
			searchSearchRequest.getPipelineAggregationsMap();

		ElasticsearchAggregationResultsTranslator
			elasticsearchAggregationResultsTranslator =
				new ElasticsearchAggregationResultsTranslator(
					aggregationsMap::get, this, pipelineAggregationsMap::get,
					this);

		List<AggregationResult> aggregationResults =
			elasticsearchAggregationResultsTranslator.translate(aggregates);

		for (AggregationResult aggregationResult : aggregationResults) {
			searchSearchResponse.addAggregationResult(aggregationResult);
		}
	}

	private void _setScrollId(
		SearchResponse<JsonData> searchResponse,
		SearchSearchResponse searchSearchResponse) {

		SetterUtil.setNotBlankString(
			searchSearchResponse::setScrollId, searchResponse.scrollId());
	}

	private void _setSearchHits(
		SearchResponse searchResponse,
		SearchSearchResponse searchSearchResponse,
		SearchSearchRequest searchSearchRequest) {

		HitsMetadataTranslator hitsMetadataTranslator =
			new HitsMetadataTranslator(
				_documentBuilderFactory, _geoBuilders,
				_highlightFieldBuilderFactory, _searchHitBuilderFactory,
				_searchHitsBuilderFactory);

		searchSearchResponse.setSearchHits(
			hitsMetadataTranslator.translate(
				searchSearchRequest.getAlternateUidFieldName(),
				searchResponse.hits()));
	}

	private void _setSearchTimeValue(
		SearchResponse searchResponse,
		SearchSearchResponse searchSearchResponse) {

		SearchTimeValue.Builder builder = SearchTimeValue.Builder.newBuilder();

		builder.duration(
			searchResponse.took()
		).timeUnit(
			TimeUnit.MILLISECOND
		);

		searchSearchResponse.setSearchTimeValue(builder.build());
	}

	@Reference
	private AggregationResults _aggregationResults;

	@Reference
	private CommonSearchResponseAssembler _commonSearchResponseAssembler;

	@Reference
	private DocumentBuilderFactory _documentBuilderFactory;

	@Reference
	private GeoBuilders _geoBuilders;

	@Reference
	private GroupByResponseFactory _groupByResponseFactory;

	@Reference
	private HighlightFieldBuilderFactory _highlightFieldBuilderFactory;

	@Reference
	private SearchHitBuilderFactory _searchHitBuilderFactory;

	@Reference
	private SearchHitDocumentTranslator _searchHitDocumentTranslator;

	@Reference
	private SearchHitsBuilderFactory _searchHitsBuilderFactory;

	private SearchResponseTranslator _searchResponseTranslator;

	@Reference
	private StatsRequestBuilderFactory _statsRequestBuilderFactory;

	@Reference
	private StatsResultsTranslator _statsResultsTranslator;

	@Reference
	private StatsTranslator _statsTranslator;

}