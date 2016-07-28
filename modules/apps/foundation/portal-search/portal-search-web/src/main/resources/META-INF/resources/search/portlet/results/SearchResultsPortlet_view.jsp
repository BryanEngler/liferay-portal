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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.portal.kernel.language.LanguageUtil" %>
<%@ page import="com.liferay.portal.kernel.search.Document" %>
<%@ page import="com.liferay.portal.kernel.util.HtmlUtil" %>
<%@ page import="com.liferay.portal.search.web.internal.portlet.results.SearchResultsDisplayContext" %>

<portlet:defineObjects />

<%
SearchResultsDisplayContext searchResultsDisplayContext = (SearchResultsDisplayContext)java.util.Objects.requireNonNull(request.getAttribute(SearchResultsDisplayContext.ATTRIBUTE));

com.liferay.portal.kernel.dao.search.SearchContainer<Document> searchContainer1 = com.liferay.portal.search.web.internal.portlet.results.SearchResultsSearchContainerUtil.getSearchContainer(searchResultsDisplayContext, renderRequest, renderResponse, request);
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

<c:if test="false">

	<%
	javax.portlet.PortletURL portletURL = null;//renderResponse.createRenderURL();
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
</c:if>

<p class="search-total-label text-default">
	About <%= searchContainer1.getTotal() %> results for <strong><%= searchResultsDisplayContext.getKeywords() %></strong>
</p>

<liferay-ui:search-container
	emptyResultsMessage='<%= LanguageUtil.format(request, "no-results-were-found-that-matched-the-keywords-x", "<strong>" + HtmlUtil.escape(searchResultsDisplayContext.getKeywords()) + "</strong>", false) %>'
	id="search"
	searchContainer="<%= searchContainer1 %>"
>
	<liferay-ui:search-container-row
		className="com.liferay.portal.kernel.search.Document"
		escapedModel="<%= false %>"
		keyProperty="UID"
		modelVar="document"
		stringKey="<%= true %>"
	>

		<%
		com.liferay.portal.search.web.internal.result.display.context.SearchResultSummaryDisplayContext searchResultSummaryDisplayContext = searchResultsDisplayContext.getSummary(document);
		%>

		<liferay-ui:search-container-column-text>
			<c:if test="<%= searchResultSummaryDisplayContext.isCoverImageVisible() %>">
				<img alt="blog cover image" class="img-rounded search-result-thumbnail-img" src="<%= searchResultSummaryDisplayContext.getCoverImageURL() %>" />
			</c:if>

			<c:if test="<%= searchResultSummaryDisplayContext.isAssetIconVisible() %>">
				<span class="search-asset-type-sticker sticker sticker-default sticker-lg sticker-rounded sticker-static">
					<svg class="lexicon-icon">
						<use xlink:href="<%= searchResultSummaryDisplayContext.getPathThemeImages() %>/lexicon/icons.svg#<%= searchResultSummaryDisplayContext.getAssetIcon() %>" />
					</svg>
				</span>
			</c:if>
		</liferay-ui:search-container-column-text>

		<liferay-ui:search-container-column-text
			colspan="<%= 2 %>"
		>
			<h4>
				<a href="<%= searchResultSummaryDisplayContext.getViewURL() %>">
					<strong><%= searchResultSummaryDisplayContext.getHighlightedTitle() %></strong>
				</a>
			</h4>

			<h6 class="text-default">
				<strong><%= searchResultSummaryDisplayContext.getModelResource() %></strong> &#183;

				<c:if test="<%= searchResultSummaryDisplayContext.isLocaleReminderVisible() %>">
					<liferay-ui:icon image='<%= "../language/" + searchResultSummaryDisplayContext.getLocaleLanguageId() %>' message="<%= searchResultSummaryDisplayContext.getLocaleReminder() %>" />
				</c:if>

				<c:if test="<%= searchResultSummaryDisplayContext.isCreatorVisible() %>">
					<liferay-ui:message key="written-by" /> <strong><%= searchResultSummaryDisplayContext.getCreator() %></strong>
				</c:if>

				<c:if test="<%= searchResultSummaryDisplayContext.isCreationDateVisible() %>">
					<liferay-ui:message key="on-date" /> <%= searchResultSummaryDisplayContext.getCreationDateString() %>
				</c:if>
			</h6>

			<c:if test="<%= searchResultSummaryDisplayContext.isContentVisible() %>">
				<h6 class="search-document-content text-default">
					<%= searchResultSummaryDisplayContext.getContent() %>
				</h6>
			</c:if>

			<c:if test="<%= searchResultSummaryDisplayContext.isAssetCategoriesOrTagsVisible() %>">
				<h6 class="search-document-tags text-default">
					<liferay-ui:asset-tags-summary
						className="<%= searchResultSummaryDisplayContext.getClassName() %>"
						classPK="<%= searchResultSummaryDisplayContext.getClassPK() %>"
						paramName="<%= searchResultSummaryDisplayContext.getFieldAssetTagNames() %>"
						portletURL="<%= searchResultSummaryDisplayContext.getPortletURL() %>"
					/>

					<liferay-ui:asset-categories-summary
						className="<%= searchResultSummaryDisplayContext.getClassName() %>"
						classPK="<%= searchResultSummaryDisplayContext.getClassPK() %>"
						paramName="<%= searchResultSummaryDisplayContext.getFieldAssetCategoryIds() %>"
						portletURL="<%= searchResultSummaryDisplayContext.getPortletURL() %>"
					/>
				</h6>
			</c:if>
		</liferay-ui:search-container-column-text>
	</liferay-ui:search-container-row>

	<liferay-ui:search-iterator displayStyle="descriptive" markupView="lexicon" type="more" />
</liferay-ui:search-container>