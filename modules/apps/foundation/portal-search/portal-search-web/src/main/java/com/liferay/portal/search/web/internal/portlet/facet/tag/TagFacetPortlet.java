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

package com.liferay.portal.search.web.internal.portlet.facet.tag;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.internal.display.context.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.facet.display.builder.AssetTagsSearchFacetDisplayBuilder;
import com.liferay.portal.search.web.internal.facet.display.context.AssetTagsSearchFacetDisplayContext;
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
			TagFacetPortletKeys.CSS_CLASS_WRAPPER,
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
			TagFacetPortletKeys.DISPLAY_NAME,
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=" +
			TagFacetPortletKeys.VIEW_TEMPLATE,
		"javax.portlet.name=" + TagFacetPortletKeys.PORTLET_NAME,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = {Portlet.class, SearchAwarePortlet.class}
)
public class TagFacetPortlet extends MVCPortlet implements SearchAwarePortlet {

	@Override
	public void contribute(
		SearchBuilder searchBuilder,
		SearchParametersBuilder searchParametersBuilder,
		RenderRequest renderRequest, String portletId,
		SearchContext searchContext) {

		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			portletOriginalServletRequestSupplierFactory.get(renderRequest);

		TagFacetPortletPreferences tagFacetPortletPreferences =
			getPortletPreferences(renderRequest, portletId);

		String paramName = _PARAM;

		Optional<String> paramValue = getTagParamValue(
			originalHttpServletRequestSupplier.get(), paramName);

		setTags(searchContext, paramValue);

		searchBuilder.addFacet(
			buildFacet(tagFacetPortletPreferences, searchContext));
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletSharedSearchResult portletSharedSearchResult =
			portletSharedSearchHelper.search(renderRequest, renderResponse);

		AssetTagsSearchFacetDisplayContext assetTagsSearchFacetDisplayContext =
			buildDisplayContext(portletSharedSearchResult, renderRequest);

		renderRequest.setAttribute(
			AssetTagsSearchFacetDisplayContext.ATTRIBUTE,
			assetTagsSearchFacetDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	protected AssetTagsSearchFacetDisplayContext buildDisplayContext(
		PortletSharedSearchResult portletSharedSearchResult,
		RenderRequest renderRequest) {

		Facet facet = portletSharedSearchResult.getFacet(
			AssetTagsFacetConstants.FIELD_NAME);

		String paramName = _PARAM;

		String fieldParam = getFieldParam(renderRequest, paramName);

		AssetTagsFacetConfiguration assetTagsFacetConfiguration =
			new AssetTagsFacetConfigurationImpl(facet.getFacetConfiguration());

		int frequencyThreshold =
			assetTagsFacetConfiguration.getFrequencyThreshold();
		int maxTerms = assetTagsFacetConfiguration.getMaxTerms();

		Optional<PortletPreferences> portletPreferences = getPortletPreferences(
			renderRequest);

		TagFacetPortletPreferences assetTagsFacetPortletConfiguration =
			new TagFacetPortletPreferencesImpl(portletPreferences);

		boolean frequenciesVisible =
			assetTagsFacetPortletConfiguration.isFrequenciesVisible();
		String displayStyle =
			assetTagsFacetPortletConfiguration.getDisplayStyle();

		AssetTagsSearchFacetDisplayBuilder assetTagsSearchFacetDisplayBuilder =
			new AssetTagsSearchFacetDisplayBuilder();

		assetTagsSearchFacetDisplayBuilder.setDisplayStyle(displayStyle);
		assetTagsSearchFacetDisplayBuilder.setFacet(facet);
		assetTagsSearchFacetDisplayBuilder.setFrequenciesVisible(
			frequenciesVisible);
		assetTagsSearchFacetDisplayBuilder.setFrequencyThreshold(
			frequencyThreshold);
		assetTagsSearchFacetDisplayBuilder.setMaxTerms(maxTerms);
		assetTagsSearchFacetDisplayBuilder.setParamName(_PARAM);
		assetTagsSearchFacetDisplayBuilder.setParamValue(fieldParam);

		AssetTagsSearchFacetDisplayContext assetTagsSearchFacetDisplayContext =
			assetTagsSearchFacetDisplayBuilder.build();

		return assetTagsSearchFacetDisplayContext;
	}

	protected MultiValueFacet buildFacet(
		TagFacetPortletPreferences tagFacetPortletPreferences,
		SearchContext searchContext) {

		AssetTagsFacetBuilder assetTagsFacetBuilder =
			new AssetTagsFacetBuilder();

		assetTagsFacetBuilder.setFrequencyThreshold(
			tagFacetPortletPreferences.getFrequencyThreshold());
		assetTagsFacetBuilder.setMaxTerms(
			tagFacetPortletPreferences.getMaxTerms());
		assetTagsFacetBuilder.setSearchContext(searchContext);

		return assetTagsFacetBuilder.build();
	}

	protected String getFieldParam(
		RenderRequest renderRequest, String paramName) {

		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			portletOriginalServletRequestSupplierFactory.get(renderRequest);

		Optional<String> paramValue = getTagParamValue(
			originalHttpServletRequestSupplier.get(), paramName);

		return paramValue.orElse(StringPool.BLANK);
	}

	protected Optional<PortletPreferences> getPortletPreferences(
		RenderRequest renderRequest) {

		return Optional.ofNullable(renderRequest.getPreferences());
	}

	protected TagFacetPortletPreferences getPortletPreferences(
		RenderRequest renderRequest, String portletId) {

		return new TagFacetPortletPreferencesImpl(
			portletPreferencesLookup.getPortletPreferences(
				renderRequest, portletId));
	}

	protected Optional<String> getTagParamValue(
		HttpServletRequest httpServletRequest, String paramName) {

		return StringUtil.trimOpt(
			ParamUtil.getString(httpServletRequest, paramName));
	}

	protected ThemeDisplay getThemeDisplay(RenderRequest renderRequest) {
		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		return themeDisplaySupplier.getThemeDisplay();
	}

	protected void setTags(
		SearchContext searchContext, Optional<String> paramValue) {

		paramValue.ifPresent(
			tag -> {
				searchContext.setAssetTagNames(new String[] {tag});
				searchContext.setAttribute("assetTagNames", tag);
			});
	}

	@Reference
	protected PortletOriginalServletRequestSupplierFactory
		portletOriginalServletRequestSupplierFactory;

	@Reference
	protected PortletPreferencesLookup portletPreferencesLookup;

	@Reference
	protected PortletSharedSearchHelper portletSharedSearchHelper;

	private static final String _PARAM = "tag";

}