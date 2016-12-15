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

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.portal.kernel.util.HtmlUtil" %>
<%@ page import="com.liferay.portal.search.web.internal.portlet.facet.user.UserFacetPortletDisplayContext" %>
<%@ page import="com.liferay.portal.search.web.internal.portlet.facet.user.UserFacetPortletTermDisplayContext" %>
<%@ page import="com.liferay.portal.search.web.internal.util.NamespaceUtil" %>

<%
UserFacetPortletDisplayContext userFacetPortletDisplayContext = (UserFacetPortletDisplayContext)java.util.Objects.requireNonNull(request.getAttribute(UserFacetPortletDisplayContext.ATTRIBUTE));

String namespace = NamespaceUtil.randomNamespace("portlet_search_facet_user", request);

String cssClassFacetTerm = "facet-term-" + namespace;

String paramName = userFacetPortletDisplayContext.getFieldParamInputName();
%>

<c:choose>
	<c:when test="<%= userFacetPortletDisplayContext.isRenderNothing() %>">
		<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(userFacetPortletDisplayContext.getFieldParamInputName()) %>" type="hidden" value="<%= userFacetPortletDisplayContext.getFieldParamInputValue() %>" />
	</c:when>
	<c:otherwise>
		<liferay-ui:panel-container extended="true" id='<%= namespace + "facetUserPanelContainer" %>' markupView="lexicon" persistState="true">
			<liferay-ui:panel collapsible="true" cssClass="search-facet" id='<%= namespace + "facetUserPanel" %>' markupView="lexicon" persistState="true" title="users">
				<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(userFacetPortletDisplayContext.getFieldParamInputName()) %>" type="hidden" value="<%= userFacetPortletDisplayContext.getFieldParamInputValue() %>" />

				<c:if test="<%= !userFacetPortletDisplayContext.isNothingSelected() %>">
					<p><a class="text-default" data-value="0" href="javascript:;"><liferay-ui:message key="facet-portlet-clear" /></a></p>
				</c:if>

				<%
				int i = 1;

				for (UserFacetPortletTermDisplayContext userFacetPortletTermDisplayContext : userFacetPortletDisplayContext.getTermDisplayContexts()) {
					String termName = "term_" + i++;
				%>

					<aui:input autocomplete="off"
						name='<%= termName + "_value" %>'
						type="hidden"
						value="<%= userFacetPortletTermDisplayContext.getValue() %>"
					/>

					<aui:input
						checked="<%= userFacetPortletTermDisplayContext.isSelected() %>"
						cssClass="<%= cssClassFacetTerm %>"
						label="TODO inline above!"
						name="<%= termName %>"
						type="checkbox"
					>
						<span style="font-size: small; color: black;"><%= HtmlUtil.escape(userFacetPortletTermDisplayContext.getTerm()) %></span>
						<c:if test="<%= userFacetPortletTermDisplayContext.isFrequencyVisible() %>">
							<span style="font-size: x-small; color: gray;">&nbsp;(<%= userFacetPortletTermDisplayContext.getFrequency() %>)</span>
						</c:if>
					</aui:input>

				<%
				}
				%>

			</liferay-ui:panel>
		</liferay-ui:panel-container>
	</c:otherwise>
</c:choose>

<aui:script sandbox="<%= true %>">
	$('<%= "." + cssClassFacetTerm %>').on(
		'click',
		function(event) {
			var term = $(event.currentTarget);

			var inputName = term[0].getAttribute('name') + "_value";
			var input = $("[name='" + inputName + "']");
			var inputVal = input.val();

			<%= namespace %>_insertParam("<%= paramName %>", inputVal);
		}
	);

	function <%= namespace %>_insertParam(key, value) {
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