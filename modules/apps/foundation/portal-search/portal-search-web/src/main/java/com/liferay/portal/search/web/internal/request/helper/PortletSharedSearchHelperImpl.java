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

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.internal.display.context.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.display.context.SearchContributor;
import com.liferay.portal.search.web.internal.display.context.SearchContributorsSupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.request.params.SearchParametersBuilderImpl;
import com.liferay.portal.search.web.internal.results.data.SearchResultsDataSupplier;
import com.liferay.portal.search.web.internal.results.search.SearchResultsDataSupplierImpl;
import com.liferay.portal.search.web.portlet.SearchAwarePortlet;
import com.liferay.portal.search.web.portlet.SearchParametersBuilder;
import com.liferay.portal.search.web.search.builder.SearchBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author André de Oliveira
 */
@Component(service = PortletSharedSearchHelper.class)
public class PortletSharedSearchHelperImpl
	implements PortletSharedSearchHelper {

	@Override
	public PortletSharedSearchResult search(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			portletOriginalServletRequestSupplierFactory.get(renderRequest);

		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		SearchParametersBuilder searchParametersBuilder =
			new SearchParametersBuilderImpl();

		SearchContributorsSupplier searchContributorsSupplier =
			() -> getSearchContributors(searchParametersBuilder, renderRequest);

		SearchResultsDataSupplier searchResultsDataSupplier =
			new SearchResultsDataSupplierImpl(
				searchParametersBuilder.build(),
				originalHttpServletRequestSupplier, renderRequest,
				themeDisplaySupplier, searchContributorsSupplier,
				facetedSearcherManager);

		SearchHttpServletRequestHelper searchHttpServletRequestHelper =
			new SearchHttpServletRequestHelper(
				originalHttpServletRequestSupplier);

		return searchHttpServletRequestHelper.searchOnlyOnce(
			searchResultsDataSupplier);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void addSearchAwarePortlet(
		SearchAwarePortlet searchAwarePortlet) {

		Class<?> clazz = searchAwarePortlet.getClass();

		String portletClassName = clazz.getName();

		_searchAwareFacetPortlets.put(portletClassName, searchAwarePortlet);
	}

	protected Stream<Portlet> getExplicitlyAddedPortlets(
		ThemeDisplay themeDisplay) {

		Layout layout = themeDisplay.getLayout();

		LayoutTypePortlet layoutTypePortlet =
			(LayoutTypePortlet)layout.getLayoutType();

		List<Portlet> portlets = layoutTypePortlet.getExplicitlyAddedPortlets();

		return portlets.stream();
	}

	protected Optional<SearchAwarePortlet> getSearchAwarePortlet(
		String portletClassName) {

		return Optional.ofNullable(
			_searchAwareFacetPortlets.get(portletClassName));
	}

	protected Optional<SearchContributor> getSearchContributor(
		Portlet portlet, SearchParametersBuilder searchParametersBuilder,
		RenderRequest renderRequest) {

		Optional<SearchAwarePortlet> searchAwarePortletOptional =
			getSearchAwarePortlet(portlet.getPortletClass());

		Optional<SearchContributor> searchContributorOptional =
			searchAwarePortletOptional.map(
				searchAwarePortlet -> getSearchContributor(
					searchAwarePortlet, searchParametersBuilder, renderRequest,
					portlet.getPortletId()));

		return searchContributorOptional;
	}

	protected SearchContributor getSearchContributor(
		SearchAwarePortlet searchAwarePortlet,
		SearchParametersBuilder searchParametersBuilder,
		RenderRequest renderRequest, String portletId) {

		return new SearchContributor() {

			@Override
			public void contribute(
				SearchBuilder searchBuilder, SearchContext searchContext) {

				searchAwarePortlet.contribute(
					searchBuilder, searchParametersBuilder, renderRequest,
					portletId, searchContext);
			}

		};
	}

	protected Collection<SearchContributor> getSearchContributors(
		SearchParametersBuilder searchParametersBuilder,
		RenderRequest renderRequest) {

		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		Stream<Portlet> portlets = getExplicitlyAddedPortlets(themeDisplay);

		Stream<Optional<SearchContributor>> searchContributorOptionals =
			portlets.map(
				portlet -> getSearchContributor(
					portlet, searchParametersBuilder, renderRequest));

		searchContributorOptionals = searchContributorOptionals.filter(
			Optional::isPresent);

		Stream<SearchContributor> searchContributors =
			searchContributorOptionals.map(Optional::get);

		return searchContributors.collect(Collectors.toList());
	}

	protected ThemeDisplay getThemeDisplay(RenderRequest renderRequest) {
		return (ThemeDisplay)renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
	}

	protected void removeSearchAwarePortlet(
		SearchAwarePortlet searchAwarePortlet) {

		Class<?> clazz = searchAwarePortlet.getClass();

		String portletClassName = clazz.getName();

		_searchAwareFacetPortlets.remove(portletClassName);
	}

	@Reference
	protected FacetedSearcherManager facetedSearcherManager;

	@Reference
	protected Language language;

	@Reference
	protected Portal portal;

	@Reference
	protected PortletOriginalServletRequestSupplierFactory
		portletOriginalServletRequestSupplierFactory;

	private final Map<String, SearchAwarePortlet> _searchAwareFacetPortlets =
		new HashMap<>();

}