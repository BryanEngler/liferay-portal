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

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<liferay-frontend:defineObjects />

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
com.liferay.portal.search.web.internal.search.facet.portlet.SearchFacetDisplayContext searchFacetDisplayContext =
	(com.liferay.portal.search.web.internal.search.facet.portlet.SearchFacetDisplayContext)java.util.Objects.requireNonNull(
		request.getAttribute(com.liferay.portal.search.web.internal.search.facet.portlet.SearchFacetDisplayContext.ATTRIBUTE));

com.liferay.portal.search.web.components.facet.SearchFacetPortletViewHelper searchFacetPortletViewHelper =
	new com.liferay.portal.search.web.components.facet.SearchFacetPortletViewHelper(request);

if (!searchFacetPortletViewHelper.isSearchFacetConfigured()) {
%>

	<div class="alert alert-info portlet-configuration">
		<a href="<%= portletDisplay.getURLConfiguration() %>" onClick="<%= portletDisplay.getURLConfigurationJS() %>">
			<liferay-ui:message key="please-configure-this-portlet-and-select-at-least-one-facet-to-display" />
		</a>
	</div>

<%
}
else {
	searchFacetPortletViewHelper.includeView(pageContext);
}
%>

<aui:script sandbox="<%= true %>">
	$('.facet-value').on(
		'click',
		function(event) {
			var term = $(event.currentTarget);

			var inputFacetName = "<portlet:namespace />inputFacetName";
			var facetName = $("[name='"+ inputFacetName +"']").val();

			var inputFacetValue = term[0].getAttribute('name')+"_value";
			var facetValue = $("[name='" + inputFacetValue + "']").val();

			if (facetName && facetValue) {
				insertParam(facetName, facetValue);
			}
		}
	);

	function insertParam(key, value) {
		key = encodeURI(key);
		value = encodeURI(value);

		var kvp = document.location.search.substr(1).split('&');

		var i = kvp.length;
		var x;

		while (i--) {
			x = kvp[i].split('=');

			if (x[0]==key) {
				x[1] = value;

				kvp[i] = x.join('=');

				break;
			}
		}

		if (i<0) {
			kvp[kvp.length] = [key,value].join('=');
		}

		//this will reload the page, it's likely better to store this until finished

		document.location.search = kvp.join('&');
	}
</aui:script>