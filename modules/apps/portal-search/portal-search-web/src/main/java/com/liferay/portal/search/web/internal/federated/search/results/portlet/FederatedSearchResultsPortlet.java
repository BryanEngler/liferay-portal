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

package com.liferay.portal.search.web.internal.federated.search.results.portlet;

import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.internal.federated.search.results.constants.FederatedSearchResultsPortletKeys;
import com.liferay.portal.search.spi.federated.summary.FederatedSearchResultSummaryBuilder;
import com.liferay.portal.search.summary.FederatedSearchSummary;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchRequest;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchResponse;
import com.liferay.portal.search.web.search.result.FederatedSearchResults;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author André de Oliveira
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.css-class-wrapper=portlet-search-results",
		"com.liferay.portlet.display-category=category.search",
		"com.liferay.portlet.icon=/icons/search.png",
		"com.liferay.portlet.instanceable=true",
		"com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.restore-current-view=false",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=Federated Search Results",
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/META-INF/resources/",
		"javax.portlet.init-param.view-template=/federated/search/results/view.jsp",
		"javax.portlet.name=" + FederatedSearchResultsPortletKeys.FEDERATED_SEARCH_RESULTS,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = Portlet.class
)
public class FederatedSearchResultsPortlet extends MVCPortlet {

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletSharedSearchResponse portletSharedSearchResponse =
			portletSharedSearchRequest.search(renderRequest);

		FederatedSearchResultsPortletDisplayContext
			federatedSearchResultsPortletDisplayContext =
			buildDisplayContext(portletSharedSearchResponse);

		renderRequest.setAttribute(
			WebKeys.PORTLET_DISPLAY_CONTEXT,
			federatedSearchResultsPortletDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_federatedSearchResultSummaryBuilders = ServiceTrackerListFactory.open(
			bundleContext, FederatedSearchResultSummaryBuilder.class);
	}

	protected FederatedSearchResultsPortletDisplayContext buildDisplayContext(
			PortletSharedSearchResponse portletSharedSearchResponse)
		throws PortletException {

		FederatedSearchResultsPortletDisplayContext
			federatedSearchResultsPortletDisplayContext =
			new FederatedSearchResultsPortletDisplayContext();

		Map<String, List<FederatedSearchSummary>> federatedSearchSummaries =
				new HashMap<>();

		FederatedSearchResults federatedSearchResults =
			portletSharedSearchResponse.getFederatedSearchResults();

		for (FederatedSearchResultSummaryBuilder
			 federatedSearchResultSummaryBuilder :
				_federatedSearchResultSummaryBuilders) {

			String source =
				federatedSearchResultSummaryBuilder.getSourceDisplayName();

			Document[] documents =
				federatedSearchResults.getDocumentsFromSource(source);

			List<FederatedSearchSummary> federatedSearchResultSummaries =
				new ArrayList<>();

			for (Document document : documents) {
				FederatedSearchSummary federatedSearchSummary =
					federatedSearchResultSummaryBuilder.getSummary(
						document);

				federatedSearchResultSummaries.add(federatedSearchSummary);
			}

			federatedSearchSummaries.put(source, federatedSearchResultSummaries);
		}

		federatedSearchResultsPortletDisplayContext.setFederatedSearchSummaries(
			federatedSearchSummaries);

		return federatedSearchResultsPortletDisplayContext;
	}

	@Reference
	protected PortletSharedSearchRequest portletSharedSearchRequest;

	private ServiceTrackerList<FederatedSearchResultSummaryBuilder, FederatedSearchResultSummaryBuilder>
		_federatedSearchResultSummaryBuilders;

}