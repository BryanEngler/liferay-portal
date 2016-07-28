<%--
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
--%>

<%@page import="com.liferay.portal.kernel.search.Document"%>
<%@page import="com.liferay.portal.kernel.dao.search.SearchContainer"%>
<%@ include file="/init.jsp" %>

<%@ page import="com.liferay.portal.search.web.internal.results.data.SearchResultsData" %>
<%@ page import="com.liferay.portal.search.web.components.results.list.portlet.SearchResultsListDisplayContext" %>
<%@ page import="com.liferay.portal.search.web.components.results.list.portlet.SearchResultsListViewHelper" %>

<%@ page import="com.liferay.asset.kernel.model.AssetEntry" %>
<%@ page import="com.liferay.blogs.kernel.model.BlogsEntry" %>
<%@ page import="com.liferay.message.boards.kernel.model.MBMessage" %>

<%@ page import="java.text.SimpleDateFormat" %>

<%@ page import="com.liferay.blogs.kernel.service.BlogsEntryLocalServiceUtil" %>

<%
SearchResultsListDisplayContext searchResultsListDisplayContext = 
	(SearchResultsListDisplayContext)java.util.Objects.requireNonNull(
		request.getAttribute(SearchResultsListDisplayContext.ATTRIBUTE));

SearchResultsListViewHelper dc = new SearchResultsListViewHelper(
	searchResultsListDisplayContext.getSearchResultsData());
				 
SearchContainer<Document> newSearchContainer = dc.getSearchResultsContainer();
%>

<style>
	.taglib-asset-tags-summary a.badge, .taglib-asset-tags-summary a.badge:hover {
		color: #65B6F0;
	}

	.search-total-label {
		margin-top: 35px;
	}

	.search-asset-type-sticker {
		color: #869CAD;
	}

	.search-document-content {
		font-weight: 400;
	}

	.search-result-thumbnail-img {
		height: 44px;
		width: 44px;
	}

	.tabular-list-group .list-group-item-content h6.search-document-tags {
		margin-top: 13px;
	}
</style>

<%
PortletURL portletURL = renderResponse.createRenderURL();
%>

<liferay-frontend:management-bar
	searchContainerId="resultsContainer"
>
	<liferay-frontend:management-bar-buttons>
		<liferay-frontend:management-bar-display-buttons
			displayViews='<%= new String[] {"icon", "descriptive"} %>'
			portletURL="<%= portletURL %>"
			selectedDisplayStyle="descriptive"
		/>
	</liferay-frontend:management-bar-buttons>

	<liferay-frontend:management-bar-filters>
		<liferay-frontend:management-bar-navigation
			navigationKeys='<%= new String[] {"category", "asset-type"} %>'
			navigationParam=""
			portletURL="<%= portletURL %>"
		/>

		<liferay-frontend:management-bar-sort
			orderByCol=""
			orderByType=""
			orderColumns='<%= new String[] {"title", "display-date"} %>'
			portletURL="<%= portletURL %>"
		/>
	</liferay-frontend:management-bar-filters>
</liferay-frontend:management-bar>

<%
java.util.List<Document> searchResults = newSearchContainer.getResults();

int searchResultsAmount = searchResults.size();

String searchQuery = searchResultsListDisplayContext.getQ();
%>

<p class="search-total-label text-default">
	About <%= searchResultsAmount %> results for <strong><%= searchQuery %></strong>
</p>

<%@ include file="/components/results/list/results_list.jspf" %>
