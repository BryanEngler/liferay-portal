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

package com.liferay.portal.search.web.internal.search.facet.portlet;

import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.internal.request.helper.LiferayPortletHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.OriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortalOriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchHelper;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResult;
import com.liferay.portal.search.web.internal.request.params.SearchParametersImpl;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;
import com.liferay.portal.search.web.portlet.SearchParametersConfiguration;

import java.io.IOException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author André de Oliveira
 * @author Rodrigo Paulino
 */
@Component(service = SearchFacetPortletRenderHelper.class)
public class SearchFacetPortletRenderHelper {

	public SearchFacet getSearchFacet(String searchFacetClassName) {
		SearchFacet searchFacet = _searchFacets.get(searchFacetClassName);

		Optional<SearchFacet> searchFacetOptional = Optional.ofNullable(
			searchFacet);

		return searchFacetOptional.orElseThrow(
			() -> new IllegalArgumentException(searchFacetClassName));
	}

	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		SearchFacetConfiguration searchFacetConfigurationImpl =
			new SearchFacetConfigurationImpl(
				Optional.ofNullable(renderRequest.getPreferences()));

		PortletSharedSearchResult portletSharedSearchResult =
			portletSharedSearchFactory.search(renderRequest, renderResponse);

		Optional<String> searchFacetClassNameOptional =
			searchFacetConfigurationImpl.getSearchFacetClassName();

		Optional<SearchFacet> searchFacetOptional =
			searchFacetClassNameOptional.map(this::getSearchFacet);

		searchFacetOptional.ifPresent(
			searchFacet -> {
				renderRequest.setAttribute(
					"search.jsp-search-facet", searchFacet);

				String fieldName = searchFacet.getFieldName();

				Facet facet = portletSharedSearchResult.getFacet(fieldName);

				renderRequest.setAttribute("search.jsp-facet", facet);
			});

		SearchResultsData searchResultsData =
			portletSharedSearchResult.getSearchResultsData();

		SearchFacetDisplayContext displayContext =
			new SearchFacetDisplayContext(
				searchResultsData.getKeywords(), searchResultsData);

		renderRequest.setAttribute(
			SearchFacetDisplayContext.ATTRIBUTE, displayContext);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void addSearchFacet(SearchFacet searchFacet) {
		String searchFacetClassName = searchFacet.getClassName();

		_searchFacets.put(searchFacetClassName, searchFacet);
	}

	protected SearchParametersImpl createSearchParameters(
		RenderRequest renderRequest,
		SearchParametersConfiguration searchParametersConfiguration) {

		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			new PortalOriginalHttpServletRequestSupplier(
				new LiferayPortletHttpServletRequestSupplier(renderRequest),
				portal);

		return new SearchParametersImpl(
			originalHttpServletRequestSupplier, searchParametersConfiguration);
	}

	protected void removeSearchFacet(SearchFacet searchFacet) {
		String searchFacetClassName = searchFacet.getClassName();

		_searchFacets.remove(searchFacetClassName);
	}

	@Reference
	protected Portal portal;

	@Reference
	protected PortletSharedSearchHelper portletSharedSearchFactory;

	private final Map<String, SearchFacet> _searchFacets =
		new ConcurrentHashMap<>();

}