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

package com.liferay.portal.search.web.components.results.list.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.search.web.internal.request.helper.OriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortletOriginalServletRequestSupplierFactory;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchHelper;
import com.liferay.portal.search.web.internal.request.helper.PortletSharedSearchResult;
import com.liferay.portal.search.web.internal.request.params.SearchParameters;
import com.liferay.portal.search.web.internal.request.params.SearchParametersConfiguration;
import com.liferay.portal.search.web.internal.request.params.SearchParametersImpl;
import com.liferay.portlet.RenderRequestImpl;

import java.io.IOException;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

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
			SearchResultsListPortletKeys.CSS_CLASS_WRAPPER,
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
			SearchResultsListPortletKeys.DISPLAY_NAME,
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=" +
			SearchResultsListPortletKeys.VIEW_TEMPLATE,
		"javax.portlet.name=" + SearchResultsListPortletKeys.PORTLET_NAME,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = Portlet.class
)
public class SearchResultsListPortlet extends MVCPortlet {

	public static HttpServletRequest getOriginalServletRequest(
		RenderRequest renderRequest) {

		RenderRequestImpl renderRequestImpl =
			((RenderRequestImpl)renderRequest);

		HttpServletRequest httpServletRequest =
			renderRequestImpl.getHttpServletRequest();

		httpServletRequest = PortalUtil.getOriginalServletRequest(
			httpServletRequest);

		HttpServletRequestWrapper httpServletRequestWrapper =
			(HttpServletRequestWrapper)httpServletRequest;

		httpServletRequest =
			(HttpServletRequest)httpServletRequestWrapper.getRequest();

		return httpServletRequest;
	}

	// "javax.portlet.supported-public-render-parameter=q",

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		SearchParametersConfiguration searchParametersConfiguration =
			new SearchResultsListConfigurationImpl();

		SearchParameters searchParameters = createSearchParameters(
			renderRequest, searchParametersConfiguration);

		PortletSharedSearchResult result =
			portletSharedSearchFactory.search(
				renderRequest, renderResponse, searchParametersConfiguration);

		renderRequest.setAttribute(
			SearchResultsListDisplayContext.ATTRIBUTE,
			new SearchResultsListDisplayContext(
				searchParameters.getQParameter(),
				result.getSearchResultsData()));

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

	@Reference
	protected PortletOriginalServletRequestSupplierFactory
		portletOriginalServletRequestSupplierFactory;

	@Reference
	protected PortletSharedSearchHelper portletSharedSearchFactory;

}