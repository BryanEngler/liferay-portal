package com.liferay.portal.search.web.internal.display.context;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;

public class SearchResponse {

	public SearchResponse(
		Hits hits, SearchContext searchContext,
		SearchContainer<Document> searchContainer) {

		_hits = hits;
		_searchContext = searchContext;
		_searchContainer = searchContainer;
	}

	public Hits getHits() {
		return _hits;
	}

	public SearchContext getSearchContext() {
		return _searchContext;
	}

	public SearchContainer<Document> getSearchContainer() {
		return _searchContainer;
	}

	private final SearchContainer<Document> _searchContainer;
	private final SearchContext _searchContext;
	private final Hits _hits;

}