package com.liferay.portal.search.web.components.results.list.portlet;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

import java.util.List;

public class SearchResultsListViewHelper {

	public SearchResultsListViewHelper(SearchResultsData searchResultsData) {
		_searchResultsData = searchResultsData;
	}

	public String[] getQueryTerms() {
		return _searchResultsData.getQueryTerms();
	}

	public SearchContainer<Document> getSearchResultsContainer() {
		List<Document> documents = _searchResultsData.getDocuments();

		SearchContainer<Document> searchContainer = new SearchContainer<>();

		searchContainer.setResults(documents);

		return searchContainer;
	}

	private final SearchResultsData _searchResultsData;

}