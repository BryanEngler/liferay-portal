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

package com.liferay.portal.search.web.internal.portlet.facet.category;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.internal.display.context.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.facet.display.builder.AssetCategoriesSearchFacetDisplayBuilder;
import com.liferay.portal.search.web.internal.facet.display.context.AssetCategoriesSearchFacetDisplayContext;
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
			CategoryFacetPortletKeys.CSS_CLASS_WRAPPER,
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
			CategoryFacetPortletKeys.DISPLAY_NAME,
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=" +
			CategoryFacetPortletKeys.VIEW_TEMPLATE,
		"javax.portlet.name=" + CategoryFacetPortletKeys.PORTLET_NAME,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = {Portlet.class, SearchAwarePortlet.class}
)
public class CategoryFacetPortlet
	extends MVCPortlet implements SearchAwarePortlet {

	@Override
	public void contribute(
		SearchBuilder searchBuilder,
		SearchParametersBuilder searchParametersBuilder,
		RenderRequest renderRequest, String portletId,
		SearchContext searchContext) {

		CategoryFacetPortletPreferences categoryFacetPortletPreferences =
			getPortletPreferences(renderRequest, portletId);

		String paramName = categoryFacetPortletPreferences.getParamName();

		Optional<String> paramValue = getAssetCategoriesParamValue(
			renderRequest, paramName);

		setAssetCategories(searchContext, paramValue);

		searchBuilder.addFacet(
			buildFacet(categoryFacetPortletPreferences, searchContext));
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletSharedSearchResult portletSharedSearchResult =
			portletSharedSearchHelper.search(renderRequest, renderResponse);

		AssetCategoriesSearchFacetDisplayContext
			assetCategoriesSearchFacetDisplayContext = buildDisplayContext(
				portletSharedSearchResult, renderRequest);

		renderRequest.setAttribute(
			AssetCategoriesSearchFacetDisplayContext.ATTRIBUTE,
			assetCategoriesSearchFacetDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	protected AssetCategoriesSearchFacetDisplayContext buildDisplayContext(
		PortletSharedSearchResult result, RenderRequest renderRequest) {

		Facet facet = result.getFacet(AssetCategoriesFacetConstants.FIELD_NAME);

		AssetCategoriesFacetConfiguration assetCategoriesFacetConfiguration =
			new AssetCategoriesFacetConfigurationImpl(
				facet.getFacetConfiguration());

		int maxTerms = assetCategoriesFacetConfiguration.getMaxTerms();
		int frequencyThreshold =
			assetCategoriesFacetConfiguration.getFrequencyThreshold();

		Optional<PortletPreferences> portletPreferences = getPortletPreferences(
			renderRequest);

		CategoryFacetPortletPreferences categoryFacetPortletPreferences =
			new CategoryFacetPortletPreferencesImpl(portletPreferences);

		boolean frequenciesVisible =
			categoryFacetPortletPreferences.isFrequenciesVisible();
		String displayStyle = categoryFacetPortletPreferences.getDisplayStyle();

		String paramName = categoryFacetPortletPreferences.getParamName();

		String fieldParam = getFieldParam(renderRequest, paramName);

		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		Locale locale = themeDisplay.getLocale();

		AssetCategoriesSearchFacetDisplayBuilder
			assetCategoriesSearchFacetDisplayBuilder =
				new AssetCategoriesSearchFacetDisplayBuilder();

		assetCategoriesSearchFacetDisplayBuilder.setFacet(facet);
		assetCategoriesSearchFacetDisplayBuilder.setPermissionChecker(
			themeDisplay.getPermissionChecker());
		assetCategoriesSearchFacetDisplayBuilder.setParamName(paramName);
		assetCategoriesSearchFacetDisplayBuilder.setParamValue(fieldParam);
		assetCategoriesSearchFacetDisplayBuilder.setLocale(locale);
		assetCategoriesSearchFacetDisplayBuilder.setDisplayStyle(displayStyle);
		assetCategoriesSearchFacetDisplayBuilder.setFrequencyThreshold(
			frequencyThreshold);
		assetCategoriesSearchFacetDisplayBuilder.setMaxTerms(maxTerms);
		assetCategoriesSearchFacetDisplayBuilder.setFrequenciesVisible(
			frequenciesVisible);

		try {
			return assetCategoriesSearchFacetDisplayBuilder.build();
		}
		catch (PortalException pe) {
			throw new RuntimeException(pe);
		}
	}

	protected MultiValueFacet buildFacet(
		CategoryFacetPortletPreferences categoryFacetPortletPreferences,
		SearchContext searchContext) {

		AssetCategoriesFacetBuilder assetCategoriesFacetBuilder =
			new AssetCategoriesFacetBuilder();

		assetCategoriesFacetBuilder.setFrequencyThreshold(
			categoryFacetPortletPreferences.getFrequencyThreshold());
		assetCategoriesFacetBuilder.setMaxTerms(
			categoryFacetPortletPreferences.getMaxTerms());
		assetCategoriesFacetBuilder.setSearchContext(searchContext);

		return assetCategoriesFacetBuilder.build();
	}

	protected Optional<long[]> getAssetCategories(Optional<String> paramValue) {
		Optional<Long> map = paramValue.map(Long::valueOf);

		Optional<long[]> map2 = map.map(l -> new long[] {l});

		return map2;
	}

	protected Optional<String> getAssetCategoriesParamValue(
		RenderRequest renderRequest, String paramName) {

		return StringUtil.trimOpt(
			ParamUtil.getString(getRequest(renderRequest), paramName));
	}

	protected String getFieldParam(
		RenderRequest renderRequest, String paramName) {

		Optional<String> paramValue = getAssetCategoriesParamValue(
			renderRequest, paramName);

		return paramValue.orElse(StringPool.BLANK);
	}

	protected Optional<PortletPreferences> getPortletPreferences(
		RenderRequest renderRequest) {

		return Optional.ofNullable(renderRequest.getPreferences());
	}

	protected CategoryFacetPortletPreferences getPortletPreferences(
		RenderRequest renderRequest, String portletId) {

		return new CategoryFacetPortletPreferencesImpl(
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

	protected void setAssetCategories(
		SearchContext searchContext, Optional<String> paramValue) {

		paramValue.ifPresent(
			assetCategoryId -> {
				searchContext.setAssetCategoryIds(
					new long[] {Long.valueOf(assetCategoryId)});
				searchContext.setAttribute(
					AssetCategoriesFacetConstants.FIELD_NAME, assetCategoryId);
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