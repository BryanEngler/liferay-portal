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

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/clay" prefix="clay" %><%@
taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %><%@
taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>

<%@ page import="com.liferay.frontend.taglib.clay.servlet.taglib.util.JSPNavigationItemList" %><%@
page import="com.liferay.petra.string.StringPool" %><%@
page import="com.liferay.portal.kernel.language.LanguageUtil" %><%@
page import="com.liferay.portal.kernel.util.Constants" %><%@
page import="com.liferay.portal.kernel.util.HtmlUtil" %><%@
page import="com.liferay.portal.kernel.util.ParamUtil" %><%@
page import="com.liferay.portal.search.ranking.web.internal.constants.ResultsRankingPortletKeys" %><%@
page import="com.liferay.portal.search.ranking.web.internal.display.context.ResultsRankingPortletDisplayContext" %>

<liferay-frontend:defineObjects />

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
ResultsRankingPortletDisplayContext resultsRankingPortletDisplayContext = (ResultsRankingPortletDisplayContext)request.getAttribute(ResultsRankingPortletKeys.RESULTS_RANKING_DISPLAY_CONTEXT);
%>

<clay:management-toolbar
	actionDropdownItems="<%= resultsRankingPortletDisplayContext.getActionDropdownItems() %>"
	clearResultsURL="<%= resultsRankingPortletDisplayContext.getClearResultsURL() %>"
	componentId="resultsRankingEntriesManagementToolbar"
	creationMenu="<%= resultsRankingPortletDisplayContext.getCreationMenu() %>"
	disabled="<%= resultsRankingPortletDisplayContext.isDisabledManagementBar() %>"
	filterDropdownItems="<%= resultsRankingPortletDisplayContext.getFilterItemsDropdownItems() %>"
	itemsTotal="<%= resultsRankingPortletDisplayContext.getTotalItems() %>"
	searchActionURL="<%= resultsRankingPortletDisplayContext.getSearchActionURL() %>"
	searchContainerId="resultsRankingEntries"
	searchFormName="searchFm"
	selectable="<%= true %>"
	showCreationMenu="<%= resultsRankingPortletDisplayContext.isShowCreationMenu() %>"
	sortingOrder="<%= resultsRankingPortletDisplayContext.getOrderByType() %>"
	sortingURL="<%= resultsRankingPortletDisplayContext.getSortingURL() %>"
/>

<portlet:actionURL name="deleteResultsRankingsEntry" var="deleteResultsRankingsEntryURL">
	<portlet:param name="redirect" value="<%= currentURL %>" />
</portlet:actionURL>

<aui:form action="<%= deleteResultsRankingsEntryURL %>" cssClass="container-fluid-1280" method="post" name="resultsRankingEntriesFm">
	<aui:input name="redirect" type="hidden" value="<%= currentURL %>" />

	<liferay-ui:search-container
		id="resultsRankingEntries"
		searchContainer="<%= resultsRankingPortletDisplayContext.getSearchContainer() %>"
	>
		<liferay-ui:search-container-row
			className="com.liferay.portal.search.ranking.web.internal.display.context.ResultsRankingEntryDisplayContext"
			keyProperty="resultsRankingsEntryId"
			modelVar="resultsRankingEntryDisplayContext"
		>
			<portlet:renderURL var="rowURL">
				<portlet:param name="mvcRenderCommandName" value="editResultsRankingsEntry" />
				<portlet:param name="redirect" value="<%= currentURL %>" />
				<portlet:param name="uid" value="<%= resultsRankingEntryDisplayContext.getUid() %>" />
			</portlet:renderURL>

			<liferay-ui:search-container-column-text
				cssClass="table-cell-expand table-title"
				href="<%= rowURL %>"
				name="search-terms"
				value="<%= resultsRankingEntryDisplayContext.getKeywords() %>"
			/>

			<liferay-ui:search-container-column-text
				cssClass="table-cell-expand"
				href="<%= rowURL %>"
				name="aliases"
				value="<%= resultsRankingEntryDisplayContext.getAliases() %>"
			/>

			<liferay-ui:search-container-column-text
				cssClass="table-cell-expand-smallest table-cell-minw-150"
				href="<%= rowURL %>"
				name="index"
				value="<%= resultsRankingEntryDisplayContext.getIndex() %>"
			/>

			<liferay-ui:search-container-column-text
				cssClass="table-cell-expand-smallest table-cell-minw-150"
				name="pinned-results"
				value="<%= resultsRankingEntryDisplayContext.getPinnedResultsCount() %>"
			/>

			<liferay-ui:search-container-column-text
				cssClass="table-cell-expand-smallest table-cell-minw-150"
				name="hidden-results"
				value="<%= resultsRankingEntryDisplayContext.getHiddenResultsCount() %>"
			/>

			<liferay-ui:search-container-column-date
				cssClass="table-cell-expand-smallest table-cell-minw-150 table-cell-ws-nowrap"
				name="modified-date"
				value="<%= resultsRankingEntryDisplayContext.getModifiedDate() %>"
			/>

			<liferay-ui:search-container-column-date
				cssClass="table-cell-expand-smallest table-cell-minw-150 table-cell-ws-nowrap"
				name="display-date"
				value="<%= resultsRankingEntryDisplayContext.getDisplayDate() %>"
			/>

			<liferay-ui:search-container-column-status
				name="status"
				status="<%= 0 %>"
			/>
		</liferay-ui:search-container-row>

		<liferay-ui:search-iterator
			markupView="lexicon"
		/>
	</liferay-ui:search-container>
</aui:form>