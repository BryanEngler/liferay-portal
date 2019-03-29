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

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

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

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(index);

		BooleanQuery booleanQuery = queries.booleanQuery();

		Query matchTitleQuery = queries.match("companyId", keywords);

		booleanQuery.addMustQueryClauses(matchTitleQuery);

		searchSearchRequest.setQuery(booleanQuery);
		searchSearchRequest.setStart(start.intValue());
		searchSearchRequest.setSize(delta.intValue());

		SearchSearchResponse searchSearchResponse =
			searchEngineAdapter.execute(searchSearchRequest);

		Hits hits = searchSearchResponse.getHits();

		return _toResults(hits);
	}

	@Reference
	protected Queries queries;

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	private SearchResult _toResults(Hits hits) throws Exception {
		Document[] docs = hits.getDocs();

		com.liferay.headless.search.dto.v1_0.Document[] restDocuments =
				new com.liferay.headless.search.dto.v1_0.Document[docs.length];

		for (int i = 0; i< docs.length; i++) {
			Document document = docs[i];

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
				items = Long.valueOf(docs.length);
				documents = restDocuments;
			}
		};
	}


}