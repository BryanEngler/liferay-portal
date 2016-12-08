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

package com.liferay.portal.search.web.internal.request.helper;

import com.liferay.portal.search.web.internal.results.data.SearchResultsDataSupplier;

import javax.servlet.http.HttpServletRequest;

/**
 * @author André de Oliveira
 */
public class SearchHttpServletRequestHelper {

	public SearchHttpServletRequestHelper(
		OriginalHttpServletRequestSupplier request) {

		_request = request.get();
	}

	public PortletSharedSearchResult searchOnlyOnce(
		SearchResultsDataSupplier searchResultsDataSupplier) {

		setResults(searchResultsDataSupplier);

		PortletSharedSearchResult results = getResults();

		return results;
	}

	protected PortletSharedSearchResult getResults() {
		return (PortletSharedSearchResult)_request.getAttribute(
			_SEARCH_RESULTS_ATTRIBUTE);
	}

	protected void setResults(SearchResultsDataSupplier search) {
		if (_request.getAttribute(_SEARCH_RESULTS_ATTRIBUTE) != null) {
			return;
		}

		PortletSharedSearchResult portletSharedSearchResult = search.get();

		_request.setAttribute(
			_SEARCH_RESULTS_ATTRIBUTE, portletSharedSearchResult);
	}

	private static final String _SEARCH_RESULTS_ATTRIBUTE =
		"LIFERAY_SHARED____________________SEARCH_RESULTS___________________";

	private final HttpServletRequest _request;

}