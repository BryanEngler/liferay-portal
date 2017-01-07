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

package com.liferay.portal.search.web.internal.portlet.facet.site;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.ScopeFacet;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.web.internal.display.context.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.facet.display.context.ScopeSearchFacetDisplayContext;
import com.liferay.portal.search.web.internal.facet.display.context.ScopeSearchFacetTermDisplayContext;
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

import java.util.ArrayList;
import java.util.List;
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
 * @author André de Oliveira
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.css-class-wrapper=" +
			SiteFacetPortletKeys.CSS_CLASS_WRAPPER,
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
			SiteFacetPortletKeys.DISPLAY_NAME,
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=" +
			SiteFacetPortletKeys.VIEW_TEMPLATE,
		"javax.portlet.name=" + SiteFacetPortletKeys.PORTLET_NAME,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = {Portlet.class, SearchAwarePortlet.class}
)
public class SiteFacetPortlet extends MVCPortlet implements SearchAwarePortlet {

	@Override
	public void contribute(
		SearchBuilder searchBuilder,
		SearchParametersBuilder searchParametersBuilder,
		RenderRequest renderRequest, String portletId,
		SearchContext searchContext) {

		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			portletOriginalServletRequestSupplierFactory.get(renderRequest);

		HttpServletRequest httpServletRequest =
			originalHttpServletRequestSupplier.get();

		setSites(searchContext, getSites(httpServletRequest));

		SiteFacetPortletPreferences siteFacetPortletPreferences =
			getPortletPreferences(renderRequest, portletId);

		searchBuilder.addFacet(
			buildFacet(siteFacetPortletPreferences, searchContext));
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletSharedSearchResult portletSharedSearchResult =
			portletSharedSearchHelper.search(renderRequest, renderResponse);

		SiteFacetPortletDisplayContext siteFacetPortletDisplayContext =
			buildDisplayContext(portletSharedSearchResult, renderRequest);

		renderRequest.setAttribute(
			SiteFacetPortletDisplayContext.ATTRIBUTE,
			siteFacetPortletDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	protected SiteFacetPortletDisplayContext buildDisplayContext(
		PortletSharedSearchResult portletSharedSearchResult,
		RenderRequest renderRequest) {

		Facet facet = portletSharedSearchResult.getFacet(
			ScopeFacetConstants.FIELD_NAME);

		String fieldParam = getFieldParam(renderRequest);
		Locale locale = getLocale(renderRequest);

		ScopeFacetConfiguration siteFacetConfiguration =
			new ScopeFacetConfigurationImpl(facet.getFacetConfiguration());

		int countThreshold = siteFacetConfiguration.getFrequencyThreshold();
		int maxTerms = siteFacetConfiguration.getMaxTerms();

		Optional<PortletPreferences> portletPreferences = getPortletPreferences(
			renderRequest);

		SiteFacetPortletPreferences siteFacetPortletConfiguration =
			new SiteFacetPortletPreferencesImpl(portletPreferences);

		boolean frequenciesVisible =
			siteFacetPortletConfiguration.isFrequenciesVisible();

		ScopeSearchFacetDisplayContext scopeSearchFacetDisplayContext =
			new ScopeSearchFacetDisplayContext(
				facet, fieldParam, locale, countThreshold, maxTerms,
				frequenciesVisible, groupLocalService);

		return buildDisplayContext(
			fieldParam, frequenciesVisible, scopeSearchFacetDisplayContext);
	}

	protected SiteFacetPortletDisplayContext buildDisplayContext(
		String fieldParam, boolean showFrequencies,
		ScopeSearchFacetDisplayContext scopeSearchFacetDisplayContext) {

		SiteFacetPortletDisplayContext siteFacetPortletDisplayContext =
			new SiteFacetPortletDisplayContext();

		List<SiteFacetPortletTermDisplayContext> termDisplayContexts =
			buildTermDisplayContexts(
				scopeSearchFacetDisplayContext.getTermDisplayContexts(),
				showFrequencies);

		siteFacetPortletDisplayContext.setTerms(termDisplayContexts);

		boolean renderNothing = termDisplayContexts.isEmpty();

		boolean nothingSelected =
			scopeSearchFacetDisplayContext.isNothingSelected();

		siteFacetPortletDisplayContext.setFieldParamInputName(_PARAM);
		siteFacetPortletDisplayContext.setFieldParamInputValue(fieldParam);
		siteFacetPortletDisplayContext.setRenderNothing(renderNothing);
		siteFacetPortletDisplayContext.setNothingSelected(nothingSelected);

		return siteFacetPortletDisplayContext;
	}

	protected ScopeFacet buildFacet(
		SiteFacetPortletPreferences siteFacetPortletPreferences,
		SearchContext searchContext) {

		ScopeFacetBuilder scopeFacetBuilder = new ScopeFacetBuilder();

		scopeFacetBuilder.setFrequencyThreshold(
			siteFacetPortletPreferences.getFrequencyThreshold());
		scopeFacetBuilder.setMaxTerms(
			siteFacetPortletPreferences.getMaxTerms());
		scopeFacetBuilder.setSearchContext(searchContext);

		return scopeFacetBuilder.build();
	}

	protected List<SiteFacetPortletTermDisplayContext> buildTermDisplayContexts(
		List<ScopeSearchFacetTermDisplayContext>
			scopeSearchFacetTermDisplayContexts,
		boolean showCounts) {

		List<SiteFacetPortletTermDisplayContext> termDisplayContexts =
			new ArrayList<>();

		for (ScopeSearchFacetTermDisplayContext
				scopeSearchFacetTermDisplayContext :
					scopeSearchFacetTermDisplayContexts) {

			SiteFacetPortletTermDisplayContext termDisplayContext =
				getTermDisplayContext(
					showCounts, scopeSearchFacetTermDisplayContext);

			termDisplayContexts.add(termDisplayContext);
		}

		return termDisplayContexts;
	}

	protected String getDescriptiveName(
		ScopeSearchFacetTermDisplayContext scopeSearchFacetTermDisplayContext) {

		try {
			return scopeSearchFacetTermDisplayContext.getDescriptiveName();
		}
		catch (PortalException pe) {
			throw new RuntimeException(pe);
		}
	}

	protected String getFieldParam(RenderRequest renderRequest) {
		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			portletOriginalServletRequestSupplierFactory.get(renderRequest);

		Optional<String> paramValue = getSiteParamValue(
			originalHttpServletRequestSupplier.get());

		return paramValue.orElse(StringPool.BLANK);
	}

	protected Locale getLocale(RenderRequest renderRequest) {
		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		return themeDisplay.getLocale();
	}

	protected Optional<PortletPreferences> getPortletPreferences(
		RenderRequest renderRequest) {

		return Optional.ofNullable(renderRequest.getPreferences());
	}

	protected SiteFacetPortletPreferences getPortletPreferences(
		RenderRequest renderRequest, String portletId) {

		return new SiteFacetPortletPreferencesImpl(
			portletPreferencesLookup.getPortletPreferences(
				renderRequest, portletId));
	}

	protected Optional<String> getSiteParamValue(
		HttpServletRequest httpServletRequest) {

		return StringUtil.trimOpt(
			ParamUtil.getString(httpServletRequest, _PARAM));
	}

	protected Optional<long[]> getSites(HttpServletRequest httpServletRequest) {
		Optional<String> paramValue = getSiteParamValue(httpServletRequest);

		return paramValue.map(value -> {
			String[] groupIds = StringUtil.split(value);

			List<Long> longValues = new ArrayList<>();

			for (String groupId : groupIds) {
				longValues.add(Long.valueOf(groupId));
			}

			return ArrayUtil.toLongArray(longValues);
		});
	}

	protected SiteFacetPortletTermDisplayContext getTermDisplayContext(
		boolean showCounts,
		ScopeSearchFacetTermDisplayContext scopeSearchFacetTermDisplayContext) {

		SiteFacetPortletTermDisplayContext termDisplayContext =
			new SiteFacetPortletTermDisplayContext();

		termDisplayContext.setTerm(
			getDescriptiveName(scopeSearchFacetTermDisplayContext));
		termDisplayContext.setFrequency(
			scopeSearchFacetTermDisplayContext.getCount());
		termDisplayContext.setFrequencyVisible(showCounts);
		termDisplayContext.setSelected(
			scopeSearchFacetTermDisplayContext.isSelected());
		termDisplayContext.setValue(
			String.valueOf(scopeSearchFacetTermDisplayContext.getGroupId()));

		return termDisplayContext;
	}

	protected ThemeDisplay getThemeDisplay(RenderRequest renderRequest) {
		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		return themeDisplaySupplier.getThemeDisplay();
	}

	protected void setSites(
		SearchContext searchContext, Optional<long[]> sitesOptional) {

		sitesOptional.ifPresent(sites -> searchContext.setGroupIds(sites));
	}

	@Reference
	protected GroupLocalService groupLocalService;

	@Reference
	protected PortletOriginalServletRequestSupplierFactory
		portletOriginalServletRequestSupplierFactory;

	@Reference
	protected PortletPreferencesLookup portletPreferencesLookup;

	@Reference
	protected PortletSharedSearchHelper portletSharedSearchHelper;

	private static final String _PARAM = "site";

}