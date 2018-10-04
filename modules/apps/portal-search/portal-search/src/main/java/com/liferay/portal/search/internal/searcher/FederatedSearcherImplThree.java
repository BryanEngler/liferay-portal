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

package com.liferay.portal.search.internal.searcher;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.HitsImpl;
import com.liferay.portal.kernel.search.ParseException;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.StringQuery;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.engine.adapter.search.SearchRequestExecutor;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.spi.federated.searcher.FederatedSearcher;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = FederatedSearcher.class)
public class FederatedSearcherImplThree implements FederatedSearcher {

	@Override
	public Hits getHits(SearchContext searchContext) {
		String keywords = searchContext.getKeywords();

		if (Validator.isBlank(keywords)) {
			return new HitsImpl();
		}

		//search nutch_liferay
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(new String[]{"liferay_page_index"});

		Query federatedContentQuery = new StringQuery(
			"content_\\*:" + keywords);

		Query federatedContentTitleQuery = new StringQuery(
			"title_\\*:" + keywords);

		Query federatedDescriptionQuery = new StringQuery(
			"meta_description:" + keywords);

		Query federatedKeywordsQuery = new StringQuery(
			"meta_keywords:" + keywords);

		Query federatedTitleQuery = new StringQuery(
			"page_title:" + keywords);

		BooleanQuery booleanQuery = new BooleanQueryImpl();
		Query hitsQuery;

		try {
			booleanQuery.add(federatedTitleQuery, BooleanClauseOccur.SHOULD);
			booleanQuery.add(federatedKeywordsQuery, BooleanClauseOccur.SHOULD);
			booleanQuery.add(federatedDescriptionQuery, BooleanClauseOccur.SHOULD);
			booleanQuery.add(federatedContentTitleQuery, BooleanClauseOccur.SHOULD);
			booleanQuery.add(federatedContentQuery, BooleanClauseOccur.SHOULD);

			searchSearchRequest.setQuery(booleanQuery);

			hitsQuery = booleanQuery;
		}
		catch (ParseException pe) {
			hitsQuery = federatedContentQuery;
		}

		searchSearchRequest.setSize(10);
		searchSearchRequest.setHighlightEnabled(true);
		searchSearchRequest.setHighlightFieldNames(
			"content_*", "title_*", "meta_description", "meta_keywords", "page_title");

		//only needed if liferay_page_index index is not in same cluster as liferay index
		searchSearchRequest.setConnectionId("federated");

		SearchSearchResponse searchSearchResponse =
			searchSearchRequest.accept(searchRequestExecutor);

		Hits hits = searchSearchResponse.getHits();

		hits.setQuery(hitsQuery);

		return hits;
	}

	@Override
	public String getSourceDisplayName() {
		//must match FederatedSearchResultSummaryBuilderImplThree.getSourceDisplayName()
		return "Federated Page Search";
	}

	@Reference
	protected SearchRequestExecutor searchRequestExecutor;
}