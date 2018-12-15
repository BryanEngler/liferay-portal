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

package com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter.search;

import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.adapter.search.MultisearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.MultisearchSearchResponse;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, service = MultisearchSearchRequestExecutor.class)
public class MultisearchSearchRequestExecutorImpl
	implements MultisearchSearchRequestExecutor {

	@Override
	public MultisearchSearchResponse execute(
		MultisearchSearchRequest multisearchSearchRequest) {

		MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

		List<SearchSearchRequest> searchSearchRequests =
			multisearchSearchRequest.getSearchSearchRequests();

		List<SearchRequestHolder> searchRequestHolders = new ArrayList<>(
			searchSearchRequests.size());

		searchSearchRequests.forEach(
			searchSearchRequest -> {
				SearchRequest searchRequest = new SearchRequest(
					searchSearchRequest.getIndexNames());

				SearchSourceBuilder searchSourceBuilder =
					new SearchSourceBuilder();

				searchSearchRequestAssembler.assemble(
					searchSourceBuilder, searchSearchRequest, searchRequest);

				SearchRequestHolder searchRequestHolder =
					new SearchRequestHolder(
						searchSearchRequest, searchSourceBuilder.toString());

				searchRequestHolders.add(searchRequestHolder);

				multiSearchRequest.add(searchRequest);
			});

		MultiSearchResponse multiSearchResponse = getMultiSearchResponse(
			multiSearchRequest);

		Iterator<MultiSearchResponse.Item> multiSearchResponseItems =
			multiSearchResponse.iterator();

		MultisearchSearchResponse multisearchSearchResponse =
			new MultisearchSearchResponse();

		int counter = 0;

		while (multiSearchResponseItems.hasNext()) {
			MultiSearchResponse.Item multiSearchResponseItem =
				multiSearchResponseItems.next();

			SearchResponse searchResponse =
				multiSearchResponseItem.getResponse();

			SearchSearchResponse searchSearchResponse =
				new SearchSearchResponse();

			SearchRequestHolder searchRequestHolder = searchRequestHolders.get(
				counter);

			searchSearchResponseAssembler.assemble(
				searchResponse, searchSearchResponse,
				searchRequestHolder.getSearchSearchRequest(),
				searchRequestHolder.getSearchRequestBuilderString());

			counter++;
		}

		return multisearchSearchResponse;
	}

	protected MultiSearchResponse getMultiSearchResponse(
		MultiSearchRequest multiSearchRequest) {

		RestHighLevelClient restHighLevelClient =
			elasticsearchConnectionManager.getRestHighLevelClient();

		try {
			return restHighLevelClient.msearch(
				multiSearchRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	@Reference
	protected SearchSearchRequestAssembler searchSearchRequestAssembler;

	@Reference
	protected SearchSearchResponseAssembler searchSearchResponseAssembler;

	private class SearchRequestHolder {

		public SearchRequestHolder(
			SearchSearchRequest searchSearchRequest,
			String searchRequestBuilderString) {

			_searchSearchRequest = searchSearchRequest;
			_searchRequestBuilderString = searchRequestBuilderString;
		}

		public String getSearchRequestBuilderString() {
			return _searchRequestBuilderString;
		}

		public SearchSearchRequest getSearchSearchRequest() {
			return _searchSearchRequest;
		}

		private final String _searchRequestBuilderString;
		private final SearchSearchRequest _searchSearchRequest;

	}

}