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

<%@ include file="/components/facets/init.jsp" %>

<%
if (Validator.isNull(fieldParam)) {
	fieldParam = String.valueOf(searchDisplayContext.getSearchScopeGroupId());
}

ScopeSearchFacetDisplayContext scopeSearchFacetDisplayContext = new ScopeSearchFacetDisplayContext(facet, fieldParam, locale, dataJSONObject.getInt("frequencyThreshold"), dataJSONObject.getInt("maxTerms"), dataJSONObject.getBoolean("showAssetCount", true), GroupLocalServiceUtil.getService());
%>

<c:choose>
	<c:when test="<%= scopeSearchFacetDisplayContext.isRenderNothing() %>">
		<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(scopeSearchFacetDisplayContext.getFieldParamInputName()) %>" type="hidden" value="<%= scopeSearchFacetDisplayContext.getFieldParamInputValue() %>" />
	</c:when>
	<c:otherwise>
		<liferay-ui:panel-container extended="<%= true %>" id='<%= randomNamespace + "facetScopePanelContainer" %>' markupView="lexicon" persistState="<%= true %>">
			<liferay-ui:panel collapsible="<%= true %>" cssClass="<%= cssClass %>" id='<%= randomNamespace + "facetScopePanel" %>' markupView="lexicon" persistState="<%= true %>" title="sites">
				<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(scopeSearchFacetDisplayContext.getFieldParamInputName()) %>" type="hidden" value="<%= scopeSearchFacetDisplayContext.getFieldParamInputValue() %>" />
				<aui:input autocomplete="off" name="inputFacetName" type="hidden" value="groupId" />

					<%
					List<ScopeSearchFacetTermDisplayContext> scopeSearchFacetTermDisplayContexts = scopeSearchFacetDisplayContext.getTermDisplayContexts();

					for (ScopeSearchFacetTermDisplayContext scopeSearchFacetTermDisplayContext : scopeSearchFacetTermDisplayContexts) {
						String checkBoxText = scopeSearchFacetTermDisplayContext.getDescriptiveName();

						if (scopeSearchFacetTermDisplayContext.isShowCount()) {
							checkBoxText = LanguageUtil.format(request, "x-(x)", new Object[] {scopeSearchFacetTermDisplayContext.getDescriptiveName(), scopeSearchFacetTermDisplayContext.getCount()});
						}
					%>

						<aui:input cssClass="facet-value" label='<%= checkBoxText %>'
							name="<%= HtmlUtil.escape(scopeSearchFacetTermDisplayContext.getDescriptiveName()) %>" type="checkbox"
						/>

						<aui:input autocomplete="off"
							name="<%= HtmlUtil.escape(scopeSearchFacetTermDisplayContext.getDescriptiveName()) + "_value" %>"
							type="hidden"
							value="<%= scopeSearchFacetTermDisplayContext.getGroupId() %>"
						/>

					<%
					}
					%>

				</ul>
			</liferay-ui:panel>
		</liferay-ui:panel-container>
	</c:otherwise>
</c:choose>