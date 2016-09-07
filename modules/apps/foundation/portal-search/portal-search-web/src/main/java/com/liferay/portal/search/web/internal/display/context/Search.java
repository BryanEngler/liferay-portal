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
import com.liferay.portal.search.web.search.builder.SearchBuilder;

import java.util.Collection;

public class Search {

	private final SearchContainerSupplier _searchContainerSupplier;
	private final SearchContextSupplier searchContextSupplier;
	private final QueryConfigSupplier queryConfigSupplier;
	private final SearchContributorsSupplier searchContributorsSupplier;
	private final FacetedSearcherManager facetedSearcherManager;

	public Search(
		KeywordsSupplier keywordsSupplier,
		SearchContextSupplier searchContextSupplier,
		SearchContainerSupplier searchContainerSupplier,
		QueryConfigSupplier queryConfigSupplier,
		SearchContributorsSupplier searchContributorsSupplier,
		FacetedSearcherManager facetedSearcherManager

					) {
						this.keywordsSupplier = keywordsSupplier;
						this.searchContextSupplier = searchContextSupplier;
						_searchContainerSupplier = searchContainerSupplier;
						this.queryConfigSupplier = queryConfigSupplier;
						this.searchContributorsSupplier = searchContributorsSupplier;
						this.facetedSearcherManager = facetedSearcherManager;

	}

	public SearchResponse search() {
		String keywords = StringUtil.trim(keywordsSupplier.getKeywords());

		SearchContainer<Document> searchContainer =
			_searchContainerSupplier.getSearchContainer();

		SearchContext searchContext = buildSearchContext(
			keywords, searchContainer.getStart(), searchContainer.getEnd());

		Hits hits;

		if (keywords.isEmpty()) {
			hits = new HitsImpl();
		}
		else {
			FacetedSearcher facetedSearcher =
				facetedSearcherManager.createFacetedSearcher();

			hits = search(facetedSearcher, searchContext);

			searchContainer.setTotal(hits.getLength());
			searchContainer.setResults(hits.toList());

			searchContainer.setSearch(true);
		}

		return new SearchResponse(hits, searchContext, searchContainer);
	}

	protected SearchContext buildSearchContext(
		String keywords, int start, int end) {

		SearchContext searchContext = searchContextSupplier.getSearchContext();

		searchContext.setAttribute("paginationType", "more");
		searchContext.setEnd(end);
		searchContext.setKeywords(keywords);
		searchContext.setQueryConfig(queryConfigSupplier.getQueryConfig());
		searchContext.setStart(start);

		class SearchDisplayContextSearchBuilder implements SearchBuilder {

			@Override
			public void addFacet(Facet facet) {
				searchContext.addFacet(facet);
			}

		}

		SearchBuilder searchBuilder = new SearchDisplayContextSearchBuilder();

		addAssetEntriesFacet(searchContext);

		addScopeFacet(searchContext);

		Collection<SearchContributor> searchContributors =
			searchContributorsSupplier.getSearchContributors();

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

	private final KeywordsSupplier keywordsSupplier;

}