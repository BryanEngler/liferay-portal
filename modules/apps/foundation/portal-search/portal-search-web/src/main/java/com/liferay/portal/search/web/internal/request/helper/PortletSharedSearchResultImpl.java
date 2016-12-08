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

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;
import com.liferay.portal.search.web.portlet.SearchParametersConfiguration;

/**
 * @author André de Oliveira
 */
public class PortletSharedSearchResultImpl
	implements PortletSharedSearchResult {

	@Override
	public Facet getFacet(String name) {
		return _searchContext.getFacet(name);
	}

	@Override
	public SearchParametersConfiguration getSearchParametersConfiguration() {
		return _searchParametersConfiguration;
	}

	@Override
	public SearchResultsData getSearchResultsData() {
		return _searchResultsData;
	}

	public void setSearchContext(SearchContext searchContext) {
		_searchContext = searchContext;
	}

	public void setSearchParametersConfiguration(
		SearchParametersConfiguration searchParametersConfiguration) {

		_searchParametersConfiguration = searchParametersConfiguration;
	}

	public void setSearchResultsData(SearchResultsData searchResultsData) {
		_searchResultsData = searchResultsData;
	}

	private SearchContext _searchContext;
	private SearchParametersConfiguration _searchParametersConfiguration;
	private SearchResultsData _searchResultsData;

}