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

package com.liferay.portal.search.ranking.web.internal.request;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.search.constants.SearchContextAttributes;
import com.liferay.portal.search.legacy.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.ranking.web.internal.display.context.ResultsRankingEntryDisplayContext;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Wade Cao
 */
public class SearchRankingRequest {

	public SearchRankingRequest(
		HttpServletRequest httpServletRequest, Queries queries,
		SearchContainer<ResultsRankingEntryDisplayContext> searchContainer,
		Searcher searcher,
		SearchRequestBuilderFactory searchRequestBuilderFactory,
		ThemeDisplay themeDisplay) {

		_queries = queries;
		_searchContext = SearchContextFactory.getInstance(httpServletRequest);
		_searchContainer = searchContainer;
		_searcher = searcher;
		_searchRequestBuilderFactory = searchRequestBuilderFactory;
		_themeDisplay = themeDisplay;
	}

	public SearchRankingResponse search() {
		SearchContext searchContext = buildSearchContext();

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.getSearchRequestBuilder(searchContext);

		searchRequestBuilder.query(_queries.matchAll());

		SearchResponse searchResponse = _searcher.search(
			searchRequestBuilder.build());

		SearchRankingResponse searchRankingResponse =
			new SearchRankingResponse();

		searchRankingResponse.setSearchHits(searchResponse.getSearchHits());

		return searchRankingResponse;
	}

	protected SearchContext buildSearchContext() {
		User user = _themeDisplay.getUser();

		long groupId = user.getGroupId();

		_searchContext.setAttribute(Field.GROUP_ID, groupId);

		_searchContext.setAttribute("filterExpired", Boolean.TRUE);
		_searchContext.setAttribute(
			SearchContextAttributes.ATTRIBUTE_KEY_EMPTY_SEARCH, Boolean.TRUE);
		_searchContext.setAttribute("paginationType", "more");
		_searchContext.setCompanyId(_themeDisplay.getCompanyId());
		_searchContext.setLayout(_themeDisplay.getLayout());
		_searchContext.setLocale(_themeDisplay.getLocale());
		_searchContext.setTimeZone(_themeDisplay.getTimeZone());
		_searchContext.setUserId(_themeDisplay.getUserId());

		//_searchContext.setEnd(_searchContainer.getEnd());
		//_searchContext.setStart(_searchContainer.getStart());

		QueryConfig queryConfig = _searchContext.getQueryConfig();

		queryConfig.setCollatedSpellCheckResultEnabled(false);
		queryConfig.setLocale(_themeDisplay.getLocale());
		queryConfig.setSelectedIndexNames("results-ranking");

		return _searchContext;
	}

	private final Queries _queries;
	private final SearchContainer<ResultsRankingEntryDisplayContext>
		_searchContainer;
	private final SearchContext _searchContext;
	private final Searcher _searcher;
	private final SearchRequestBuilderFactory _searchRequestBuilderFactory;
	private final ThemeDisplay _themeDisplay;

}