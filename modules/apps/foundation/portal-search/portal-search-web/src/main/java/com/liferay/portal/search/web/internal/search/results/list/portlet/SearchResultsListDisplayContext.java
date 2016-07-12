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

package com.liferay.portal.search.web.internal.search.results.list.portlet;

import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

import java.io.Serializable;

/**
 * @author André de Oliveira
 */
public class SearchResultsListDisplayContext implements Serializable {

	public SearchResultsListDisplayContext(
		SearchResultsData searchResultsData, String qParameter) {

		_searchResultsData = searchResultsData;
		_qParameter = qParameter;
	}

	public String getQ() {
		return _qParameter;
	}

	public SearchResultsData getSearchResultsData() {
		return _searchResultsData;
	}

	public static final String ATTRIBUTE = "SearchResultsListDisplayContext";

	private final String _qParameter;
	private final SearchResultsData _searchResultsData;

}