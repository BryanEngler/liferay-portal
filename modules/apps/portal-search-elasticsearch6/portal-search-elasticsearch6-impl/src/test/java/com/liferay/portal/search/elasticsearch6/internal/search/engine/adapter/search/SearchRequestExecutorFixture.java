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

package com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter.search;

import com.liferay.portal.search.elasticsearch6.internal.SearchHitDocumentTranslatorImpl;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.elasticsearch6.internal.facet.DefaultFacetProcessor;
import com.liferay.portal.search.elasticsearch6.internal.facet.DefaultFacetTranslator;
import com.liferay.portal.search.elasticsearch6.internal.facet.FacetProcessor;
import com.liferay.portal.search.elasticsearch6.internal.facet.FacetTranslator;
import com.liferay.portal.search.elasticsearch6.internal.filter.ElasticsearchFilterTranslatorFixture;
import com.liferay.portal.search.elasticsearch6.internal.groupby.DefaultGroupByTranslator;
import com.liferay.portal.search.elasticsearch6.internal.highlight.DefaultHighlighterTranslator;
import com.liferay.portal.search.elasticsearch6.internal.query.ElasticsearchQueryTranslatorFixture;
import com.liferay.portal.search.elasticsearch6.internal.search.response.DefaultSearchResponseTranslator;
import com.liferay.portal.search.elasticsearch6.internal.sort.DefaultSortTranslator;
import com.liferay.portal.search.elasticsearch6.internal.stats.DefaultStatsTranslator;
import com.liferay.portal.search.engine.adapter.search.SearchRequestExecutor;

/**
 * @author Michael C. Han
 */
public class SearchRequestExecutorFixture {

	public SearchRequestExecutor getSearchRequestExecutor() {
		return _searchRequestExecutor;
	}

	public void setUp() {
		FacetProcessor<?> facetProcessor = getFacetProcessor();

		_searchRequestExecutor = createSearchRequestExecutor(
			_elasticsearchClientResolver, facetProcessor);
	}

	protected static CommonSearchRequestBuilderAssembler
		createCommonSearchRequestBuilderAssembler(
			FacetProcessor facetProcessor) {

		return new CommonSearchRequestBuilderAssemblerImpl() {
			{
				setFacetTranslator(createFacetTranslator(facetProcessor));

				ElasticsearchFilterTranslatorFixture
					elasticsearchFilterTranslatorFixture =
						new ElasticsearchFilterTranslatorFixture();

				setFilterTranslator(
					elasticsearchFilterTranslatorFixture.
						getElasticsearchFilterTranslator());

				ElasticsearchQueryTranslatorFixture
					elasticsearchQueryTranslatorFixture =
						new ElasticsearchQueryTranslatorFixture();

				setQueryTranslator(
					elasticsearchQueryTranslatorFixture.
						getElasticsearchQueryTranslator());
			}
		};
	}

	protected static CountSearchRequestExecutor
		createCountSearchRequestExecutor(
			ElasticsearchClientResolver elasticsearchClientResolver,
			FacetProcessor facetProcessor) {

		return new CountSearchRequestExecutorImpl() {
			{
				setCommonSearchRequestBuilderAssembler(
					createCommonSearchRequestBuilderAssembler(facetProcessor));
				setCommonSearchResponseAssembler(
					new CommonSearchResponseAssemblerImpl());
				setElasticsearchClientResolver(elasticsearchClientResolver);
			}
		};
	}

	protected static FacetTranslator createFacetTranslator(
		FacetProcessor facetProcessor) {

		return new DefaultFacetTranslator() {
			{
				setFacetProcessor(facetProcessor);

				ElasticsearchFilterTranslatorFixture
					elasticsearchFilterTranslatorFixture =
						new ElasticsearchFilterTranslatorFixture();

				setFilterTranslator(
					elasticsearchFilterTranslatorFixture.
						getElasticsearchFilterTranslator());
			}
		};
	}

	protected static MultisearchSearchRequestExecutor
		createMultisearchSearchRequestExecutor(
			ElasticsearchClientResolver elasticsearchClientResolver,
			FacetProcessor facetProcessor) {

		return new MultisearchSearchRequestExecutorImpl() {
			{
				setElasticsearchClientResolver(elasticsearchClientResolver);
				setSearchSearchRequestAssembler(
					createSearchSearchRequestAssembler(facetProcessor));
				setSearchSearchResponseAssembler(
					createSearchSearchResponseAssembler());
			}
		};
	}

	protected static SearchRequestExecutor createSearchRequestExecutor(
		ElasticsearchClientResolver elasticsearchClientResolver,
		FacetProcessor facetProcessor) {

		return new ElasticsearchSearchRequestExecutor() {
			{
				setCountSearchRequestExecutor(
					createCountSearchRequestExecutor(
						elasticsearchClientResolver, facetProcessor));
				setMultisearchSearchRequestExecutor(
					createMultisearchSearchRequestExecutor(
						elasticsearchClientResolver, facetProcessor));
				setSearchSearchRequestExecutor(
					createSearchSearchRequestExecutor(
						elasticsearchClientResolver, facetProcessor));
			}
		};
	}

	protected static SearchSearchRequestAssembler
		createSearchSearchRequestAssembler(FacetProcessor facetProcessor) {

		return new SearchSearchRequestAssemblerImpl() {
			{
				setCommonSearchRequestBuilderAssembler(
					createCommonSearchRequestBuilderAssembler(facetProcessor));
				setGroupByTranslator(new DefaultGroupByTranslator());
				setHighlighterTranslator(new DefaultHighlighterTranslator());
				setSortTranslator(new DefaultSortTranslator());
				setStatsTranslator(new DefaultStatsTranslator());
			}
		};
	}

	protected static SearchSearchRequestExecutor
		createSearchSearchRequestExecutor(
			ElasticsearchClientResolver elasticsearchClientResolver,
			FacetProcessor facetProcessor) {

		return new SearchSearchRequestExecutorImpl() {
			{
				setElasticsearchClientResolver(elasticsearchClientResolver);
				setSearchSearchRequestAssembler(
					createSearchSearchRequestAssembler(facetProcessor));
				setSearchSearchResponseAssembler(
					createSearchSearchResponseAssembler());
			}
		};
	}

	protected static SearchSearchResponseAssembler
		createSearchSearchResponseAssembler() {

		return new SearchSearchResponseAssemblerImpl() {
			{
				setCommonSearchResponseAssembler(
					new CommonSearchResponseAssemblerImpl());
				setSearchResponseTranslator(
					new DefaultSearchResponseTranslator() {
						{
							setSearchHitDocumentTranslator(
								new SearchHitDocumentTranslatorImpl());
							setStatsTranslator(new DefaultStatsTranslator());
						}
					});
			}
		};
	}

	protected FacetProcessor getFacetProcessor() {
		if (_facetProcessor != null) {
			return _facetProcessor;
		}

		return new DefaultFacetProcessor();
	}

	protected void setElasticsearchClientResolver(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
	}

	protected void setFacetProcessor(FacetProcessor<?> facetProcessor) {
		_facetProcessor = facetProcessor;
	}

	private ElasticsearchClientResolver _elasticsearchClientResolver;
	private FacetProcessor<?> _facetProcessor;
	private SearchRequestExecutor _searchRequestExecutor;

}