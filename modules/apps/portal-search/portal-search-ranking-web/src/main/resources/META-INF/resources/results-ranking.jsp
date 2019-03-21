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
	/* TODO: Get list of result rankings when backend is done */
	Map<String, Object> context = new HashMap<>();
	List<Object> resultRankings = new ArrayList<>();
	context.put("resultRankings", resultRankings);
%>

<clay:management-toolbar
	showCreationMenu="<%= false %>"
/>

<div class="closed container-fluid-1280 sidenav-container sidenav-right" id="<portlet:namespace />infoPanelId">
	<soy:component-renderer
		context="<%= context %>"
		module="js/ResultRankings.es"
		templateNamespace="com.liferay.portal.search.ranking.web.ResultRankings.render"
	/>
</div>