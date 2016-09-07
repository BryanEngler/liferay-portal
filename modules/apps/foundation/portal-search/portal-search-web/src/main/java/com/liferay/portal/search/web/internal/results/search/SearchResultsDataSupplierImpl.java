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

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.internal.display.context.FacetsConfigurationSupplier;
import com.liferay.portal.search.web.internal.display.context.KeywordsSupplier;
import com.liferay.portal.search.web.internal.display.context.PortalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.display.context.PortletURLFactory;
import com.liferay.portal.search.web.internal.display.context.QueryConfigSupplier;
import com.liferay.portal.search.web.internal.display.context.Search;
import com.liferay.portal.search.web.internal.display.context.SearchContainerSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchContextSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchContributorsSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchResponse;
import com.liferay.portal.search.web.internal.display.context.SearchScopeGroupIdSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchContainerSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResult;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResultImpl;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;
import com.liferay.portal.search.web.internal.results.data.SearchResultsDataSupplier;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author André de Oliveira
 */
public class SearchResultsDataSupplierImpl
	implements SearchResultsDataSupplier {

	public SearchResultsDataSupplierImpl(
		KeywordsSupplier keywordsSupplier, PortletURLFactory portletURLFactory,
		PortalHttpServletRequestSupplier requestSupplier,
		RenderRequest renderRequest,
		SearchContributorsSupplier searchContributorsSupplier,
		FacetedSearcherManager facetedSearcherManager, Language language) {

		_keywordsSupplier = keywordsSupplier;
		_portletURLFactory = portletURLFactory;
		_requestSupplier = requestSupplier;
		_renderRequest = renderRequest;
		_searchContributorsSupplier = searchContributorsSupplier;
		_facetedSearcherManager = facetedSearcherManager;
		_language = language;
	}

	@Override
	public PortletSharedSearchResult get() {
		Search search = createSearch();

		SearchResponse searchResponse = search.search();

		SearchContainer<Document> searchContainer =
			searchResponse.getSearchContainer();

		Hits hits = searchResponse.getHits();

		SearchResultsData searchResultsData = new SearchResultsDataImpl(
			searchContainer.getResults(), hits.getQueryTerms());

		return new PortletSharedSearchResultImpl(
			searchResultsData, searchResponse.getSearchContext());
	}

	protected Search createSearch() {
		PortletPreferences portletPreferences = null;

		SearchScopeGroupIdSupplier searchScopeGroupIdSupplier = () -> 0;

		FacetsConfigurationSupplier facetsConfigurationSupplier =
			() ->
				"{\"facets\":[{\"fieldName\":\"assetCategoryIds\"," +
				"\"static\":false,\"data\":{\"displayStyle\":\"list\"," +
				"\"maxTerms\":10,\"showAssetCount\":true," +
				"\"frequencyThreshold\":1},\"weight\":1.3," +
				"\"className\":" +
				"\"com.liferay.portal.kernel.search.facet.MultiValueFacet\"," +
				"\"id\":" +
				"\"com.liferay.portal.search.web.internal.facet" +
				".AssetCategoriesSearchFacet\",\"label\":\"any-category\"," +
				"\"order\":\"OrderHitsDesc\"},{\"fieldName\":" +
				"\"entryClassName\",\"static\":false,\"data\":{" +
				"\"frequencyThreshold\":1,\"values\":[" +
				"\"com.liferay.wiki.model.WikiPage\"," +
				"\"com.liferay.document.library.kernel.model.DLFileEntry\"," +
				"\"com.liferay.portal.kernel.model.User\"," +
				"\"com.liferay.bookmarks.model.BookmarksFolder\"," +
				"\"com.liferay.blogs.kernel.model.BlogsEntry\"," +
				"\"com.liferay.dynamic.data.lists.model.DDLFormRecord\"," +
				"\"com.liferay.document.library.kernel.model.DLFolder\"," +
				"\"com.liferay.dynamic.data.lists.model.DDLRecord\"," +
				"\"com.liferay.bookmarks.model.BookmarksEntry\"," +
				"\"com.liferay.journal.model.JournalArticle\"," +
				"\"com.liferay.journal.model.JournalFolder\"," +
				"\"com.liferay.message.boards.kernel.model.MBMessage\"," +
				"\"com.liferay.calendar.model.CalendarBooking\"," +
				"\"com.liferay.knowledge.base.model.KBArticle\"]}," +
				"\"weight\":1.5,\"className\":" +
				"\"com.liferay.portal.kernel.search.facet.AssetEntriesFacet\"," +
				"\"id\":" +
				"\"com.liferay.portal.search.web.internal.facet" +
				".AssetEntriesSearchFacet\",\"label\":\"any-asset\"," +
				"\"order\":\"OrderHitsDesc\"},{\"fieldName\":\"assetTagNames\"," +
				"\"static\":false,\"data\":{\"displayStyle\":\"list\",\"maxTerms\":10,\"showAssetCount\":true,\"frequencyThreshold\":1},\"weight\":1.4,\"className\":\"com.liferay.portal.kernel.search.facet.MultiValueFacet\",\"id\":\"com.liferay.portal.search.web.internal.facet.AssetTagsSearchFacet\",\"label\":\"any-tag\",\"order\":\"OrderHitsDesc\"},{\"fieldName\":\"folderId\",\"static\":false,\"data\":{\"maxTerms\":10,\"showAssetCount\":true,\"frequencyThreshold\":1},\"weight\":1.2,\"className\":\"com.liferay.portal.kernel.search.facet.MultiValueFacet\",\"id\":\"com.liferay.portal.search.web.internal.facet.FolderSearchFacet\",\"label\":\"any-folder\",\"order\":\"OrderHitsDesc\"},{\"fieldName\":\"geoLocation\",\"static\":false,\"data\":{\"GEOLOCATION-FIELD-NAME\":\"geoLocation\",\"ranges\":[{\"range\":\"[0 TO 10]\",\"label\":\"0m-to-10m\"},{\"range\":\"[10 TO 500]\",\"label\":\"10m-to-500m\"},{\"range\":\"[500 TO 1000]\",\"label\":\"500m-to-1km\"},{\"range\":\"[1000 TO 2000]\",\"label\":\"1km-to-2km\"},{\"range\":\"[2000 TO 5000]\",\"label\":\"2km-to-5km\"},{\"range\":\"[5000 TO 10000]\",\"label\":\"5km-to-10km\"},{\"range\":\"[10000 TO 20000]\",\"label\":\"10km-to-20km\"}],\"frequencyThreshold\":1,\"GEODISTANCE-CENTER-POINT\":\"42.3594,-71.0587\"},\"weight\":1,\"className\":\"com.liferay.portal.search.facet.internal.geolocation.GeoDistanceFacet\",\"id\":\"com.liferay.portal.search.web.internal.facet.GeoDistanceSearchFacet\",\"label\":\"any-distance\",\"order\":\"OrderHitsDesc\"},{\"fieldName\":\"modified\",\"static\":false,\"data\":{\"ranges\":[{\"range\":\"[past-hour TO *]\",\"label\":\"past-hour\"},{\"range\":\"[past-24-hours TO *]\",\"label\":\"past-24-hours\"},{\"range\":\"[past-week TO *]\",\"label\":\"past-week\"},{\"range\":\"[past-month TO *]\",\"label\":\"past-month\"},{\"range\":\"[past-year TO *]\",\"label\":\"past-year\"}],\"frequencyThreshold\":1},\"weight\":1,\"className\":\"com.liferay.portal.kernel.search.facet.ModifiedFacet\",\"id\":\"com.liferay.portal.search.web.internal.facet.ModifiedSearchFacet\",\"label\":\"any-time\",\"order\":\"OrderHitsDesc\"},{\"fieldName\":\"groupId\",\"static\":false,\"data\":{\"maxTerms\":10,\"showAssetCount\":true,\"frequencyThreshold\":1},\"weight\":1.6,\"className\":\"com.liferay.portal.kernel.search.facet.ScopeFacet\",\"id\":\"com.liferay.portal.search.web.internal.facet.ScopeSearchFacet\",\"label\":\"any-site\",\"order\":\"OrderHitsDesc\"},{\"fieldName\":\"userName\",\"static\":false,\"data\":{\"maxTerms\":10,\"showAssetCount\":true,\"frequencyThreshold\":1},\"weight\":1.1,\"className\":\"com.liferay.portal.kernel.search.facet.MultiValueFacet\",\"id\":\"com.liferay.portal.search.web.internal.facet.UserSearchFacet\",\"label\":\"any-user\",\"order\":\"OrderHitsDesc\"}]}";

		QueryConfigSupplier queryConfigSupplier = () -> new QueryConfig();


		SearchContextSupplier searchContextSupplier =
						() -> createSearchContext();

		SearchContainerSupplier searchContainerSupplier =
			new SearchPortletSearchContainerSupplier(
				_renderRequest, _language, _requestSupplier, HtmlUtil.getHtml(),
				_keywordsSupplier, _portletURLFactory);

		return new Search(
			_keywordsSupplier,
			searchContextSupplier,
			searchContainerSupplier,
			queryConfigSupplier,
			_searchContributorsSupplier,
			_facetedSearcherManager);
	}

	protected SearchContext createSearchContext() {
		HttpServletRequest request = _requestSupplier.get();

		SearchContext searchContext = new SearchContext();

		// Theme display

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		searchContext.setCompanyId(themeDisplay.getCompanyId());
		searchContext.setLayout(themeDisplay.getLayout());
		searchContext.setLocale(themeDisplay.getLocale());
		searchContext.setTimeZone(themeDisplay.getTimeZone());
		searchContext.setUserId(themeDisplay.getUserId());

		// Query config

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setLocale(themeDisplay.getLocale());

		return searchContext;
	}

	private final FacetedSearcherManager _facetedSearcherManager;
	private final KeywordsSupplier _keywordsSupplier;
	private final Language _language;
	private final PortletURLFactory _portletURLFactory;
	private final RenderRequest _renderRequest;
	private final PortalHttpServletRequestSupplier
		_requestSupplier;
	private final SearchContributorsSupplier _searchContributorsSupplier;

}