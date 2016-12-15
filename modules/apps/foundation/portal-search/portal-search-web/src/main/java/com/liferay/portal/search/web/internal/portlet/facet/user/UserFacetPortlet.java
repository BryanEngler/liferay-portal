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

package com.liferay.portal.search.web.internal.portlet.facet.user;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.internal.display.context.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.facet.UserSearchFacet;
import com.liferay.portal.search.web.internal.facet.display.builder.UserSearchFacetDisplayBuilder;
import com.liferay.portal.search.web.internal.facet.display.context.UserSearchFacetDisplayContext;
import com.liferay.portal.search.web.internal.facet.display.context.UserSearchFacetTermDisplayContext;
import com.liferay.portal.search.web.internal.preferences.PortletPreferencesLookup;
import com.liferay.portal.search.web.internal.request.helper.OriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortletOriginalServletRequestSupplierFactory;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchHelper;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResult;
import com.liferay.portal.search.web.portlet.SearchAwarePortlet;
import com.liferay.portal.search.web.portlet.SearchParametersBuilder;
import com.liferay.portal.search.web.search.builder.SearchBuilder;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Lino Alves
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.css-class-wrapper=" +
			UserFacetPortletKeys.CSS_CLASS_WRAPPER,
		"com.liferay.portlet.display-category=category.search",
		"com.liferay.portlet.icon=/icons/search.png",
		"com.liferay.portlet.instanceable=true",
		"com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.restore-current-view=false",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=" +
			UserFacetPortletKeys.DISPLAY_NAME,
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=" +
			UserFacetPortletKeys.VIEW_TEMPLATE,
		"javax.portlet.name=" + UserFacetPortletKeys.PORTLET_NAME,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = {Portlet.class, SearchAwarePortlet.class}
)
public class UserFacetPortlet extends MVCPortlet implements SearchAwarePortlet {

	@Override
	public void contribute(
		SearchBuilder searchBuilder,
		SearchParametersBuilder searchParametersBuilder,
		RenderRequest renderRequest, String portletId,
		SearchContext searchContext) {

		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		Optional<PortletPreferences> preferences = getPortletPreferences(
			renderRequest, portletId);

		SearchFacet searchFacet = new UserSearchFacet();

		long companyId = themeDisplay.getCompanyId();

		FacetConfiguration facetConfiguration =
			searchFacet.getDefaultConfiguration(companyId);

		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			portletOriginalServletRequestSupplierFactory.get(renderRequest);

		setUsers(searchContext, getUsers(originalHttpServletRequestSupplier));

		addFacet(searchBuilder, facetConfiguration, searchContext);
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletSharedSearchResult portletSharedSearchResult =
			portletSharedSearchHelper.search(renderRequest, renderResponse);

		UserFacetPortletDisplayContext userFacetPortletDisplayContext =
			buildDisplayContext(renderRequest, portletSharedSearchResult);

		renderRequest.setAttribute(
			UserFacetPortletDisplayContext.ATTRIBUTE,
			userFacetPortletDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	protected void addFacet(
		SearchBuilder searchBuilder, FacetConfiguration facetConfiguration,
		SearchContext searchContext) {

		MultiValueFacet facet = new MultiValueFacet(searchContext);

		facet.setFacetConfiguration(facetConfiguration);

		searchBuilder.addFacet(facet);
	}

	protected UserFacetPortletDisplayContext buildDisplayContext(
		RenderRequest renderRequest, PortletSharedSearchResult result) {

		SearchFacet searchFacet = new UserSearchFacet();

		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		PortletPreferences preferences = renderRequest.getPreferences();

		long companyId = themeDisplay.getCompanyId();

		FacetConfiguration facetConfiguration =
			searchFacet.getDefaultConfiguration(companyId);

		JSONObject dataJSONObject = facetConfiguration.getData();

		Facet facet = result.getFacet(searchFacet.getFieldName());
		String fieldParam = getFieldParam(renderRequest);

		int countThreshold = dataJSONObject.getInt("frequencyThreshold");
		int maxTerms = dataJSONObject.getInt("maxTerms");
		boolean showFrequencies = dataJSONObject.getBoolean(
			"showAssetCount", true);

		UserSearchFacetDisplayBuilder userSearchFacetDisplayBuilder =
			new UserSearchFacetDisplayBuilder();

		userSearchFacetDisplayBuilder.setFacet(facet);
		userSearchFacetDisplayBuilder.setFrequenciesVisible(showFrequencies);
		userSearchFacetDisplayBuilder.setFrequencyThreshold(countThreshold);
		userSearchFacetDisplayBuilder.setMaxTerms(maxTerms);
		userSearchFacetDisplayBuilder.setParamName(_PARAM);
		userSearchFacetDisplayBuilder.setParamValue(fieldParam);

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			userSearchFacetDisplayBuilder.build();

		return buildDisplayContext(fieldParam, userSearchFacetDisplayContext);
	}

	protected UserFacetPortletDisplayContext buildDisplayContext(
		String fieldParam,
		UserSearchFacetDisplayContext userSearchFacetDisplayContext) {

		UserFacetPortletDisplayContext userFacetPortletDisplayContext =
			new UserFacetPortletDisplayContext();

		List<UserFacetPortletTermDisplayContext> termDisplayContexts =
			buildTermDisplayContexts(
				userSearchFacetDisplayContext.getTermDisplayContexts());

		userFacetPortletDisplayContext.setTerms(termDisplayContexts);

		boolean renderNothing = termDisplayContexts.isEmpty();

		boolean nothingSelected =
			userSearchFacetDisplayContext.isNothingSelected();

		userFacetPortletDisplayContext.setFieldParamInputName(_PARAM);
		userFacetPortletDisplayContext.setFieldParamInputValue(fieldParam);
		userFacetPortletDisplayContext.setRenderNothing(renderNothing);
		userFacetPortletDisplayContext.setNothingSelected(nothingSelected);

		return userFacetPortletDisplayContext;
	}

	protected List<UserFacetPortletTermDisplayContext> buildTermDisplayContexts(
		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts) {

		List<UserFacetPortletTermDisplayContext> termDisplayContexts =
			new ArrayList<>();

		for (UserSearchFacetTermDisplayContext
				userSearchFacetTermDisplayContext :
					userSearchFacetTermDisplayContexts) {

			termDisplayContexts.add(
				getTermDisplayContext(userSearchFacetTermDisplayContext));
		}

		return termDisplayContexts;
	}

	protected String getFieldParam(RenderRequest renderRequest) {
		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			portletOriginalServletRequestSupplierFactory.get(renderRequest);

		Optional<String> paramValue = getUserParamValue(
			originalHttpServletRequestSupplier);

		return paramValue.orElse(StringPool.BLANK);
	}

	protected Optional<PortletPreferences> getPortletPreferences(
		RenderRequest renderRequest, String portletId) {

		return portletPreferencesLookup.getPortletPreferences(
			renderRequest, portletId);
	}

	protected UserFacetPortletTermDisplayContext getTermDisplayContext(
		UserSearchFacetTermDisplayContext userSearchFacetTermDisplayContext) {

		UserFacetPortletTermDisplayContext termDisplayContext =
			new UserFacetPortletTermDisplayContext();

		termDisplayContext.setTerm(
			userSearchFacetTermDisplayContext.getUserName());
		termDisplayContext.setFrequency(
			userSearchFacetTermDisplayContext.getFrequency());
		termDisplayContext.setFrequencyVisible(
			userSearchFacetTermDisplayContext.isFrequencyVisible());
		termDisplayContext.setSelected(
			userSearchFacetTermDisplayContext.isSelected());
		termDisplayContext.setValue(
			userSearchFacetTermDisplayContext.getUserName());

		return termDisplayContext;
	}

	protected ThemeDisplay getThemeDisplay(RenderRequest renderRequest) {
		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		return themeDisplaySupplier.getThemeDisplay();
	}

	protected Optional<String> getUserParamValue(
		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier) {

		String paramValue = ParamUtil.getString(
			originalHttpServletRequestSupplier.get(), _PARAM);

		if (paramValue.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(paramValue);
	}

	protected Optional<String> getUsers(
		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier) {

		Optional<String> paramValue = getUserParamValue(
			originalHttpServletRequestSupplier);

		return paramValue;
	}

	protected void setUsers(
		SearchContext searchContext, Optional<String> usersOptional) {

		usersOptional.ifPresent(
			userName -> searchContext.setAttribute("userName", userName));
	}

	@Reference
	protected PortletOriginalServletRequestSupplierFactory
		portletOriginalServletRequestSupplierFactory;

	@Reference
	protected PortletPreferencesLookup portletPreferencesLookup;

	@Reference
	protected PortletSharedSearchHelper portletSharedSearchHelper;

	private static final String _PARAM = "userName";

}