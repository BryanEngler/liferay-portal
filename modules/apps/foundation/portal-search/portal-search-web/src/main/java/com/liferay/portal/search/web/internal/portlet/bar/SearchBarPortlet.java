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

package com.liferay.portal.search.web.internal.portlet.bar;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.internal.display.context.SearchScope;
import com.liferay.portal.search.web.internal.request.helper.OriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortletOriginalServletRequestSupplierFactory;
import com.liferay.portal.search.web.internal.request.params.SearchParameters;
import com.liferay.portal.search.web.internal.request.params.SearchParametersBuilderImpl;
import com.liferay.portal.search.web.internal.request.params.SearchParametersImpl;
import com.liferay.portal.search.web.portlet.SearchParametersBuilder;
import com.liferay.portal.search.web.portlet.SearchParametersConfiguration;

import java.io.IOException;

import java.util.Optional;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author André de Oliveira
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.css-class-wrapper=" +
			SearchBarPortletKeys.CSS_CLASS_WRAPPER,
		"com.liferay.portlet.display-category=category.search",
		"com.liferay.portlet.header-portlet-css=" +
			SearchBarPortletKeys.HEADER_PORTLET_CSS,
		"com.liferay.portlet.icon=/icons/search.png",
		"com.liferay.portlet.instanceable=true",
		"com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.restore-current-view=false",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=" + SearchBarPortletKeys.DISPLAY_NAME,
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=" +
			SearchBarPortletKeys.VIEW_TEMPLATE,
		"javax.portlet.name=" + SearchBarPortletKeys.PORTLET_NAME,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = Portlet.class
)
public class SearchBarPortlet extends MVCPortlet {

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		SearchBarPortletPreferences searchBarPortletPreferences =
			new SearchBarPortletPreferencesImpl(
				Optional.ofNullable(renderRequest.getPreferences()));

		SearchBarPortletDisplayBuilder searchBarPortletDisplayBuilder =
			new SearchBarPortletDisplayBuilder();

		// TODO Autocomplete

		String fakeAutocompleteURL =
			renderRequest.getContextPath() + "/search/portlet/bar/demo.json";

		searchBarPortletDisplayBuilder.setAutocompleteURL(
			searchBarPortletPreferences.getAutocompleteURL());

		String keywordsParameterNameString =
			searchBarPortletPreferences.getKeywordsParameterNameString();

		searchBarPortletDisplayBuilder.setKeywords(
			getKeywords(renderRequest, keywordsParameterNameString));

		searchBarPortletDisplayBuilder.setKeywordsParameterName(
			keywordsParameterNameString);

		searchBarPortletDisplayBuilder.setScopePreference(
			searchBarPortletPreferences.getScope());

		// TODO Set / get Scope Selection from URL parameter

		searchBarPortletDisplayBuilder.setScope(SearchScope.EVERYTHING);

		// TODO see SearchDisplayContext (ThemeDisplay stagingGroup)

		searchBarPortletDisplayBuilder.setScopePreferenceEverythingAvailable(
			true);

		SearchBarPortletDisplayContext searchBarPortletDisplayContext =
			searchBarPortletDisplayBuilder.build();

		renderRequest.setAttribute(
			SearchBarPortletDisplayContext.ATTRIBUTE,
			searchBarPortletDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	protected SearchParameters createSearchParameters(
		RenderRequest renderRequest,
		SearchParametersConfiguration searchParametersConfiguration) {

		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			portletOriginalServletRequestSupplierFactory.get(renderRequest);

		return new SearchParametersImpl(
			originalHttpServletRequestSupplier, searchParametersConfiguration);
	}

	protected String getKeywords(
		RenderRequest renderRequest, String keywordsParameterNameString) {

		SearchParametersBuilder searchParametersBuilder =
			new SearchParametersBuilderImpl();

		searchParametersBuilder.setKeywordsParameterName(
			keywordsParameterNameString);

		SearchParameters searchParameters = createSearchParameters(
			renderRequest, searchParametersBuilder.build());

		Optional<String> parameterOptional =
			searchParameters.getKeywordsParameterValue();

		return parameterOptional.orElse(StringPool.BLANK);
	}

	@Reference
	protected PortletOriginalServletRequestSupplierFactory
		portletOriginalServletRequestSupplierFactory;

}