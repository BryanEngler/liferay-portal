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
<%@ page import="com.liferay.portal.search.web.internal.facet.display.context.AssetTagsSearchFacetDisplayContext" %>
<%@ page import="com.liferay.portal.search.web.internal.facet.display.context.AssetTagsSearchFacetTermDisplayContext" %>
<%@ page import="com.liferay.portal.search.web.internal.util.NamespaceUtil" %>

<portlet:defineObjects />

<style>
	.facet-checkbox-label {
		display: block;
	}
</style>

<%
AssetTagsSearchFacetDisplayContext assetTagsSearchFacetDisplayContext = (AssetTagsSearchFacetDisplayContext)java.util.Objects.requireNonNull(request.getAttribute(AssetTagsSearchFacetDisplayContext.ATTRIBUTE));

String namespace = NamespaceUtil.randomNamespace("portlet_search_facet_asset_tags", request);

String cssClassFacetTerm = "facet-term-" + namespace;
%>

<c:choose>
	<c:when test="<%= assetTagsSearchFacetDisplayContext.isRenderNothing() %>">
		<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(assetTagsSearchFacetDisplayContext.getParamName()) %>" type="hidden" value="<%= assetTagsSearchFacetDisplayContext.getParamValue() %>" />
	</c:when>
	<c:otherwise>
		<liferay-ui:panel-container extended="true" id='<%= namespace + "facetAssetTagsPanelContainer" %>' markupView="lexicon" persistState="true">
			<liferay-ui:panel collapsible="true" cssClass="search-facet" id='<%= namespace + "facetAssetTagsPanel" %>' markupView="lexicon" persistState="true" title="portlet.tag-facet.title">
				<aui:form method="post" name="assetTagsFacetForm">
					<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(assetTagsSearchFacetDisplayContext.getParamName()) %>" type="hidden" value="<%= assetTagsSearchFacetDisplayContext.getParamValue() %>" />

					<c:if test="<%= !assetTagsSearchFacetDisplayContext.isNothingSelected() %>">
						<aui:a cssClass="text-default" href="javascript:;" onClick='<%= namespace + "_clearFacet('" + assetTagsSearchFacetDisplayContext.getParamName() + "');" %>'><liferay-ui:message key="portlet.tag-facet.clear" /></aui:a>
					</c:if>

					<aui:fieldset>
						<ul class="<%= assetTagsSearchFacetDisplayContext.isCloudWithCount() ? "tag-cloud" : "tag-list" %> list-unstyled">

							<%
							int i = 1;

							for (AssetTagsSearchFacetTermDisplayContext assetTagsSearchFacetTermDisplayContext : assetTagsSearchFacetDisplayContext.getTermDisplayContexts()) {
								String termName = "term_" + i++;
							%>

								<li class="facet-value tag-popularity-<%= assetTagsSearchFacetTermDisplayContext.getPopularity() %>">
									<label class="facet-checkbox-label" for="<portlet:namespace /><%= termName %>">
										<input
											class="<%= cssClassFacetTerm %>"
											data-term-id="<%= assetTagsSearchFacetTermDisplayContext.getValue() %>"
											id="<portlet:namespace /><%= termName %>"
											name="<portlet:namespace /><%= termName %>"
											onChange='<%= renderResponse.getNamespace() + "_applyFacet(event);" %>'
											type="checkbox"
											<%= assetTagsSearchFacetTermDisplayContext.isSelected() ? "checked" : StringPool.BLANK %>
										/>

										<span class="term-name">
											<%= HtmlUtil.escape(assetTagsSearchFacetTermDisplayContext.getDisplayName()) %>
										</span>

										<c:if test="<%= assetTagsSearchFacetTermDisplayContext.isFrequencyVisible() %>">
											<small class="term-count">
												(<%= assetTagsSearchFacetTermDisplayContext.getFrequency() %>)
											</small>
										</c:if>
									</label>
								</li>

							<%
							}
							%>

						</ul>
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

				var key = '<%= assetTagsSearchFacetDisplayContext.getParamName() %>';

				var parameterArray = document.location.search.substr(1).split('&');

				var newParameters = <%= namespace %>_removeParameters(key, parameterArray);

				if (selectedFacets.length > 0) {
					newParameters = <%= namespace %>_addParameter(key, selectedFacets.join(','), newParameters);
				}

				document.location.search = newParameters.join('&');
			}
		},
		['aui-base']
	);
</aui:script>