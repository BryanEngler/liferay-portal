package com.liferay.portal.search.web.internal.request.helper;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

public class PortletSharedSearchResultImpl implements PortletSharedSearchResult {

	public PortletSharedSearchResultImpl(
		SearchResultsData searchResultsData, SearchContext searchContext) {

		_searchResultsData = searchResultsData;
		_searchContext = searchContext;
	}

	@Override
	public Facet getFacet(String name) {
		return _searchContext.getFacet(name);
	}

	@Override
	public SearchResultsData getSearchResultsData() {
		return _searchResultsData;
	}

	private final SearchContext _searchContext;
	private final SearchResultsData _searchResultsData;

}