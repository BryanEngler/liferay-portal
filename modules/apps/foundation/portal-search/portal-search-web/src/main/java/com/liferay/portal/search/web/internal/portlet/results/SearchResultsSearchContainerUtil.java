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

package com.liferay.portal.search.web.internal.portlet.results;

import com.liferay.portal.kernel.dao.search.DisplayTerms;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.search.web.internal.container.SearchContainerPortletURL;
import com.liferay.portal.search.web.portlet.SearchParametersConfiguration;

import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author André de Oliveira
 */
public class SearchResultsSearchContainerUtil {

	public static SearchContainer<Document> getSearchContainer(
			SearchResultsDisplayContext searchResultsDisplayContext,
			RenderRequest renderRequest, RenderResponse renderResponse,
			HttpServletRequest request)
		throws PortletException {

		PortletRequest portletRequest = renderRequest;
		DisplayTerms displayTerms = null;
		DisplayTerms searchTerms = null;
		String curParam = searchResultsDisplayContext.getFromParameterName();
		int cur = searchResultsDisplayContext.getFrom();
		int delta = SearchContainer.DEFAULT_DELTA;
		PortletURL iteratorURL = cleanPortletURL(request);
		List<String> headerNames = null;
		String emptyResultsMessage = null;
		String cssClass = null;

		SearchContainer<Document> searchContainer = new SearchContainer<>(
			portletRequest, displayTerms, searchTerms, curParam, cur, delta,
			iteratorURL, headerNames, emptyResultsMessage, cssClass);

		searchContainer.setResults(searchResultsDisplayContext.getDocuments());
		searchContainer.setTotal(searchResultsDisplayContext.getTotalHits());

		return searchContainer;
	}

	protected static PortletURL cleanPortletURL(HttpServletRequest request) {
		String url = HttpUtil.getCompleteURL(
			PortalUtil.getOriginalServletRequest(request));

		url = HttpUtil.removeParameter(
			url, SearchParametersConfiguration.DEFAULT_FROM_PARAMETER_NAME);

		return new SearchContainerPortletURL(url);
	}

}