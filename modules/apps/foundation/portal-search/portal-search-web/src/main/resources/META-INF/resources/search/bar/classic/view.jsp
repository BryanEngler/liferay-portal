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
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<portlet:defineObjects />

<%
com.liferay.portal.search.web.internal.search.bar.classic.portlet.SearchBarClassicDisplayContext context =
	new com.liferay.portal.search.web.internal.search.bar.classic.portlet.SearchBarClassicDisplayContext(request, portletPreferences);
%>

<portlet:actionURL name="redirectSearchBar" var="portletURL">
	<portlet:param name="mvcActionCommandName" value="redirectSearchBar" />
</portlet:actionURL>

<%-- --%>

<aui:form action="<%= portletURL %>" method="post" name="fm">
	<aui:fieldset>
		<aui:input cssClass="search-input" inlineField="<%= true %>" label=""
			name="<%= context.getQParameterName() %>" placeholder="search"
			title="search" type="text" value="<%= context.getQ() %>"
		/>

		<aui:field-wrapper inlineField="<%= true %>">
			<liferay-ui:icon cssClass="icon-monospaced" icon="search" markupView="lexicon" onClick="renderResponse.getNamespace() + search();" url="javascript:;" />
		</aui:field-wrapper>
	</aui:fieldset>
</aui:form>

<p style="font-size:0.5em">
Search Bar Classic
</p>