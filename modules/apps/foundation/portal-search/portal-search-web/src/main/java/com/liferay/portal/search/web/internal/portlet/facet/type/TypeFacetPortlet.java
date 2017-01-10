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

package com.liferay.portal.search.web.internal.portlet.facet.type;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacet;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.internal.display.context.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.facet.display.builder.AssetEntriesSearchFacetDisplayBuilder;
import com.liferay.portal.search.web.internal.facet.display.context.AssetEntriesSearchFacetDisplayContext;
import com.liferay.portal.search.web.internal.preferences.PortletPreferencesLookup;
import com.liferay.portal.search.web.internal.request.helper.OriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortletOriginalServletRequestSupplierFactory;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchHelper;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResult;
import com.liferay.portal.search.web.internal.util.StringUtil;
import com.liferay.portal.search.web.portlet.SearchAwarePortlet;
import com.liferay.portal.search.web.portlet.SearchParametersBuilder;
import com.liferay.portal.search.web.search.builder.SearchBuilder;

import java.io.IOException;

import java.util.Locale;
import java.util.Optional;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

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
			TypeFacetPortletKeys.CSS_CLASS_WRAPPER,
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
			TypeFacetPortletKeys.DISPLAY_NAME,
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=" +
			TypeFacetPortletKeys.VIEW_TEMPLATE,
		"javax.portlet.name=" + TypeFacetPortletKeys.PORTLET_NAME,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = {Portlet.class, SearchAwarePortlet.class}
)
public class TypeFacetPortlet extends MVCPortlet implements SearchAwarePortlet {

	@Override
	public void contribute(
		SearchBuilder searchBuilder,
		SearchParametersBuilder searchParametersBuilder,
		RenderRequest renderRequest, String portletId,
		SearchContext searchContext) {

		TypeFacetPortletPreferences typeFacetPortletPreferences =
			getPortletPreferences(renderRequest, portletId);

		String paramName = typeFacetPortletPreferences.getParamName();

		Optional<String> paramValue = getTypeParamValue(
			renderRequest, paramName);

		setTypes(searchContext, paramValue);

		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		searchBuilder.addFacet(
			buildFacet(
				typeFacetPortletPreferences, themeDisplay.getCompanyId(),
				searchContext));
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletSharedSearchResult portletSharedSearchResult =
			portletSharedSearchHelper.search(renderRequest, renderResponse);

		AssetEntriesSearchFacetDisplayContext
			assetEntriesSearchFacetDisplayContext = buildDisplayContext(
				portletSharedSearchResult, renderRequest);

		renderRequest.setAttribute(
			AssetEntriesSearchFacetDisplayContext.ATTRIBUTE,
			assetEntriesSearchFacetDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	protected AssetEntriesSearchFacetDisplayContext buildDisplayContext(
		PortletSharedSearchResult portletSharedSearchResult,
		RenderRequest renderRequest) {

		Facet facet = portletSharedSearchResult.getFacet(
			AssetEntriesFacetConstants.FIELD_NAME);

		AssetEntriesFacetConfiguration assetEntriesFacetConfiguration =
			new AssetEntriesFacetConfigurationImpl(
				facet.getFacetConfiguration());

		int frequencyThreshold =
			assetEntriesFacetConfiguration.getFrequencyThreshold();

		Optional<PortletPreferences> portletPreferences = getPortletPreferences(
			renderRequest);

		TypeFacetPortletPreferences typeFacetPortletPreferences =
			new TypeFacetPortletPreferencesImpl(portletPreferences);

		boolean frequenciesVisible =
			typeFacetPortletPreferences.isFrequenciesVisible();

		String paramName = typeFacetPortletPreferences.getParamName();

		String fieldParam = getFieldParam(renderRequest, paramName);

		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		Locale locale = themeDisplay.getLocale();

		Optional<String[]> assetTypesArray =
			typeFacetPortletPreferences.getAssetTypesArray();

		String[] classNames = assetTypesArray.orElse(
			assetEntriesFacetConfiguration.getClassNames());

		AssetEntriesSearchFacetDisplayBuilder
			assetEntriesSearchFacetDisplayBuilder =
				new AssetEntriesSearchFacetDisplayBuilder();

		assetEntriesSearchFacetDisplayBuilder.setClassNames(classNames);
		assetEntriesSearchFacetDisplayBuilder.setFacet(facet);
		assetEntriesSearchFacetDisplayBuilder.setFrequencyThreshold(
			frequencyThreshold);
		assetEntriesSearchFacetDisplayBuilder.setFrequenciesVisible(
			frequenciesVisible);
		assetEntriesSearchFacetDisplayBuilder.setLocale(locale);
		assetEntriesSearchFacetDisplayBuilder.setParamName(paramName);
		assetEntriesSearchFacetDisplayBuilder.setParamValue(fieldParam);
		assetEntriesSearchFacetDisplayBuilder.setPermissionChecker(
			themeDisplay.getPermissionChecker());

		AssetEntriesSearchFacetDisplayContext
			assetEntriesSearchFacetDisplayContext =
				assetEntriesSearchFacetDisplayBuilder.build();

		return assetEntriesSearchFacetDisplayContext;
	}

	protected AssetEntriesFacet buildFacet(
		TypeFacetPortletPreferences typeFacetPortletPreferences, long companyId,
		SearchContext searchContext) {

		AssetEntriesFacetBuilder assetEntriesFacetBuilder =
			new AssetEntriesFacetBuilder();

		assetEntriesFacetBuilder.setCompanyId(companyId);
		assetEntriesFacetBuilder.setFrequencyThreshold(
			typeFacetPortletPreferences.getFrequencyThreshold());
		assetEntriesFacetBuilder.setSearchContext(searchContext);

		return assetEntriesFacetBuilder.build();
	}

	protected String getFieldParam(
		RenderRequest renderRequest, String paramName) {

		Optional<String> paramValue = getTypeParamValue(
			renderRequest, paramName);

		return paramValue.orElse(StringPool.BLANK);
	}

	protected Optional<PortletPreferences> getPortletPreferences(
		RenderRequest renderRequest) {

		return Optional.ofNullable(renderRequest.getPreferences());
	}

	protected TypeFacetPortletPreferences getPortletPreferences(
		RenderRequest renderRequest, String portletId) {

		return new TypeFacetPortletPreferencesImpl(
			portletPreferencesLookup.getPortletPreferences(
				renderRequest, portletId));
	}

	protected HttpServletRequest getRequest(RenderRequest renderRequest) {
		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			portletOriginalServletRequestSupplierFactory.get(renderRequest);

		return originalHttpServletRequestSupplier.get();
	}

	protected ThemeDisplay getThemeDisplay(RenderRequest renderRequest) {
		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		return themeDisplaySupplier.getThemeDisplay();
	}

	protected Optional<String> getTypeParamValue(
		RenderRequest renderRequest, String paramName) {

		return StringUtil.trimOpt(
			ParamUtil.getString(getRequest(renderRequest), paramName));
	}

	protected void setTypes(
		SearchContext searchContext, Optional<String> paramValue) {

		paramValue.ifPresent(
			type -> {
				searchContext.setAttribute(
					AssetEntriesFacetConstants.FIELD_NAME, type);
				searchContext.setEntryClassNames(new String[] {type});
			});
	}

	@Reference
	protected PortletOriginalServletRequestSupplierFactory
		portletOriginalServletRequestSupplierFactory;

	@Reference
	protected PortletPreferencesLookup portletPreferencesLookup;

	@Reference
	protected PortletSharedSearchHelper portletSharedSearchHelper;

}