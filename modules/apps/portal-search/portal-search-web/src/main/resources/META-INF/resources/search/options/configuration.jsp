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
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.portal.kernel.util.Constants" %><%@
page import="com.liferay.portal.search.web.internal.search.options.portlet.SearchOptionsPortletPreferences" %><%@
page import="com.liferay.portal.search.web.internal.util.PortletPreferencesJspUtil" %><%@
page import="com.liferay.petra.string.StringPool" %>
<%@ page import="com.liferay.registry.RegistryUtil" %>
<%@ page import="com.liferay.registry.Registry" %>
<%@ page
	import="com.liferay.portal.search.spi.federated.searcher.FederatedSearcher" %>
<%@ page import="java.util.Collection" %>

<portlet:defineObjects />

<%
Registry registry = RegistryUtil.getRegistry();

Collection<FederatedSearcher> federatedSearchers = registry.getServices(FederatedSearcher.class, null);

SearchOptionsPortletPreferences searchOptionsPortletPreferences = new com.liferay.portal.search.web.internal.search.options.portlet.SearchOptionsPortletPreferencesImpl(java.util.Optional.ofNullable(portletPreferences), federatedSearchers);
%>

<liferay-portlet:actionURL portletConfiguration="<%= true %>" var="configurationActionURL" />

<liferay-portlet:renderURL portletConfiguration="<%= true %>" var="configurationRenderURL" />

<liferay-frontend:edit-form
	action="<%= configurationActionURL %>"
	method="post"
	name="fm"
	onSubmit='<%= "event.preventDefault(); " + renderResponse.getNamespace() + "saveConfiguration();" %>'
>
	<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />
	<aui:input name="redirect" type="hidden" value="<%= configurationRenderURL %>" />

	<liferay-frontend:edit-form-body>
		<liferay-frontend:fieldset-group>
			<aui:fieldset>
				<aui:input helpMessage="allow-empty-searches-help" label="allow-empty-searches" name="<%= PortletPreferencesJspUtil.getInputName(SearchOptionsPortletPreferences.PREFERENCE_KEY_ALLOW_EMPTY_SEARCHES) %>" type="checkbox" value="<%= searchOptionsPortletPreferences.isAllowEmptySearches() %>" />

				<aui:input helpMessage="basic-facet-selection-help" label="basic-facet-selection" name="<%= PortletPreferencesJspUtil.getInputName(SearchOptionsPortletPreferences.PREFERENCE_KEY_BASIC_FACET_SELECTION) %>" type="checkbox" value="<%= searchOptionsPortletPreferences.isBasicFacetSelection() %>" />

				<aui:select label="Federated Search" name="<%= PortletPreferencesJspUtil.getInputName(SearchOptionsPortletPreferences.PREFERENCE_KEY_FEDERATED_SEARCH_ENABLED) %>">
					<aui:option label="enabled" selected="<%= searchOptionsPortletPreferences.federatedSearchEnabled() %>" value="<%= true %>" />
					<aui:option label="disabled" selected="<%= !searchOptionsPortletPreferences.federatedSearchEnabled() %>" value="<%= false %>" />
				</aui:select>

				<aui:input name="<%= PortletPreferencesJspUtil.getInputName(SearchOptionsPortletPreferences.PREFERENCE_KEY_FEDERATED_SEARCH_SOURCES) %>" type="hidden" value="<%= searchOptionsPortletPreferences.getFederatedSearchSourcesString() %>"/>

				<div class="<%= searchOptionsPortletPreferences.federatedSearchEnabled() ? StringPool.BLANK : "hide" %>" id="<portlet:namespace />federatedSearchSourcesBoxes">
					<liferay-ui:input-move-boxes
						leftBoxName="currentFederatedSearchSources"
						leftList="<%= searchOptionsPortletPreferences.getCurrentFederatedSearchSources() %>"
						leftTitle="current sources"
						rightBoxName="availableFederatedSearchSources"
						rightList="<%= searchOptionsPortletPreferences.getAvailableFederatedSearchSources() %>"
						rightTitle="available sources"
					/>
				</div>

			</aui:fieldset>
		</liferay-frontend:fieldset-group>
	</liferay-frontend:edit-form-body>

	<liferay-frontend:edit-form-footer>
		<aui:button type="submit" />

		<aui:button type="cancel" />
	</liferay-frontend:edit-form-footer>
</liferay-frontend:edit-form>

<aui:script>
	function <portlet:namespace />saveConfiguration() {
		var form = AUI.$(document.<portlet:namespace />fm);

		form.fm('federatedSearchSources').val(Liferay.Util.listSelect(form.fm('currentFederatedSearchSources')));

		submitForm(form);
	}

	Liferay.Util.toggleSelectBox('<portlet:namespace />federatedSearchEnabled', 'true', '<portlet:namespace />federatedSearchSourcesBoxes');
</aui:script>