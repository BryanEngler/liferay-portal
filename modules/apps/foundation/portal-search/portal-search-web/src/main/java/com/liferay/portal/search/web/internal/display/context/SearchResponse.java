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
import com.liferay.portal.kernel.search.SearchContext;

/**
 * @author André de Oliveira
 */
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

	public SearchContainer<Document> getSearchContainer() {
		return _searchContainer;
	}

	public SearchContext getSearchContext() {
		return _searchContext;
	}

	private final Hits _hits;
	private final SearchContainer<Document> _searchContainer;
	private final SearchContext _searchContext;

}