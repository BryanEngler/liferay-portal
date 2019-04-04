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
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.search.legacy.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

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
	public SearchResult getSearchIndexKeywordsHiddenStartDelta(
			String index, String keywords, String hidden, Long start, Long delta)
		throws Exception {

		SearchContext searchContext = new SearchContext();

		searchContext.setKeywords(keywords);
		searchContext.setStart(start.intValue());
		searchContext.setEnd(start.intValue() + delta.intValue());
		searchContext.setCompanyId(start);

		SearchRequestBuilder searchRequestBuilder =
			searchRequestBuilderFactory.getSearchRequestBuilder(
				searchContext);

		SearchRequest searchRequest = searchRequestBuilder.build();

		SearchResponse searchResponse = searcher.search(searchRequest);

		return _toResults(searchResponse);
	}

	@Reference
	protected SearchRequestBuilderFactory searchRequestBuilderFactory;

	@Reference
	protected Searcher searcher;

	private SearchResult _toResults(SearchResponse searchResponse) throws Exception {
		List<Document> docs = searchResponse.getDocuments71();

		com.liferay.headless.search.dto.v1_0.Document[] restDocuments =
				new com.liferay.headless.search.dto.v1_0.Document[docs.size()];

		for (int i = 0; i< docs.size(); i++) {
			Document document = docs.get(i);

			com.liferay.headless.search.dto.v1_0.Document restDocument =
				new com.liferay.headless.search.dto.v1_0.Document() {
				{
					author = document.get(Field.USER_NAME);
					date = document.get(Field.CREATE_DATE);
					clicks = document.get("clicks");
					date = document.get(Field.CREATE_DATE);
					description = document.get(Field.DESCRIPTION);
					hidden = document.get(Field.HIDDEN);
					id = document.get(Field.UID);
					pinned = document.get("pinned");
					title = document.get(Field.TITLE);
					type = document.get(Field.ENTRY_CLASS_NAME);
				}
			};

			restDocuments[i] = restDocument;
		}

		return new SearchResult() {
			{
				items = Long.valueOf(docs.size());
				documents = restDocuments;
			}
		};
	}


}