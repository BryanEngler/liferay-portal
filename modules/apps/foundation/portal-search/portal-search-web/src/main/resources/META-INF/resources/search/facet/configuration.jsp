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

<%@ page import="com.liferay.portal.kernel.util.Constants" %>
<%@ page import="com.liferay.portal.search.web.internal.search.facet.portlet.SearchFacetPortletKeys" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.portal.search.web.internal.search.facet.portlet.SearchFacetDisplayContext" %>
<%@ page import="com.liferay.taglib.servlet.PipingServletResponse" %>
<%@ page import="com.liferay.taglib.aui.AUIUtil" %>
<%@ page import="com.liferay.portal.kernel.util.ListUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="com.liferay.portal.search.web.facet.SearchFacet" %>
<%@ page import="com.liferay.portal.kernel.util.StringPool" %>
<%@ page import="com.liferay.portal.search.web.facet.util.SearchFacetTracker" %>
<%@ page import="com.liferay.portal.search.web.facet.util.comparator.SearchFacetComparator" %>

<liferay-theme:defineObjects />

<portlet:defineObjects />

<liferay-portlet:actionURL portletConfiguration="<%= true %>" var="configurationActionURL" />

<%
String title = portletPreferences.getValue(SearchFacetPortletKeys.FACET_TITLE, StringPool.BLANK);
String facet = portletPreferences.getValue(SearchFacetPortletKeys.FACET, StringPool.BLANK);

List<SearchFacet> searchFacets = ListUtil.copy(SearchFacetTracker.getSearchFacets());

for (SearchFacet searchFacet : searchFacets) {
	searchFacet.init(company.getCompanyId(), null);
}

searchFacets = ListUtil.sort(searchFacets, SearchFacetComparator.INSTANCE);
%>

<aui:form action="<%= configurationActionURL %>" method="post" onSubmit='<%= "event.preventDefault(); " + renderResponse.getNamespace() + "saveConfiguration();" %>'>
	<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />

	<aui:fieldset id="controls" label="search-bar-classic-properties">
		<aui:input disabled="true" label="facet-title" name="preferences--title--" value="<%= title %>" />
		<aui:select id="facetSelect" label="facet" name="preferences--facet--" value="<%= facet %>">
			<aui:option label="" value="" />

			<%
			for (SearchFacet searchFacet : searchFacets) {
			%>

				<aui:option label="<%= searchFacet.getTitle() %>" value="<%= searchFacet.getClassName() %>" />

			<%
			}
			%>

		</aui:select>

		<%
		for (SearchFacet searchFacet : searchFacets) {
		%>

			<aui:script>
				Liferay.Util.toggleSelectBox('<portlet:namespace />facetSelect', '<%= searchFacet.getClassName() %>', '<portlet:namespace /><%= AUIUtil.normalizeId(searchFacet.getClassName()) %>FacetConfiguration');
			</aui:script>

			<div id="<portlet:namespace /><%= AUIUtil.normalizeId(searchFacet.getClassName()) %>FacetConfiguration">
				<liferay-ui:toggle-area id="<portlet:namespace /><%= AUIUtil.normalizeId(searchFacet.getClassName()) %>FacetToggleArea">
					<aui:fieldset>

						<%
						request.setAttribute("facet_configuration.jsp-searchFacet", searchFacet);
						searchFacet.includeConfiguration(request, new PipingServletResponse(pageContext));
						%>

					</aui:fieldset>
				</liferay-ui:toggle-area>
			</div>

		<%
		}
		%>

		<aui:button-row>
			<aui:button type="submit" />
		</aui:button-row>
	</aui:fieldset>
</aui:form>

<aui:script>
	function <portlet:namespace />saveConfiguration() {
		var form = AUI.$(document.<portlet:namespace />fm);

		submitForm(form);
	}
</aui:script>