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

package com.liferay.portal.search.web.internal.display.context;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Html;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.liferay.portal.search.web.internal.portlet.SearchPorletQueryConfigPreferences;
import com.liferay.portal.search.web.internal.portlet.SearchPortletFacetsConfigurationSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletFacetsDisplayPreferences;
import com.liferay.portal.search.web.internal.portlet.SearchPortletFacetsSearchContributor;
import com.liferay.portal.search.web.internal.portlet.SearchPortletHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletKeywordsSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletQueryConfigSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchContainerSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchContextSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchFacetsSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchResultPreferences;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchScopeGroupIdSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchScopePreferenceStringSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchScopePreferenceSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchScopeSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;

/**
 * @author Eudaldo Alonso
 */
public class SearchDisplayContext {

	public SearchDisplayContext(
			RenderRequest renderRequest, PortletPreferences portletPreferences,
			Portal portal, Html html, Language language,
			FacetedSearcherManager facetedSearcherManager,
			IndexSearchPropsValues indexSearchPropsValues,
			PortletURLFactory portletURLFactory)
		throws Exception {

		FacetsDisplayPreferences facetsDisplayPreferences =
			new SearchPortletFacetsDisplayPreferences(portletPreferences);

		SearchFacetsSupplier searchFacetsSupplier =
			new SearchPortletSearchFacetsSupplier(facetsDisplayPreferences);

		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		PortalHttpServletRequestSupplier requestSupplier =
			new SearchPortletHttpServletRequestSupplier(portal, renderRequest);

		QueryConfigPreferences queryConfigPreferences =
			new SearchPorletQueryConfigPreferences(
				portletPreferences, indexSearchPropsValues);

		QueryConfigSupplier queryConfigSupplier =
			new SearchPortletQueryConfigSupplier(queryConfigPreferences);

		FacetsConfigurationSupplier facetsConfigurationSupplier =
			new SearchPortletFacetsConfigurationSupplier(portletPreferences);

		SearchScopePreferenceStringSupplier searchScopePreferenceStringSupplier =
			new SearchPortletSearchScopePreferenceStringSupplier(
				portletPreferences);

		SearchScopePreferenceSupplier searchScopePreferenceSupplier =
			new SearchPortletSearchScopePreferenceSupplier(
				searchScopePreferenceStringSupplier);

		SearchScopeSupplier searchScopeSupplier =
			new SearchPortletSearchScopeSupplier(
				renderRequest, searchScopePreferenceSupplier);

		SearchScopeGroupIdSupplier searchScopeGroupIdSupplier =
			new SearchPortletSearchScopeGroupIdSupplier(
			searchScopeSupplier, themeDisplaySupplier);

		KeywordsSupplier keywordsSupplier = new SearchPortletKeywordsSupplier(
			renderRequest);

		SearchResultPreferences searchResultPreferences =
			new SearchPortletSearchResultPreferences(
				portletPreferences, themeDisplaySupplier);

		_searchResultPreferences = searchResultPreferences;
		_portletPreferences = portletPreferences;
		_keywordsSupplier = keywordsSupplier;
		_searchScopeSupplier = searchScopeSupplier;
		_searchScopeGroupIdSupplier = searchScopeGroupIdSupplier;
		_searchScopePreferenceSupplier = searchScopePreferenceSupplier;
		_searchScopePreferenceStringSupplier =
			searchScopePreferenceStringSupplier;
		_searchFacetsSupplier = searchFacetsSupplier;
		_facetsConfigurationSupplier = facetsConfigurationSupplier;
		_facetsDisplayPreferences = facetsDisplayPreferences;
		_queryConfigSupplier = queryConfigSupplier;
		_queryConfigPreferences = queryConfigPreferences;
		_themeDisplaySupplier = themeDisplaySupplier;
		_portletURLFactory = portletURLFactory;

		String keywords = StringUtil.trim(keywordsSupplier.getKeywords());

		if (keywords == null) {
			_hits = null;
			_searchContext = null;
			_searchContainer = null;

			return;
		}

		SearchContextSupplier searchContextSupplier =
			new SearchPortletSearchContextSupplier(requestSupplier);

		SearchPortletSearchContainerSupplier searchContainerSupplier =
			new SearchPortletSearchContainerSupplier(
				renderRequest, language, requestSupplier, html,
				keywordsSupplier, portletURLFactory);

		SearchContributor searchContributor =
			new SearchPortletFacetsSearchContributor(
				searchFacetsSupplier, facetsConfigurationSupplier,
				themeDisplaySupplier);

		SearchContributorsSupplier searchContributorsSupplier =
			() -> Collections.singleton(searchContributor);

		Search search = new Search(
			keywordsSupplier, searchContextSupplier, searchContainerSupplier,
			queryConfigSupplier, searchContributorsSupplier,
			facetedSearcherManager);

		SearchResponse searchResponse = search.search();

		_hits = searchResponse.getHits();
		_searchContext = searchResponse.getSearchContext();
		_searchContainer = searchResponse.getSearchContainer();
	}

	public int getCollatedSpellCheckResultDisplayThreshold() {
		return _queryConfigPreferences.getCollatedSpellCheckResultDisplayThreshold();
	}

	public List<SearchFacet> getEnabledSearchFacets() {
		return new ArrayList<>(_searchFacetsSupplier.getSearchFacets());
	}

	public Hits getHits() {
		return _hits;
	}

	public String getKeywords() {
		return _keywordsSupplier.getKeywords();
	}

	public PortletURL getPortletURL() throws PortletException {
		return _portletURLFactory.getPortletURL();
	}

	public PortletURLFactory getPortletURLFactory() {
		return _portletURLFactory;
	}

	public QueryConfig getQueryConfig() {
		return _queryConfigSupplier.getQueryConfig();
	}

	public int getQueryIndexingThreshold() {
		return _queryConfigPreferences.getQueryIndexingThreshold();
	}

	public int getQuerySuggestionsDisplayThreshold() {
		return _queryConfigPreferences.getQuerySuggestionsDisplayThreshold();
	}

	public int getQuerySuggestionsMax() {
		return _queryConfigPreferences.getQuerySuggestionsMax();
	}

	public String[] getQueryTerms() {
		Hits hits = getHits();

		return hits.getQueryTerms();
	}

	public String getSearchConfiguration() {
		return _facetsConfigurationSupplier.getFacetsConfiguration();
	}

	public SearchContainer<Document> getSearchContainer() {
		return _searchContainer;
	}

	public SearchContext getSearchContext() {
		return _searchContext;
	}

	public SearchResultPreferences getSearchResultPreferences() {
		return _searchResultPreferences;
	}

	public long getSearchScopeGroupId() {
		return _searchScopeGroupIdSupplier.getSearchScopeGroupId();
	}

	public String getSearchScopeParameterString() {
		SearchScope searchScope = getSearchScope();

		return searchScope.getParameterString();
	}

	public String getSearchScopePreferenceString() {
		return _searchScopePreferenceStringSupplier.getSearchScopePreferenceString();
	}

	public boolean isCollatedSpellCheckResultEnabled() {
		return _queryConfigPreferences.isCollatedSpellCheckResultEnabled();
	}

	public boolean isDisplayFacet(SearchFacet searchFacet) {
		return _facetsDisplayPreferences.isDisplay(searchFacet);
	}

	public boolean isDisplayMainQuery() {
		if (_displayMainQuery != null) {
			return _displayMainQuery;
		}

		_displayMainQuery = GetterUtil.getBoolean(
			_portletPreferences.getValue("displayMainQuery", null));

		return _displayMainQuery;
	}

	public boolean isDisplayOpenSearchResults() {
		if (_displayOpenSearchResults != null) {
			return _displayOpenSearchResults;
		}

		_displayOpenSearchResults = GetterUtil.getBoolean(
			_portletPreferences.getValue("displayOpenSearchResults", null));

		return _displayOpenSearchResults;
	}

	public boolean isDisplayResultsInDocumentForm() {
		return _searchResultPreferences.isDisplayResultsInDocumentForm();
	}

	public boolean isDLLinkToViewURL() {
		if (_dlLinkToViewURL != null) {
			return _dlLinkToViewURL;
		}

		_dlLinkToViewURL = false;

		return _dlLinkToViewURL;
	}

	public boolean isHighlightEnabled() {
		QueryConfig queryConfig = getQueryConfig();

		return queryConfig.isHighlightEnabled();
	}

	public boolean isIncludeSystemPortlets() {
		if (_includeSystemPortlets != null) {
			return _includeSystemPortlets;
		}

		_includeSystemPortlets = false;

		return _includeSystemPortlets;
	}

	public boolean isQueryIndexingEnabled() {
		return _queryConfigPreferences.isQueryIndexingEnabled();
	}

	public boolean isQuerySuggestionsEnabled() {
		return _queryConfigPreferences.isQuerySuggestionsEnabled();
	}

	public boolean isSearchScopePreferenceEverythingAvailable() {
		ThemeDisplay themeDisplay = getThemeDisplay();

		Group group = themeDisplay.getScopeGroup();

		if (group.isStagingGroup()) {
			return false;
		}

		return true;
	}

	public boolean isSearchScopePreferenceLetTheUserChoose() {
		SearchScopePreference searchScopePreference =
			getSearchScopePreference();

		if (searchScopePreference ==
				SearchScopePreference.LET_THE_USER_CHOOSE) {

			return true;
		}

		return false;
	}

	public boolean isShowMenu() {
		for (SearchFacet searchFacet : SearchFacetTracker.getSearchFacets()) {
			if (_facetsDisplayPreferences.isDisplay(searchFacet)) {
				return true;
			}
		}

		return false;
	}

	public boolean isViewInContext() {
		return _searchResultPreferences.isViewInContext();
	}

	protected SearchScope getSearchScope() {
		return _searchScopeSupplier.getSearchScope();
	}

	protected SearchScopePreference getSearchScopePreference() {
		return _searchScopePreferenceSupplier.getSearchScopePreference();
	}

	protected ThemeDisplay getThemeDisplay() {
		return _themeDisplaySupplier.getThemeDisplay();
	}

	private Boolean _displayMainQuery;
	private Boolean _displayOpenSearchResults;
	private Boolean _dlLinkToViewURL;
	private final FacetsConfigurationSupplier _facetsConfigurationSupplier;
	private final FacetsDisplayPreferences _facetsDisplayPreferences;
	private final Hits _hits;
	private Boolean _includeSystemPortlets;
	private final KeywordsSupplier _keywordsSupplier;
	private final PortletPreferences _portletPreferences;
	private final PortletURLFactory _portletURLFactory;
	private final QueryConfigPreferences _queryConfigPreferences;
	private final QueryConfigSupplier _queryConfigSupplier;
	private final SearchContainer<Document> _searchContainer;
	private final SearchContext _searchContext;
	private final SearchFacetsSupplier _searchFacetsSupplier;
	private final SearchResultPreferences _searchResultPreferences;
	private final SearchScopeGroupIdSupplier _searchScopeGroupIdSupplier;
	private final SearchScopePreferenceStringSupplier
		_searchScopePreferenceStringSupplier;
	private final SearchScopePreferenceSupplier _searchScopePreferenceSupplier;
	private final SearchScopeSupplier _searchScopeSupplier;
	private final ThemeDisplaySupplier _themeDisplaySupplier;

}