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

package com.liferay.portal.search.web.internal.results.search;

import com.liferay.portal.kernel.dao.search.DisplayTerms;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.internal.container.SearchContainerPortletURL;
import com.liferay.portal.search.web.internal.display.context.KeywordsSupplier;
import com.liferay.portal.search.web.internal.display.context.QueryConfigSupplier;
import com.liferay.portal.search.web.internal.display.context.Search;
import com.liferay.portal.search.web.internal.display.context.SearchContainerSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchContextSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchContributorsSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchResponse;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.request.helper.OriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResult;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResultImpl;
import com.liferay.portal.search.web.internal.request.params.SearchParameters;
import com.liferay.portal.search.web.internal.request.params.SearchParametersImpl;
import com.liferay.portal.search.web.internal.results.data.SearchResultsDataSupplier;
import com.liferay.portal.search.web.portlet.SearchParametersConfiguration;

import java.util.List;
import java.util.Optional;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;

/**
 * @author André de Oliveira
 */
public class SearchResultsDataSupplierImpl
	implements SearchResultsDataSupplier {

	public SearchResultsDataSupplierImpl(
		SearchParametersConfiguration searchParametersConfiguration,
		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier,
		RenderRequest renderRequest, ThemeDisplaySupplier themeDisplaySupplier,
		SearchContributorsSupplier searchContributorsSupplier,
		FacetedSearcherManager facetedSearcherManager) {

		_searchParametersConfiguration = searchParametersConfiguration;
		_originalHttpServletRequestSupplier =
			originalHttpServletRequestSupplier;
		_renderRequest = renderRequest;
		_themeDisplaySupplier = themeDisplaySupplier;
		_searchContributorsSupplier = searchContributorsSupplier;
		_facetedSearcherManager = facetedSearcherManager;
	}

	@Override
	public PortletSharedSearchResult get() {
		SearchParameters searchParameters = new SearchParametersImpl(
			_originalHttpServletRequestSupplier,
			_searchParametersConfiguration);

		Search search = createSearch(searchParameters);

		SearchResponse searchResponse = search.search();

		PortletSharedSearchResultImpl portletSharedSearchResultImpl =
			new PortletSharedSearchResultImpl();

		portletSharedSearchResultImpl.setSearchContext(
			searchResponse.getSearchContext());
		portletSharedSearchResultImpl.setSearchParametersConfiguration(
			_searchParametersConfiguration);
		portletSharedSearchResultImpl.setSearchResultsData(
			buildSearchResultsData(searchResponse));

		return portletSharedSearchResultImpl;
	}

	protected SearchResultsDataImpl buildSearchResultsData(
		SearchResponse searchResponse) {

		SearchResultsDataImpl searchResultsDataImpl =
			new SearchResultsDataImpl();

		Hits hits = searchResponse.getHits();
		SearchContainer<Document> searchContainer =
			searchResponse.getSearchContainer();
		SearchContext searchContext = searchResponse.getSearchContext();

		searchResultsDataImpl.setDocuments(hits.toList());
		searchResultsDataImpl.setFrom(searchContainer.getCur());
		searchResultsDataImpl.setHighlights(hits.getQueryTerms());
		searchResultsDataImpl.setKeywords(searchContext.getKeywords());
		searchResultsDataImpl.setQueryString(
			(String)searchContext.getAttribute("queryString"));
		searchResultsDataImpl.setTotalHits(hits.getLength());

		return searchResultsDataImpl;
	}

	protected Search createSearch(SearchParameters searchParameters) {
		KeywordsSupplier keywordsSupplier = () -> getKeywords(searchParameters);

		SearchContextSupplier searchContextSupplier =
			() -> createSearchContext();

		SearchContainerSupplier searchContainerSupplier =
			() -> createSearchContainer(searchParameters);

		QueryConfigSupplier queryConfigSupplier = () -> new QueryConfig();

		return new Search(
			keywordsSupplier, searchContextSupplier, searchContainerSupplier,
			queryConfigSupplier, _searchContributorsSupplier,
			_facetedSearcherManager);
	}

	protected SearchContainer<Document> createSearchContainer(
		SearchParameters searchParameters) {

		Optional<Integer> fromParameterValue =
			searchParameters.getFromParameterValue();

		PortletRequest portletRequest = _renderRequest;
		DisplayTerms displayTerms = null;
		DisplayTerms searchTerms = null;
		String curParam = _searchParametersConfiguration.getFromParameterName();
		int cur = fromParameterValue.orElse(0);
		int delta = SearchContainer.DEFAULT_DELTA;
		PortletURL iteratorURL = getIteratorURL();
		List<String> headerNames = null;
		String emptyResultsMessage = null;
		String cssClass = null;

		SearchContainer<Document> searchContainer = new SearchContainer<>(
			portletRequest, displayTerms, searchTerms, curParam, cur, delta,
			iteratorURL, headerNames, emptyResultsMessage, cssClass);

		return searchContainer;
	}

	protected SearchContext createSearchContext() {
		SearchContext searchContext = new SearchContext();

		ThemeDisplay themeDisplay = _themeDisplaySupplier.getThemeDisplay();

		searchContext.setCompanyId(themeDisplay.getCompanyId());
		searchContext.setLayout(themeDisplay.getLayout());
		searchContext.setLocale(themeDisplay.getLocale());
		searchContext.setTimeZone(themeDisplay.getTimeZone());
		searchContext.setUserId(themeDisplay.getUserId());

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setLocale(themeDisplay.getLocale());

		return searchContext;
	}

	protected PortletURL getIteratorURL() {
		String url = HttpUtil.getCompleteURL(
			_originalHttpServletRequestSupplier.get());

		return new SearchContainerPortletURL(url);
	}

	protected String getKeywords(SearchParameters searchParameters) {
		Optional<String> keywordsParameterValue =
			searchParameters.getKeywordsParameterValue();

		return keywordsParameterValue.orElse(StringPool.BLANK);
	}

	private final FacetedSearcherManager _facetedSearcherManager;
	private final OriginalHttpServletRequestSupplier
		_originalHttpServletRequestSupplier;
	private final RenderRequest _renderRequest;
	private final SearchContributorsSupplier _searchContributorsSupplier;
	private final SearchParametersConfiguration _searchParametersConfiguration;
	private final ThemeDisplaySupplier _themeDisplaySupplier;

}