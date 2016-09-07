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

	public SearchResultsDataImpl(
		List<Document> documents, String[] queryTerms) {

		_documents = documents;
		_queryTerms = queryTerms;
	}

	@Override
	public List<Document> getDocuments() {
		return _documents;
	}

	@Override
	public String[] getQueryTerms() {
		return _queryTerms;
	}

	private final List<Document> _documents;
	private final String[] _queryTerms;

}