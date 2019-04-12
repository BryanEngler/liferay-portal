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

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %><%@
taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>

<%@ page import="com.liferay.portal.kernel.util.HtmlUtil" %><%@
page import="com.liferay.portal.kernel.util.ParamUtil" %>

<%@ page import="com.liferay.portal.search.ranking.web.internal.constants.ResultsRankingPortletKeys" %>
<%@ page import="com.liferay.portal.kernel.util.Constants" %>

<liferay-frontend:defineObjects />

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
String redirect = ParamUtil.getString(request, "redirect");

String resultsRankingsRootElementId = renderResponse.getNamespace() + "-results-rankings-root";

String uid = ParamUtil.getString(request, "uid");
String keywords = ParamUtil.getString(request, "keywords");
String companyId = ParamUtil.getString(request, "companyId");
%>

<div id="<%= resultsRankingsRootElementId %>"></div>

<liferay-portlet:resourceURL id="/results_ranking/get_results" portletName="<%= ResultsRankingPortletKeys.RESULTS_RANKING %>" var="resultsRankingResourceURL" >
	<portlet:param name="resultsRankingUid" value="<%= uid %>" />
	<portlet:param name="keywords" value="<%= keywords %>" />
	<portlet:param name="companyId" value="<%= companyId %>" />
	<portlet:param name="<%= Constants.CMD %>" value="getVisibleResults" />
</liferay-portlet:resourceURL>

<liferay-portlet:resourceURL id="/results_ranking/get_results" portletName="<%= ResultsRankingPortletKeys.RESULTS_RANKING %>" var="hiddenResultsRankingResourceURL" >
	<portlet:param name="resultsRankingUid" value="<%= uid %>" />
	<portlet:param name="<%= Constants.CMD %>" value="getHiddenResults" />
</liferay-portlet:resourceURL>

<aui:script require='<%= npmResolvedPackageName + "/js/index.es as ResultsRankings" %>'>
	ResultsRankings.default(
		'<%= resultsRankingsRootElementId %>',
		{
			cancelUrl: '<%= HtmlUtil.escape(redirect) %>',
			fetchDocumentsUrl: '<%= resultsRankingResourceURL %>',
			fetchDocumentsHiddenUrl: '<%= hiddenResultsRankingResourceURL %>',
			searchTerm: '<%= HtmlUtil.escape(keywords) %>'
		},
		{
			companyId: '<%= themeDisplay.getCompanyId() %>',
			searchIndex: '',
			spritemap: '<%= themeDisplay.getPathThemeImages() + "/lexicon/icons.svg" %>'
		}
	);
</aui:script>