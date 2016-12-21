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

package com.liferay.portal.search.web.internal.search.insights.portlet;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchHelper;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResult;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

import java.io.IOException;

import java.util.ResourceBundle;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.css-class-wrapper=" +
			SearchInsightsPortletKeys.CSS_CLASS_WRAPPER,
		"com.liferay.portlet.display-category=category.search-poc",
		"com.liferay.portlet.icon=/icons/search.png",
		"com.liferay.portlet.instanceable=true",
		"com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.restore-current-view=false",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=" +
			SearchInsightsPortletKeys.DISPLAY_NAME,
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=" +
			SearchInsightsPortletKeys.VIEW_TEMPLATE,
		"javax.portlet.name=" + SearchInsightsPortletKeys.PORTLET_NAME,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = Portlet.class
)
public class SearchInsightsPortlet extends MVCPortlet {

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		SearchInsightsConfigurationImpl searchInsightsConfiguration =
			new SearchInsightsConfigurationImpl();

		PortletSharedSearchResult result = portletSharedSearchHelper.search(
			renderRequest, renderResponse, searchInsightsConfiguration);

		SearchResultsData searchResultsData = result.getSearchResultsData();

		String queryString = searchResultsData.getQueryString();

		if (Validator.isNull(queryString)) {
			ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
				"content.Language", renderRequest.getLocale(), getClass());

			queryString = LanguageUtil.get(
				resourceBundle, "search-insights-help");
		}

		SearchInsightsDisplayContext searchInsightsDisplayContext =
			new SearchInsightsDisplayContext();

		searchInsightsDisplayContext.setQueryString(
			HtmlUtil.escape(queryString));

		renderRequest.setAttribute(
			SearchInsightsDisplayContext.ATTRIBUTE,
			searchInsightsDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	@Reference
	protected PortletSharedSearchHelper portletSharedSearchHelper;

}