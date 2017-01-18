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

package com.liferay.portal.search.web.internal.modified.facet.portlet;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.ModifiedFacetFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.internal.display.context.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.modified.facet.builder.ModifiedFacetBuilder;
import com.liferay.portal.search.web.internal.modified.facet.builder.ModifiedFacetConfiguration;
import com.liferay.portal.search.web.internal.modified.facet.builder.ModifiedFacetConfigurationImpl;
import com.liferay.portal.search.web.internal.modified.facet.constants.ModifiedFacetPortletKeys;
import com.liferay.portal.search.web.internal.modified.facet.display.context.ModifiedFacetDisplayBuilder;
import com.liferay.portal.search.web.internal.modified.facet.display.context.ModifiedFacetDisplayContext;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchContributor;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchRequest;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchResponse;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchSettings;

import java.io.IOException;

import java.util.Optional;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
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
		"com.liferay.portlet.css-class-wrapper=portlet-modified-facet",
		"com.liferay.portlet.display-category=category.search",
		"com.liferay.portlet.icon=/icons/search.png",
		"com.liferay.portlet.instanceable=true",
		"com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.restore-current-view=false",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=Last Modified Facet",
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/modified/facet/view.jsp",
		"javax.portlet.name=" + ModifiedFacetPortletKeys.MODIFIED_FACET,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = {Portlet.class, PortletSharedSearchContributor.class}
)
public class ModifiedFacetPortlet
	extends MVCPortlet implements PortletSharedSearchContributor {

	@Override
	public void contribute(
		PortletSharedSearchSettings portletSharedSearchSettings) {

		ModifiedFacetPortletPreferences modifiedFacetPortletPreferences =
			new ModifiedFacetPortletPreferencesImpl(
				portletSharedSearchSettings.getPortletPreferences());

		Facet facet = buildFacet(
			modifiedFacetPortletPreferences, portletSharedSearchSettings);

		portletSharedSearchSettings.addFacet(facet);
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletSharedSearchResponse portletSharedSearchResponse =
			portletSharedSearchRequest.search(renderRequest);

		ModifiedFacetDisplayContext modifiedSearchFacetDisplayContext =
			buildDisplayContext(portletSharedSearchResponse, renderRequest);

		renderRequest.setAttribute(
			WebKeys.PORTLET_DISPLAY_CONTEXT, modifiedSearchFacetDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	protected ModifiedFacetDisplayContext buildDisplayContext(
		PortletSharedSearchResponse portletSharedSearchResponse,
		RenderRequest renderRequest) {

		Facet facet = portletSharedSearchResponse.getFacet(getFieldName());

		ModifiedFacetPortletPreferences modifiedFacetPortletPreferences =
			new ModifiedFacetPortletPreferencesImpl(
				portletSharedSearchResponse.getPortletPreferences(
					renderRequest));

		ModifiedFacetConfiguration modifiedFacetConfiguration =
			new ModifiedFacetConfigurationImpl(facet.getFacetConfiguration());

		JSONArray rangesJSONArray =
			modifiedFacetConfiguration.getRangesJSONArray();

		modifiedFacetPortletPreferences.updateRangeLabels(rangesJSONArray);

		String parameterName =
			modifiedFacetPortletPreferences.getParameterName();

		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		String escapedParameterName = HtmlUtil.escapeJS(parameterName);

		int fromDay = ParamUtil.getInteger(
			renderRequest, escapedParameterName + "dayFrom");
		int fromMonth = ParamUtil.getInteger(
			renderRequest, escapedParameterName + "monthFrom");
		int fromYear = ParamUtil.getInteger(
			renderRequest, escapedParameterName + "yearFrom");

		int toDay = ParamUtil.getInteger(
			renderRequest, escapedParameterName + "dayTo");
		int toMonth = ParamUtil.getInteger(
			renderRequest, escapedParameterName + "monthTo");
		int toYear = ParamUtil.getInteger(
			renderRequest, escapedParameterName + "yearTo");

		ModifiedFacetDisplayBuilder modifiedSearchFacetDisplayBuilder =
			new ModifiedFacetDisplayBuilder();

		modifiedSearchFacetDisplayBuilder.setFacet(facet);
		modifiedSearchFacetDisplayBuilder.setRangesJSONArray(rangesJSONArray);
		modifiedSearchFacetDisplayBuilder.setTimeZone(
			themeDisplay.getTimeZone());
		modifiedSearchFacetDisplayBuilder.setLocale(themeDisplay.getLocale());

		modifiedSearchFacetDisplayBuilder.setParameterName(parameterName);

		Optional<String[]> parameterValuesOptional =
			portletSharedSearchResponse.getParameterValues(
				parameterName, renderRequest);

		parameterValuesOptional.ifPresent(
			modifiedSearchFacetDisplayBuilder::setParameterValues);

		return modifiedSearchFacetDisplayBuilder.build();
	}

	protected Facet buildFacet(
		ModifiedFacetPortletPreferences modifiedFacetPortletPreferences,
		PortletSharedSearchSettings portletSharedSearchSettings) {

		ModifiedFacetBuilder modifiedFacetBuilder = new ModifiedFacetBuilder(
			modifiedFacetFactory);

		modifiedFacetBuilder.setSearchContext(
			portletSharedSearchSettings.getSearchContext());

		Optional<String[]> parameterValuesOptional =
			portletSharedSearchSettings.getParameterValues(
				modifiedFacetPortletPreferences.getParameterName());

		parameterValuesOptional.ifPresent(
			modifiedFacetBuilder::setSelectedRanges);

		return modifiedFacetBuilder.build();
	}

	protected String getFieldName() {
		Facet facet = modifiedFacetFactory.newInstance(new SearchContext());

		return facet.getFieldName();
	}

	protected ModifiedFacetPortletPreferencesImpl getPortletPreferences(
		RenderRequest renderRequest) {

		return new ModifiedFacetPortletPreferencesImpl(
			Optional.ofNullable(renderRequest.getPreferences()));
	}

	protected ThemeDisplay getThemeDisplay(RenderRequest renderRequest) {
		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		return themeDisplaySupplier.getThemeDisplay();
	}

	@Reference
	protected ModifiedFacetFactory modifiedFacetFactory;

	@Reference
	protected PortletSharedSearchRequest portletSharedSearchRequest;

}