package com.liferay.portal.search.elasticsearch7.internal.information;

import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.DocumentCounter;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.query.Queries;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;

@Component(immediate = true, service = DocumentCounter.class)
public class DocumentCounterImpl implements DocumentCounter {

	@Override
	public long getDocumentCount(long companyId, String field) {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.addAggregation(
			_aggregations.terms(field, "companyId"));
		searchSearchRequest.setIndexNames(_indexNameBuilder.getIndexName(companyId));
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(_queries.exists(field));

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		Map<String, AggregationResult> aggregationResultsMap =
			searchSearchResponse.getAggregationResultsMap();

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResultsMap.get(field);

		Bucket bucket = termsAggregationResult.getBucket(
			String.valueOf(companyId));

		if (bucket != null) {
			return bucket.getDocCount();
		}

		return -1;
	}

	@Reference
	private Aggregations _aggregations;

	@Reference
	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}
