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

package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PredicateFilter;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.liferay.portal.search.web.internal.display.context.FacetsDisplayPreferences;
import com.liferay.portal.search.web.internal.display.context.SearchFacetsSupplier;

import java.util.Collection;
import java.util.List;

/**
 * @author André de Oliveira
 */
public class SearchPortletSearchFacetsSupplier implements SearchFacetsSupplier {

	public SearchPortletSearchFacetsSupplier(
		FacetsDisplayPreferences facetsDisplayPreferences) {

		_facetsDisplayPreferences = facetsDisplayPreferences;
	}

	@Override
	public Collection<SearchFacet> getSearchFacets() {
		if (_enabledSearchFacets != null) {
			return _enabledSearchFacets;
		}

		_enabledSearchFacets = ListUtil.filter(
			SearchFacetTracker.getSearchFacets(),
			new PredicateFilter<SearchFacet>() {

				@Override
				public boolean filter(SearchFacet searchFacet) {
					return _facetsDisplayPreferences.isDisplay(searchFacet);
				}

			});

		return _enabledSearchFacets;
	}

	private List<SearchFacet> _enabledSearchFacets;
	private final FacetsDisplayPreferences _facetsDisplayPreferences;

}