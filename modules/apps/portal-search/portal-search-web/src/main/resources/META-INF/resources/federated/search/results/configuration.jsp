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
taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>

<%@ page import="com.liferay.portal.kernel.util.Constants" %><%@
page import="com.liferay.portal.search.web.internal.util.PortletPreferencesJspUtil" %><%@
page import="com.liferay.portal.search.spi.federated.searcher.FederatedSearcher" %><%@
page import="java.util.Collection" %><%@
page import="com.liferay.registry.RegistryUtil" %><%@
page import="com.liferay.registry.Registry" %><%@
page import="com.liferay.portal.search.web.internal.federated.search.results.portlet.FederatedSearchResultsPortletPreferences" %><%@
page import="com.liferay.portal.search.web.internal.federated.search.results.portlet.FederatedSearchResultsPortletPreferencesImpl" %>

<portlet:defineObjects />

<%
Registry registry = RegistryUtil.getRegistry();

Collection<FederatedSearcher> federatedSearchers = registry.getServices(FederatedSearcher.class, null);

FederatedSearchResultsPortletPreferences
	federatedSearchResultsPortletPreferences =
	new FederatedSearchResultsPortletPreferencesImpl(
		java.util.Optional.ofNullable(portletPreferences), federatedSearchers);
%>

<liferay-portlet:actionURL portletConfiguration="<%= true %>" var="configurationActionURL" />

<liferay-portlet:renderURL portletConfiguration="<%= true %>" var="configurationRenderURL" />

<liferay-frontend:edit-form
	action="<%= configurationActionURL %>"
	method="post"
	name="fm"
>
	<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />
	<aui:input name="redirect" type="hidden" value="<%= configurationRenderURL %>" />

	<liferay-frontend:edit-form-body>
		<liferay-frontend:fieldset-group>
			<aui:select label="Federated Search Source Name" name="<%= PortletPreferencesJspUtil.getInputName(FederatedSearchResultsPortletPreferences.PREFERENCE_KEY_FEDERATED_SEARCH_SOURCE_NAME) %>">
			<%
			String[] sourceNames = federatedSearchResultsPortletPreferences.getFederatedSearchSourceNames();

			for (String sourceName : sourceNames) {
			%>
				<aui:option label="<%= sourceName %>" selected="<%= sourceName.equals(federatedSearchResultsPortletPreferences.getSelectedFederatedSearchSourceName()) %>" value="<%= sourceName %>" />
			<%
			}
			%>
			</aui:select>

			<aui:select label="Display Style" name="<%= PortletPreferencesJspUtil.getInputName(FederatedSearchResultsPortletPreferences.PREFERENCE_KEY_FEDERATED_SEARCH_DISPLAY_STYLE) %>">
			<%
			String displayStyle = federatedSearchResultsPortletPreferences.getDisplayStyle();
			%>
				<aui:option label="Video" selected="<%= displayStyle.equals("video") %>" value="video" />
				<aui:option label="Standard" selected="<%= displayStyle.equals("standard") %>" value="standard" />
			<%

			%>
			</aui:select>
		</liferay-frontend:fieldset-group>
	</liferay-frontend:edit-form-body>

	<liferay-frontend:edit-form-footer>
		<aui:button type="submit" />

		<aui:button type="cancel" />
	</liferay-frontend:edit-form-footer>
</liferay-frontend:edit-form>