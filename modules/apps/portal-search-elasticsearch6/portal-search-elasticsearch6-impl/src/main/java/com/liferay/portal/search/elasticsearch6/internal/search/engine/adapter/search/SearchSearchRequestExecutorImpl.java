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

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;

import java.io.IOException;

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
@Component(immediate = true, service = SearchSearchRequestExecutor.class)
public class SearchSearchRequestExecutorImpl
	implements SearchSearchRequestExecutor {

	@Override
	public SearchSearchResponse execute(
		SearchSearchRequest searchSearchRequest) {

		SearchRequest searchRequest = new SearchRequest(
			searchSearchRequest.getIndexNames());

		if (searchSearchRequest.isRequestCache()) {
			searchRequest.requestCache(searchSearchRequest.isRequestCache());
		}

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSearchRequestAssembler.assemble(
			searchSourceBuilder, searchSearchRequest, searchRequest);

		SearchResponse searchResponse = getSearchResponse(searchRequest);

		SearchSearchResponse searchSearchResponse = new SearchSearchResponse();

		String searchSourceBuilderString = searchSourceBuilder.toString();

		searchSourceBuilderString = StringUtil.replace(
			searchSourceBuilderString, ZERO_TERMS_QUERY_STRING,
			StringPool.BLANK);

		searchSearchResponseAssembler.assemble(
			searchResponse, searchSearchResponse, searchSearchRequest,
			searchSourceBuilderString);

		return searchSearchResponse;
	}

	protected SearchResponse getSearchResponse(SearchRequest searchRequest) {
		RestHighLevelClient restHighLevelClient =
			elasticsearchConnectionManager.getRestHighLevelClient();

		try {
			return restHighLevelClient.search(
				searchRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	protected static final String ZERO_TERMS_QUERY_STRING =
		",\"zero_terms_query\":\"NONE\"";

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	@Reference
	protected SearchSearchRequestAssembler searchSearchRequestAssembler;

	@Reference
	protected SearchSearchResponseAssembler searchSearchResponseAssembler;

}