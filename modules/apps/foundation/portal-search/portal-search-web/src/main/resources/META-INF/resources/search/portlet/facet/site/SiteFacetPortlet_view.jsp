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

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.portal.kernel.util.HtmlUtil" %>
<%@ page import="com.liferay.portal.kernel.util.StringPool" %>
<%@ page import="com.liferay.portal.search.web.internal.portlet.facet.site.SiteFacetPortletDisplayContext" %>
<%@ page import="com.liferay.portal.search.web.internal.portlet.facet.site.SiteFacetPortletTermDisplayContext" %>
<%@ page import="com.liferay.portal.search.web.internal.util.NamespaceUtil" %>

<portlet:defineObjects />

<style>
	.facet-checkbox-label {
		display: block;
	}
</style>

<%
SiteFacetPortletDisplayContext siteFacetPortletDisplayContext =
	(SiteFacetPortletDisplayContext)java.util.Objects.requireNonNull(
		request.getAttribute(SiteFacetPortletDisplayContext.ATTRIBUTE));

String namespace = NamespaceUtil.randomNamespace("portlet_search_facet_site", request);

String cssClassFacetTerm = "facet-term-" + namespace;
%>

<c:choose>
	<c:when test="<%= siteFacetPortletDisplayContext.isRenderNothing() %>">
		<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(siteFacetPortletDisplayContext.getFieldParamInputName()) %>" type="hidden" value="<%= siteFacetPortletDisplayContext.getFieldParamInputValue() %>" />
	</c:when>
	<c:otherwise>
		<liferay-ui:panel-container extended="true" id='<%= namespace + "facetScopePanelContainer" %>' markupView="lexicon" persistState="true">
			<liferay-ui:panel collapsible="true" cssClass="search-facet" id='<%= namespace + "facetScopePanel" %>' markupView="lexicon" persistState="true" title="portlet.site-facet.title">
				<aui:form method="post" name="siteFacetForm">
					<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(siteFacetPortletDisplayContext.getFieldParamInputName()) %>" type="hidden" value="<%= siteFacetPortletDisplayContext.getFieldParamInputValue() %>" />

					<c:if test="<%= !siteFacetPortletDisplayContext.isNothingSelected() %>">
						<aui:a cssClass="text-default" href="javascript:;" onClick='<%= namespace + "_clearFacet('" + siteFacetPortletDisplayContext.getFieldParamInputName() + "');" %>'><liferay-ui:message key="portlet.site-facet.clear" /></aui:a>
					</c:if>

					<aui:fieldset>

						<%
						boolean multipleSelection = siteFacetPortletDisplayContext.isMultipleSelection();

						int i = 1;

						for (SiteFacetPortletTermDisplayContext siteFacetPortletTermDisplayContext : siteFacetPortletDisplayContext.getTermDisplayContexts()) {
							String termName = "term_" + i++;
						%>

							<label class="facet-checkbox-label" for="<portlet:namespace /><%= termName %>">
								<input
									class="<%= cssClassFacetTerm %>"
									data-term-id="<%= siteFacetPortletTermDisplayContext.getValue() %>"
									id="<portlet:namespace /><%= termName %>"
									name="<portlet:namespace /><%= termName %>"
									onChange='<%= renderResponse.getNamespace() + (multipleSelection ? "_applyFacet(event);" : "_applySingleFacet(event);") %>'
									type="checkbox"
									<%= siteFacetPortletTermDisplayContext.isSelected() ? "checked" : StringPool.BLANK %>
								/>

								<span class="term-name">
									<%= HtmlUtil.escape(siteFacetPortletTermDisplayContext.getTerm()) %>
								</span>

								<c:if test="<%= siteFacetPortletTermDisplayContext.isFrequencyVisible() %>">
									<small class="term-count">
										(<%= siteFacetPortletTermDisplayContext.getFrequency() %>)
									</small>
								</c:if>
							</label>

						<%
						}
						%>

					</aui:fieldset>
				</aui:form>
			</liferay-ui:panel>
		</liferay-ui:panel-container>
	</c:otherwise>
</c:choose>

<aui:script>
	function <%= namespace %>_removeParameters(key, parameterArray) {
		key = encodeURI(key);

		var newParameters = [];

		AUI.$.each(
			parameterArray,
			function(index, item) {
				var itemSplit = item.split('=');

				if (itemSplit) {
					if (itemSplit[0] != key) {
						newParameters.push(item);
					}
				}
			}
		);

		return newParameters;
	}

	function <%= namespace %>_addParameter(key, value, parameterArray) {
		key = encodeURI(key);
		value = encodeURI(value);

		parameterArray[parameterArray.length] = [key, value].join('=');

		return parameterArray;
	}

	function <%= namespace %>_clearFacet(facetName) {
		var parameterArray = document.location.search.substr(1).split('&');

		var newParameters = <%= namespace %>_removeParameters(facetName, parameterArray);

		document.location.search = newParameters.join('&');
	}

	function <%= namespace %>_setParameters(selectedFacets){
		var key = '<%= siteFacetPortletDisplayContext.getFieldParamInputName() %>';

		var parameterArray = document.location.search.substr(1).split('&');

		var newParameters = <%= namespace %>_removeParameters(key, parameterArray);

		if (selectedFacets.length > 0) {
			newParameters = <%= namespace %>_addParameter(key, selectedFacets.join(','), newParameters);
		}

		document.location.search = newParameters.join('&');
	}

	Liferay.provide(
		window,
		'<portlet:namespace />_applyFacet',
		function(event) {
			var form = event.currentTarget.form;

			if (form) {
				var formCheckboxes = $('#' + form.id + ' input.' + '<%= cssClassFacetTerm %>');

				var selectedFacets = [];

				formCheckboxes.each(
					function(index, value) {
						if (value.checked) {
							var termId = value.getAttribute('data-term-id');

							selectedFacets.push(termId);
						}
					}
				);

				<%= namespace %>_setParameters(selectedFacets);
			}
		},
		['aui-base']
	);

	Liferay.provide(
		window,
		'<portlet:namespace />_applySingleFacet',
		function(event) {

			var form = event.currentTarget.form;

			if (form) {
				var checkbox = $('#' + event.currentTarget.name);

				var checked = checkbox.prop('checked');

				var selectedFacets = [];

				if (checked) {
					selectedFacets.push(event.currentTarget.getAttribute('data-term-id'));
				}

				<%= namespace %>_setParameters(selectedFacets);
			}
		},
		['aui-base']
	);
</aui:script>