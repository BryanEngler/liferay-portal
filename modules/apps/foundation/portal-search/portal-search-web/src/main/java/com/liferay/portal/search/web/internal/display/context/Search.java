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
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.HitsImpl;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacet;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.ScopeFacet;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.web.search.builder.SearchBuilder;

import java.util.Collection;

/**
 * @author André de Oliveira
 */
public class Search {

	public Search(
		KeywordsSupplier keywordsSupplier,
		SearchContextSupplier searchContextSupplier,
		SearchContainerSupplier searchContainerSupplier,
		QueryConfigSupplier queryConfigSupplier,
		SearchContributorsSupplier searchContributorsSupplier,
		FacetedSearcherManager facetedSearcherManager) {

		_keywordsSupplier = keywordsSupplier;
		_searchContextSupplier = searchContextSupplier;
		_searchContainerSupplier = searchContainerSupplier;
		_queryConfigSupplier = queryConfigSupplier;
		_searchContributorsSupplier = searchContributorsSupplier;
		_facetedSearcherManager = facetedSearcherManager;
	}

	public SearchResponse search() {
		String keywords = StringUtil.trim(_keywordsSupplier.getKeywords());

		SearchContainer<Document> searchContainer =
			_searchContainerSupplier.getSearchContainer();

		SearchContext searchContext = buildSearchContext(
			keywords, searchContainer.getStart(), searchContainer.getEnd());

		Hits hits = search(keywords, searchContext);

		searchContainer.setTotal(hits.getLength());
		searchContainer.setResults(hits.toList());

		searchContainer.setSearch(true);

		return new SearchResponse(hits, searchContext, searchContainer);
	}

	protected void addAssetEntriesFacet(SearchContext searchContext) {
		Facet assetEntriesFacet = new AssetEntriesFacet(searchContext);

		assetEntriesFacet.setStatic(true);

		searchContext.addFacet(assetEntriesFacet);
	}

	protected void addScopeFacet(SearchContext searchContext) {
		Facet scopeFacet = new ScopeFacet(searchContext);

		scopeFacet.setStatic(true);

		searchContext.addFacet(scopeFacet);
	}

	protected SearchContext buildSearchContext(
		String keywords, int start, int end) {

		SearchContext searchContext = _searchContextSupplier.getSearchContext();

		searchContext.setAttribute("paginationType", "more");
		searchContext.setEnd(end);
		searchContext.setKeywords(keywords);
		searchContext.setQueryConfig(_queryConfigSupplier.getQueryConfig());
		searchContext.setStart(start);

		SearchBuilder searchBuilder = new SearchBuilder() {

			@Override
			public void addFacet(Facet facet) {
				searchContext.addFacet(facet);
			}

		};

		addAssetEntriesFacet(searchContext);

		addScopeFacet(searchContext);

		Collection<SearchContributor> searchContributors =
			_searchContributorsSupplier.getSearchContributors();

		searchContributors.forEach(
			searchContributor -> searchContributor.contribute(
				searchBuilder, searchContext));

		return searchContext;
	}

	protected Hits search(
		FacetedSearcher facetedSearcher, SearchContext searchContext) {

		try {
			return facetedSearcher.search(searchContext);
		}
		catch (SearchException se) {
			throw new RuntimeException(se);
		}
	}

	protected Hits search(String keywords, SearchContext searchContext) {
		if (Validator.isBlank(keywords)) {
			return new HitsImpl();
		}

		FacetedSearcher facetedSearcher =
			_facetedSearcherManager.createFacetedSearcher();

		return search(facetedSearcher, searchContext);
	}

	private final FacetedSearcherManager _facetedSearcherManager;
	private final KeywordsSupplier _keywordsSupplier;
	private final QueryConfigSupplier _queryConfigSupplier;
	private final SearchContainerSupplier _searchContainerSupplier;
	private final SearchContextSupplier _searchContextSupplier;
	private final SearchContributorsSupplier _searchContributorsSupplier;

}