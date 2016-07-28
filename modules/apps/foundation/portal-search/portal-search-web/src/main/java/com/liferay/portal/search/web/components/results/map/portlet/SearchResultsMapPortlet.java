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

package com.liferay.portal.search.web.components.results.map.portlet;

import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.blogs.service.BlogsEntryLocalService;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.security.permission.ResourceActions;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.search.web.internal.display.context.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.portlet.results.SearchResultsSummariesBuilder;
import com.liferay.portal.search.web.internal.portlet.results.SearchResultsSummariesHolder;
import com.liferay.portal.search.web.internal.request.helper.PortletOriginalServletRequestSupplierFactory;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchHelper;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResult;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

import java.io.IOException;

import java.util.Locale;

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
			SearchResultsMapPortletKeys.CSS_CLASS_WRAPPER,
		"com.liferay.portlet.display-category=category.search",
		"com.liferay.portlet.header-portlet-css=/components/results/map/css/main.css",
		"com.liferay.portlet.icon=/icons/search.png",
		"com.liferay.portlet.instanceable=true",
		"com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.restore-current-view=false",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=" +
			SearchResultsMapPortletKeys.DISPLAY_NAME,
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=" +
			SearchResultsMapPortletKeys.VIEW_TEMPLATE,
		"javax.portlet.name=" + SearchResultsMapPortletKeys.PORTLET_NAME,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = Portlet.class
)
public class SearchResultsMapPortlet extends MVCPortlet {

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletSharedSearchResult portletSharedSearchResult =
			portletSharedSearchHelper.search(renderRequest, renderResponse);

		renderRequest.setAttribute(
			SearchResultsMapDisplayContext.ATTRIBUTE,
			buildDisplayContext(
				portletSharedSearchResult, renderRequest, renderResponse));

		super.render(renderRequest, renderResponse);
	}

	protected SearchResultsMapDisplayContext buildDisplayContext(
			PortletSharedSearchResult portletSharedSearchResult,
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		SearchResultsData searchResultsData =
			portletSharedSearchResult.getSearchResultsData();

		String keywords = searchResultsData.getKeywords();

		String mapMarkersJSON = buildMapMarkers(
			searchResultsData, renderRequest);

		SearchResultsMapDisplayContext searchResultsMapDisplayContext =
			new SearchResultsMapDisplayContext(
				searchResultsData, keywords, mapMarkersJSON);

		searchResultsMapDisplayContext.setSearchResultsSummariesHolder(
			buildSummaries(searchResultsData, renderRequest, renderResponse));

		return searchResultsMapDisplayContext;
	}

	protected String buildMapMarkers(
		SearchResultsData searchResultsData, RenderRequest renderRequest) {

		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		Locale locale = themeDisplay.getLocale();

		MapMarkersExtendedBuilder mapMarkersExtendedBuilder =
			new MapMarkersExtendedBuilder(locale, resourceActions);

		return mapMarkersExtendedBuilder.buildMapMarkersJSON(
			searchResultsData.getDocuments());
	}

	protected SearchResultsSummariesHolder buildSummaries(
			SearchResultsData searchResultsData, RenderRequest renderRequest,
			RenderResponse renderResponse)
		throws PortletException {

		SearchResultsSummariesBuilder searchResultsSummariesBuilder =
			new SearchResultsSummariesBuilder(
				searchResultsData, renderRequest, renderResponse,
				assetEntryLocalService, blogsEntryLocalService, language,
				resourceActions);

		try {
			return searchResultsSummariesBuilder.build();
		}
		catch (PortletException pe) {
			throw pe;
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new PortletException(e);
		}
	}

	protected ThemeDisplay getThemeDisplay(RenderRequest renderRequest) {
		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		return themeDisplaySupplier.getThemeDisplay();
	}

	@Reference
	protected AssetEntryLocalService assetEntryLocalService;

	@Reference
	protected BlogsEntryLocalService blogsEntryLocalService;

	@Reference
	protected Language language;

	@Reference
	protected PortletOriginalServletRequestSupplierFactory
		portletOriginalServletRequestSupplierFactory;

	@Reference
	protected PortletSharedSearchHelper portletSharedSearchHelper;

	@Reference
	protected ResourceActions resourceActions;

}