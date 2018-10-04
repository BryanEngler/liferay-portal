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

<%@ page import="com.liferay.portal.kernel.util.WebKeys" %><%@
page import="java.util.List" %><%@
page import="java.util.Map" %><%@
page import="com.liferay.portal.search.summary.FederatedSearchSummary" %><%@
page import="com.liferay.portal.search.web.internal.federated.search.results.portlet.FederatedSearchResultsPortletDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.federated.search.results.portlet.FederatedSearchResultsPortletPreferences" %>
<%@ page import="com.liferay.portal.kernel.util.Validator" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %><%@
taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>

<portlet:defineObjects />

<%
FederatedSearchResultsPortletDisplayContext federatedSearchResultsPortletDisplayContext = (FederatedSearchResultsPortletDisplayContext)java.util.Objects.requireNonNull(request.getAttribute(WebKeys.PORTLET_DISPLAY_CONTEXT));

FederatedSearchResultsPortletPreferences federatedSearchResultsPortletPreferences = new com.liferay.portal.search.web.internal.federated.search.results.portlet.FederatedSearchResultsPortletPreferencesImpl(java.util.Optional.ofNullable(portletPreferences), null);

Map<String, List<FederatedSearchSummary>> federatedSearchSummaries = federatedSearchResultsPortletDisplayContext.getFederatedSearchSummaries();

String source = federatedSearchResultsPortletPreferences.getSelectedFederatedSearchSourceName();

List<FederatedSearchSummary> federatedSearchResultSummaries = null;

if (!Validator.isBlank(source)) {
	federatedSearchResultSummaries = federatedSearchSummaries.get(source);

	if (federatedSearchResultSummaries != null && federatedSearchResultSummaries.size() > 0) {
		String displayStyle = federatedSearchResultsPortletPreferences.getDisplayStyle();

		if (displayStyle.equals("video")) {
		%>
			<style>
				result:after {
					content: "";
					display: table;
					clear: both;
				}

				videoThumb {
					float: left;
					width: 180px;
				}
			</style>

			<iframe allowfullscreen="true" hidden="true" id="<%= renderResponse.getNamespace() + "embeddedViewer" %>"  width="100%" height="450" /></iframe>

			<li>Source: <strong><%= source %></strong></li><br>
			<ul>
			<%
			for (FederatedSearchSummary federatedSearchSummary : federatedSearchResultSummaries) {
				String videoURL = "https://www.youtube.com/watch?v=" + federatedSearchSummary.getURL();
				String thumbURL = "https://img.youtube.com/vi/" + federatedSearchSummary.getURL() + "/0.jpg";
			%>
				<li style="list-style-type: none" />
					<result>
						<videoThumb>
							<img title="<%= federatedSearchSummary.getTitle() %>" src="<%= thumbURL %>" onclick='<%= renderResponse.getNamespace() %>preview("<%= federatedSearchSummary.getURL() %>")' width="150" height="100" />
							<div style="font-size: 11px" >(Click thumbnail for preview)</div>
						</videoThumb>
						<description>
							<aui:a target="_blank" href="<%= videoURL %>" ><strong><%= federatedSearchSummary.getTitle() %></strong></aui:a><br>
							<i><%= federatedSearchSummary.getContent() %></i>
						</description>
					</result><br>
				</li>
			<%
			}
			%>
			</ul>
		<%
		}
		else if (displayStyle.equals("standard")) {
			%>
			<li>Source: <strong><%= source %></strong></li><br>
			<%
			for (FederatedSearchSummary federatedSearchSummary : federatedSearchResultSummaries) {
				String videoURL = "https://www.youtube.com/watch?v=" + federatedSearchSummary.getURL();
			%>
				<li style="list-style-type: none" />
					<aui:a target="_blank" href="<%= videoURL %>" ><strong><%= federatedSearchSummary.getTitle() %></strong></aui:a><br>
					<div style="font-size: small" ><font color="green"><%= "https://www.youtube.com/watch?v=" + federatedSearchSummary.getURL() %></font></div>
					<i><%= federatedSearchSummary.getContent() %></i>
				</li><br>
			<%
			}
		}
		else {
			%>
			<style>
				.highlight {
					background-color: transparent;
				}
			</style>
			<li>Source: <strong><%= source %></strong></li><br>
			<%
			for (FederatedSearchSummary federatedSearchSummary : federatedSearchResultSummaries) {
				String url = federatedSearchSummary.getURL();
			%>
				<li style="list-style-type: none" />
					<aui:a href="<%= url %>" ><strong><%= federatedSearchSummary.getTitle() %></strong></aui:a><br>
					<div style="font-size: small" ><font color="green"><%= federatedSearchSummary.getURL() %></font></div>
					<div style="font-size: small" ><font color="#6B6C7E"><%= federatedSearchSummary.getContent() %></font></div>
				</li><br>
			<%
			}
		}
	}
}
else {
%>
<liferay-util:include page="/html/portal/portlet_not_setup.jsp" />
<%
}
%>

<script>
function <portlet:namespace />preview(videoId) {
    var url = 'https://www.youtube.com/embed/' + videoId + '?autoplay=1';

	document.getElementById('<portlet:namespace />embeddedViewer').hidden = false;
    document.getElementById('<portlet:namespace />embeddedViewer').src = url;
}
</script>