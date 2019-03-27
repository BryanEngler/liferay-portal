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

package com.liferay.portal.search.ranking.web.internal.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.ranking.web.internal.constants.ResultsRankingPortletKeys;
import com.liferay.portal.search.ranking.web.internal.display.context.ResultsRankingPortletDisplayContext;

import java.io.IOException;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Filipe Oshiro
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.css-class-wrapper=portlet-search-ranked",
		"com.liferay.portlet.display-category=category.hidden",
		"com.liferay.portlet.footer-portlet-javascript=/js/main.js",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.icon=/icons/search.png",
		"com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=Search Ranking",
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/META-INF/resources/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + ResultsRankingPortletKeys.RESULTS_RANKING,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = Portlet.class
)
public class ResultsRankingPortlet extends MVCPortlet {

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		HttpServletRequest httpServletRequest = _portal.getHttpServletRequest(
			renderRequest);

		ResultsRankingPortletDisplayContext
			resultsRankingPortletDisplayContext =
				new ResultsRankingPortletDisplayContext(
					httpServletRequest, renderRequest, renderResponse);

		setResults(resultsRankingPortletDisplayContext);

		renderRequest.setAttribute(
			ResultsRankingPortletKeys.RESULTS_RANKING_DISPLAY_CONTEXT,
			resultsRankingPortletDisplayContext);

		super.render(renderRequest, renderResponse);
	}

	protected void setResults(
		ResultsRankingPortletDisplayContext resultsRankingPortletDisplayContext) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames("results-ranking");

		Query matchAllQuery = queries.matchAll();

		searchSearchRequest.setQuery(matchAllQuery);

		SearchSearchResponse searchSearchResponse =
			searchEngineAdapter.execute(searchSearchRequest);

		Hits hits = searchSearchResponse.getHits();

		resultsRankingPortletDisplayContext.setResults(hits.toList());
	}

	@Reference
	protected Queries queries;

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	@Reference
	private Portal _portal;

}