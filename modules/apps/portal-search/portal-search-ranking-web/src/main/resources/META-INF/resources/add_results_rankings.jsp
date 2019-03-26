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

<%@ include file="/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect");

portletDisplay.setShowBackIcon(true);
portletDisplay.setURLBack(redirect);
%>

<portlet:actionURL name="createResultsRankingsEntry" var="createResultsRankingsEntryURL">
	<portlet:param name="mvcRenderCommandName" value="createResultsRankingsEntry" />
</portlet:actionURL>

<liferay-frontend:edit-form action="<%= createResultsRankingsEntryURL %>">
	<liferay-frontend:edit-form-body>
		<h2 class="sheet-title">
			<liferay-ui:message key="enter-a-search-term" />
		</h2>

		<div class="sheet-text">
			<liferay-ui:message key="customize-how-users-see-results-for-a-given-search-query" />
		</div>

		<aui:input label="<%= StringPool.BLANK %>" name="search-term" placeholder="search-term" />
	</liferay-frontend:edit-form-body>

	<liferay-frontend:edit-form-footer>
		<aui:button type="submit" value="customize-results" />

		<aui:button href="<%= redirect %>" type="cancel" />
	</liferay-frontend:edit-form-footer>
</liferay-frontend:edit-form>