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
<%@ page import="com.liferay.portal.search.web.internal.facet.display.context.AssetCategoriesSearchFacetDisplayContext" %>
<%@ page import="com.liferay.portal.search.web.internal.facet.display.context.AssetCategoriesSearchFacetTermDisplayContext" %>
<%@ page import="com.liferay.portal.search.web.internal.util.NamespaceUtil" %>

<portlet:defineObjects />

<style>
	.facet-checkbox-label {
		display: block;
	}
</style>

<%
AssetCategoriesSearchFacetDisplayContext assetCategoriesSearchFacetDisplayContext = (AssetCategoriesSearchFacetDisplayContext)java.util.Objects.requireNonNull(request.getAttribute(AssetCategoriesSearchFacetDisplayContext.ATTRIBUTE));

String namespace = NamespaceUtil.randomNamespace("portlet_search_facet_category", request);

String cssClassFacetTerm = "facet-term-" + namespace;
%>

<c:choose>
	<c:when test="<%= assetCategoriesSearchFacetDisplayContext.isRenderNothing() %>">
		<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(assetCategoriesSearchFacetDisplayContext.getParamName()) %>" type="hidden" value="<%= assetCategoriesSearchFacetDisplayContext.getParamValue() %>" />
	</c:when>
	<c:otherwise>
		<liferay-ui:panel-container extended="true" id='<%= namespace + "facetAssetCategoriesPanelContainer" %>' markupView="lexicon" persistState="true">
			<liferay-ui:panel collapsible="true" cssClass="search-facet" id='<%= namespace + "facetAssetCategoriesPanel" %>' markupView="lexicon" persistState="true" title="portlet.category-facet.title">
				<aui:form method="post" name="categoryFacetForm">
					<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(assetCategoriesSearchFacetDisplayContext.getParamName()) %>" type="hidden" value="<%= assetCategoriesSearchFacetDisplayContext.getParamValue() %>" />

					<c:if test="<%= !assetCategoriesSearchFacetDisplayContext.isNothingSelected() %>">
						<aui:a cssClass="text-default" href="javascript:;" onClick='<%= namespace + "_clearFacet('" + assetCategoriesSearchFacetDisplayContext.getParamName() + "');" %>'><liferay-ui:message key="portlet.category-facet.clear" /></aui:a>
					</c:if>

					<aui:fieldset>
						<ul class="<%= assetCategoriesSearchFacetDisplayContext.getDisplayStyle().equals("cloud") ? "tag-cloud" : "tag-list" %> list-unstyled">

							<%
							int i = 1;

							for (AssetCategoriesSearchFacetTermDisplayContext assetCategoriesSearchFacetTermDisplayContext : assetCategoriesSearchFacetDisplayContext.getTermDisplayContexts()) {
								String termName = "term_" + i++;
							%>

								<li class="facet-value tag-popularity-<%= assetCategoriesSearchFacetTermDisplayContext.getPopularity() %>">
									<label class="facet-checkbox-label" for="<portlet:namespace /><%= termName %>">
										<input
											class="<%= cssClassFacetTerm %>"
											data-term-id="<%= assetCategoriesSearchFacetTermDisplayContext.getAssetCategoryId() %>"
											id="<portlet:namespace /><%= termName %>"
											name="<portlet:namespace /><%= termName %>"
											onChange='<%= renderResponse.getNamespace() + "_applyFacet(event);" %>'
											type="checkbox"
											<%= assetCategoriesSearchFacetTermDisplayContext.isSelected() ? "checked" : StringPool.BLANK %>
										/>

										<span class="term-name">
											<%= HtmlUtil.escape(assetCategoriesSearchFacetTermDisplayContext.getTitle()) %>
										</span>

										<c:if test="<%= assetCategoriesSearchFacetTermDisplayContext.isFrequencyVisible() %>">
											<small class="term-count">
												(<%= assetCategoriesSearchFacetTermDisplayContext.getFrequency() %>)
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

				var key = '<%= assetCategoriesSearchFacetDisplayContext.getParamName() %>';

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