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

package com.liferay.portal.search.web.internal.results.search;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

import java.io.Serializable;

import java.util.List;

/**
 * @author Rodrigo Paulino
 * @author André de Oliveira
 */
public class SearchResultsDataImpl implements SearchResultsData, Serializable {

	@Override
	public List<Document> getDocuments() {
		return _documents;
	}

	@Override
	public int getFrom() {
		return _from;
	}

	@Override
	public String[] getHighlights() {
		return _highlights;
	}

	@Override
	public String getKeywords() {
		return _keywords;
	}

	@Override
	public String getQueryString() {
		return _queryString;
	}

	@Override
	public int getTotalHits() {
		return _totalHits;
	}

	public void setDocuments(List<Document> documents) {
		_documents = documents;
	}

	public void setFrom(int from) {
		_from = from;
	}

	public void setHighlights(String[] highlights) {
		_highlights = highlights;
	}

	public void setKeywords(String keywords) {
		_keywords = keywords;
	}

	public void setQueryString(String queryString) {
		_queryString = queryString;
	}

	public void setTotalHits(int totalHits) {
		_totalHits = totalHits;
	}

	private List<Document> _documents;
	private int _from;
	private String[] _highlights;
	private String _keywords;
	private String _queryString;
	private int _totalHits;

}