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
import com.liferay.portal.kernel.service.PortletPreferencesLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.internal.display.context.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.facet.display.context.ScopeSearchFacetDisplayContext;
import com.liferay.portal.search.web.internal.facet.display.context.ScopeSearchFacetTermDisplayContext;
import com.liferay.portal.search.web.internal.request.helper.OriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortletOriginalServletRequestSupplierFactory;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchHelper;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResult;
import com.liferay.portal.search.web.internal.request.params.SearchParametersConfiguration;
import com.liferay.portal.search.web.portlet.SearchAwarePortlet;
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
		SearchBuilder searchBuilder, RenderRequest renderRequest,
		String portletId, SearchContext searchContext) {

		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			portletOriginalServletRequestSupplierFactory.get(renderRequest);

		setSites(searchContext, getSites(originalHttpServletRequestSupplier));

		SiteFacetPortletPreferences siteFacetPortletPreferences =
			new SiteFacetPortletPreferencesImpl(
				getPortletPreferences(renderRequest, portletId));

		searchBuilder.addFacet(
			buildFacet(siteFacetPortletPreferences, searchContext));
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		// TODO Portlet configuration

		SearchParametersConfiguration searchParametersConfiguration = () -> "q";

		PortletSharedSearchResult result = portletSharedSearchHelper.search(
			renderRequest, renderResponse, searchParametersConfiguration);

		SiteFacetPortletDisplayContext siteFacetPortletDisplayContext =
			buildDisplayContext(renderRequest, result);

		renderRequest.setAttribute(
			SiteFacetPortletDisplayContext.ATTRIBUTE,
			siteFacetPortletDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	protected SiteFacetPortletDisplayContext buildDisplayContext(
		RenderRequest renderRequest, PortletSharedSearchResult result) {

		Facet facet = result.getFacet(ScopeFacetConstants.FIELD_NAME);

		String fieldParam = getFieldParam(renderRequest);
		Locale locale = getLocale(renderRequest);

		ScopeFacetConfiguration siteFacetConfiguration =
			new ScopeFacetConfigurationImpl(facet.getFacetConfiguration());

		int countThreshold = siteFacetConfiguration.getFrequencyThreshold();
		int maxTerms = siteFacetConfiguration.getMaxTerms();

		PortletPreferences portletPreferences = renderRequest.getPreferences();

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
			originalHttpServletRequestSupplier);

		return paramValue.orElse(StringPool.BLANK);
	}

	protected Locale getLocale(RenderRequest renderRequest) {
		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		return themeDisplay.getLocale();
	}

	protected PortletPreferences getPortletPreferences(
		RenderRequest renderRequest, String portletId) {

		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		return portletPreferencesLocalService.fetchPreferences(
			themeDisplay.getCompanyId(), PortletKeys.PREFS_OWNER_ID_DEFAULT,
			PortletKeys.PREFS_OWNER_TYPE_LAYOUT, themeDisplay.getPlid(),
			portletId);
	}

	protected Optional<String> getSiteParamValue(
		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier) {

		String paramValue = ParamUtil.getString(
			originalHttpServletRequestSupplier.get(), _PARAM);

		if (paramValue.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(paramValue);
	}

	protected Optional<long[]> getSites(
		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier) {

		Optional<String> paramValue = getSiteParamValue(
			originalHttpServletRequestSupplier);

		Optional<Long> map = paramValue.map(Long::valueOf);

		Optional<long[]> map2 = map.map(l -> new long[] {l});

		return map2;
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
	protected PortletPreferencesLocalService portletPreferencesLocalService;

	@Reference
	protected PortletSharedSearchHelper portletSharedSearchHelper;

	private static final String _PARAM = "site";

}