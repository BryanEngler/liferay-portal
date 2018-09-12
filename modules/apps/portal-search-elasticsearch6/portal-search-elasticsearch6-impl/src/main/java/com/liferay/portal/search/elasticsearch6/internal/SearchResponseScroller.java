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

package com.liferay.portal.search.elasticsearch6.internal;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch6.internal.index.IndexNameBuilder;
import com.liferay.portal.search.elasticsearch6.internal.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

/**
 * @author Michael C. Han
 */
public class SearchResponseScroller {

	public SearchResponseScroller(
		RestHighLevelClient restHighLevelClient, SearchContext searchContext,
		IndexNameBuilder indexNameBuilder, QueryBuilder queryBuilder,
		TimeValue scrollTimeValue, String... types) {

		_restHighLevelClient = restHighLevelClient;
		_searchContext = searchContext;
		_indexNameBuilder = indexNameBuilder;
		_queryBuilder = queryBuilder;
		_scrollTimeValue = scrollTimeValue;
		_types = types;
	}

	public boolean close() {
		try {
			ClearScrollRequest request = new ClearScrollRequest();

			request.setScrollIds(_previousScrollIds);

			ClearScrollResponse clearScrollResponse =
				_restHighLevelClient.clearScroll(
					request, RequestOptions.DEFAULT);

			LogUtil.logActionResponse(_log, clearScrollResponse);

			return clearScrollResponse.isSucceeded();
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}

			return false;
		}
	}

	public void prepare() throws Exception {
		SearchRequest searchRequest = new SearchRequest(
			_indexNameBuilder.getIndexName(_searchContext.getCompanyId()));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.sort(
			FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC);
		searchSourceBuilder.storedField(Field.UID);
		searchSourceBuilder.query(_queryBuilder);

		searchRequest.source(searchSourceBuilder);
		searchRequest.types(_types);

		Scroll scroll = new Scroll(_scrollTimeValue);

		searchRequest.scroll(scroll);

		SearchResponse searchResponse = _restHighLevelClient.search(
			searchRequest, RequestOptions.DEFAULT);

		_scrollId = searchResponse.getScrollId(); //process first set?

		LogUtil.logActionResponse(_log, searchResponse);
	}

	public boolean scroll(SearchHitsProcessor searchHitsProcessor)
		throws Exception {

		if (Validator.isNull(_scrollId)) { //while loop?
			return false;
		}

		SearchScrollRequest scrollRequest = new SearchScrollRequest(_scrollId);

		Scroll scroll = new Scroll(_scrollTimeValue);

		scrollRequest.scroll(scroll);

		SearchResponse searchResponse = _restHighLevelClient.scroll(
			scrollRequest, RequestOptions.DEFAULT);

		LogUtil.logActionResponse(_log, searchResponse);

		_previousScrollIds.add(_scrollId);

		SearchHits searchHits = searchResponse.getHits();

		SearchHit[] searchHitsArray = searchHits.getHits();

		if (ArrayUtil.isEmpty(searchHitsArray)) {
			_scrollId = null;

			return false;
		}

		searchHitsProcessor.processSearchHits(_searchContext, searchHits);

		_scrollId = searchResponse.getScrollId();

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SearchResponseScroller.class);

	private final RestHighLevelClient _restHighLevelClient;
	private final IndexNameBuilder _indexNameBuilder;
	private final List<String> _previousScrollIds = new ArrayList<>();
	private final QueryBuilder _queryBuilder;
	private String _scrollId;
	private final TimeValue _scrollTimeValue;
	private final SearchContext _searchContext;
	private final String[] _types;

}