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

package com.liferay.headless.search.internal.resource.v1_0;

import com.liferay.headless.search.dto.v1_0.SearchResult;
import com.liferay.headless.search.resource.v1_0.SearchResultResource;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.legacy.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bryan Engler
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/search-result.properties",
	scope = ServiceScope.PROTOTYPE, service = SearchResultResource.class
)
public class SearchResultResourceImpl extends BaseSearchResultResourceImpl {

	@Override
	public SearchResult getSearchHiddenCompanyIndexKeywordsFromSize( //hidden docs
			Long companyId, String index, String keywords, Long from, Long size)
		throws Exception {
//get hidden doc uids
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(index);

		BooleanQuery booleanQuery = queries.booleanQuery();

		TermQuery keywordsTermQuery = queries.term("keywords", keywords);
		TermQuery companyIdTermQuery = queries.term("index", "liferay-"+ companyId);

		booleanQuery.addMustQueryClauses(keywordsTermQuery, companyIdTermQuery);

		searchSearchRequest.setQuery(booleanQuery);

		SearchSearchResponse searchSearchResponse =
			searchEngineAdapter.execute(searchSearchRequest);

		Hits hits = searchSearchResponse.getHits();

		Document[] documents = hits.getDocs();

		Document document = documents[0];

		String[] hidden_doc_uids = document.getValues("hidden_documents");
//
		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(companyId);
		searchContext.setStart(from.intValue());
		searchContext.setEnd(from.intValue() + size.intValue());

		SearchSearchRequest searchSearchRequest2 = new SearchSearchRequest();

		searchSearchRequest2.setIndexNames("liferay-"+ companyId);

		TermsQuery termsQuery = queries.terms("uid");

		termsQuery.addValues(hidden_doc_uids);

		searchSearchRequest2.setQuery(termsQuery);

		SearchSearchResponse searchSearchResponse2 =
			searchEngineAdapter.execute(searchSearchRequest2);

		Hits hits1 = searchSearchResponse2.getHits();

		return _toResults(hits1.toList());
	}

	@Override
	public SearchResult getSearchCompanyKeywordsFromSize( //all docs (minus hidden)
			Long companyId, String keywords, Long from, Long size)
		throws Exception {

		//get hidden doc uids
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames("results-ranking");

		BooleanQuery booleanQuery = queries.booleanQuery();

		TermQuery keywordsTermQuery = queries.term("keywords", keywords);
		TermQuery companyIdTermQuery = queries.term("index", "liferay-"+ companyId);

		booleanQuery.addMustQueryClauses(keywordsTermQuery, companyIdTermQuery);

		searchSearchRequest.setQuery(booleanQuery);

		SearchSearchResponse searchSearchResponse =
			searchEngineAdapter.execute(searchSearchRequest);

		Hits hits = searchSearchResponse.getHits();

		Document[] documents = hits.getDocs();

		Document document = documents[0];

		String[] hidden_doc_uids = document.getValues("hidden_documents");

		List<String> hiddenUids = ListUtil.fromArray(hidden_doc_uids);
//

		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(companyId);
		searchContext.setKeywords(keywords);
		searchContext.setStart(from.intValue());
		searchContext.setEnd(from.intValue() + size.intValue());

		SearchRequestBuilder searchRequestBuilder =
			searchRequestBuilderFactory.getSearchRequestBuilder(
				searchContext);

		SearchRequest searchRequest = searchRequestBuilder.build();

		SearchResponse searchResponse = searcher.search(searchRequest);

		List<Document> docs = searchResponse.getDocuments71();

		List<Document> filteredDocs = new ArrayList<>();

		for (Document document1 : docs) {
			String uid = document1.getUID();

			if (!hiddenUids.contains(uid)) {
				filteredDocs.add(document1);
			}
		}

		return _toResults(filteredDocs);
	}

	@Reference
	protected Queries queries;

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	@Reference
	protected SearchRequestBuilderFactory searchRequestBuilderFactory;

	@Reference
	protected Searcher searcher;

	private SearchResult _toResults(List<Document> docs) throws Exception {
		com.liferay.headless.search.dto.v1_0.Document[] restDocuments =
				new com.liferay.headless.search.dto.v1_0.Document[docs.size()];

		for (int i = 0; i< docs.size(); i++) {
			Document document = docs.get(i);

			String title = document.get(Field.TITLE + "_en_US");

			if (Validator.isBlank(title)) {
				title = document.get(Field.TITLE);
			}

			final String docTitle = title;

			com.liferay.headless.search.dto.v1_0.Document restDocument =
				new com.liferay.headless.search.dto.v1_0.Document() {
				{
					author = document.get(Field.USER_NAME);
					clicks = document.get("clicks");
					description = document.get(Field.DESCRIPTION);
					hidden = document.get(Field.HIDDEN);
					id = document.get(Field.UID);
					pinned = true;
					title = docTitle;
					type = document.get(Field.ENTRY_CLASS_NAME);
				}
			};

			restDocuments[i] = restDocument;
		}

		return new SearchResult() {
			{
				total = Long.valueOf(docs.size());
				documents = restDocuments;
			}
		};
	}


}