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

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.web.internal.portlet.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPorletQueryConfigPreferences;
import com.liferay.portal.search.web.internal.portlet.SearchPortletFacetsConfigurationSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletFacetsDisplayPreferences;
import com.liferay.portal.search.web.internal.portlet.SearchPortletFacetsSearchContributor;
import com.liferay.portal.search.web.internal.portlet.SearchPortletHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletKeywordsSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletQueryConfigSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchContainerSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchContextSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchFacetsSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchScopeGroupIdSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchScopePreferenceStringSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchScopePreferenceSupplier;
import com.liferay.portal.search.web.internal.portlet.SearchPortletSearchScopeSupplier;

import java.util.Collections;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tina Tian
 */
@Component(immediate = true, service = SearchDisplayContextFactory.class)
public class SearchDisplayContextFactoryImpl
	implements SearchDisplayContextFactory {

	@Override
	public SearchDisplayContext create(
			RenderRequest renderRequest, RenderResponse renderResponse,
			PortletPreferences portletPreferences)
		throws Exception {

		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		SearchScopePreferenceStringSupplier searchScopePreferenceStringSupplier =
			new SearchPortletSearchScopePreferenceStringSupplier(
				portletPreferences);

		SearchScopePreferenceSupplier searchScopePreferenceSupplier =
			new SearchPortletSearchScopePreferenceSupplier(
				searchScopePreferenceStringSupplier);

		SearchScopeSupplier searchScopeSupplier =
			new SearchPortletSearchScopeSupplier(
				renderRequest, searchScopePreferenceSupplier);

		SearchScopeGroupIdSupplier searchScopeGroupIdSupplier =
			new SearchPortletSearchScopeGroupIdSupplier(
				searchScopeSupplier, themeDisplaySupplier);

		FacetsDisplayPreferences facetsDisplayPreferences =
			new SearchPortletFacetsDisplayPreferences(portletPreferences);

		QueryConfigPreferences queryConfigPreferences =
			new SearchPorletQueryConfigPreferences(
				portletPreferences, new IndexSearchPropsValuesImpl());

		SearchFacetsSupplier searchFacetsSupplier =
			new SearchPortletSearchFacetsSupplier(facetsDisplayPreferences);

		FacetsConfigurationSupplier facetsConfigurationSupplier =
			new SearchPortletFacetsConfigurationSupplier(portletPreferences);

		SearchContributor searchContributor =
			new SearchPortletFacetsSearchContributor(
				searchFacetsSupplier, facetsConfigurationSupplier,
				themeDisplaySupplier);

		PortalHttpServletRequestSupplier requestSupplier =
			new SearchPortletHttpServletRequestSupplier(portal, renderRequest);

		KeywordsSupplier keywordsSupplier = new SearchPortletKeywordsSupplier(
			renderRequest);

		PortletURLFactory portletURLFactory = new PortletURLFactoryImpl(
			renderRequest, renderResponse);

		SearchContainerSupplier searchContainerSupplier =
			new SearchPortletSearchContainerSupplier(
				renderRequest, language, requestSupplier, HtmlUtil.getHtml(),
				keywordsSupplier, portletURLFactory);

		return new SearchDisplayContext(
			portletPreferences,
			facetedSearcherManager,
			new SearchPortletSearchContextSupplier(requestSupplier),
			searchContainerSupplier,
			keywordsSupplier,
			() -> Collections.singleton(searchContributor),
			searchScopeGroupIdSupplier, searchScopeSupplier,
			searchScopePreferenceSupplier, searchScopePreferenceStringSupplier,
			searchFacetsSupplier,
			facetsConfigurationSupplier,
			facetsDisplayPreferences,
			new SearchPortletQueryConfigSupplier(queryConfigPreferences),
			queryConfigPreferences, themeDisplaySupplier,
			portletURLFactory);
	}

	@Reference
	protected FacetedSearcherManager facetedSearcherManager;

	@Reference
	protected Language language;

	@Reference
	protected Portal portal;

}