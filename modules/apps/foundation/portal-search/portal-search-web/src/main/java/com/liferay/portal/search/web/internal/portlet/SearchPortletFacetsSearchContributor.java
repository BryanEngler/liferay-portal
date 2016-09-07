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

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.internal.display.context.FacetsConfigurationSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchContributor;
import com.liferay.portal.search.web.internal.display.context.SearchFacetsSupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.search.builder.SearchBuilder;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author André de Oliveira
 */
public class SearchPortletFacetsSearchContributor implements SearchContributor {

	public SearchPortletFacetsSearchContributor(
		SearchFacetsSupplier searchFacetsSupplier,
		FacetsConfigurationSupplier facetsConfigurationSupplier,
		ThemeDisplaySupplier themeDisplaySupplier) {

		_searchFacetsSupplier = searchFacetsSupplier;
		_facetsConfigurationSupplier = facetsConfigurationSupplier;
		_themeDisplaySupplier = themeDisplaySupplier;
	}

	@Override
	public void contribute(
		SearchBuilder searchBuilder, SearchContext searchContext) {

		ThemeDisplay themeDisplay = _themeDisplaySupplier.getThemeDisplay();

		addEnabledSearchFacets(themeDisplay.getCompanyId(), searchContext);
	}

	protected void addEnabledSearchFacets(
		long companyId, SearchContext searchContext) {

		Collection<SearchFacet> searchFacets =
			_searchFacetsSupplier.getSearchFacets();

		Stream<SearchFacet> searchFacetsStream = searchFacets.stream();

		Stream<Optional<Facet>> optionalFacetsStream = searchFacetsStream.map(
			searchFacet -> createFacet(searchFacet, companyId, searchContext));

		optionalFacetsStream = optionalFacetsStream.filter(Optional::isPresent);

		Stream<Facet> facetsStream = optionalFacetsStream.map(Optional::get);

		facetsStream.forEach(searchContext::addFacet);
	}

	protected Optional<Facet> createFacet(
		SearchFacet searchFacet, long companyId, SearchContext searchContext) {

		try {
			searchFacet.init(
				companyId,
				_facetsConfigurationSupplier.getFacetsConfiguration(),
				searchContext);
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return Optional.ofNullable(searchFacet.getFacet());
	}

	private final FacetsConfigurationSupplier _facetsConfigurationSupplier;
	private final SearchFacetsSupplier _searchFacetsSupplier;
	private final ThemeDisplaySupplier _themeDisplaySupplier;

}