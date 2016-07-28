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

<c:if test="<%= !termCollectors.isEmpty() %>">

	<%
	int frequencyThreshold = dataJSONObject.getInt("frequencyThreshold");
	int maxTerms = dataJSONObject.getInt("maxTerms", 10);
	boolean showAssetCount = dataJSONObject.getBoolean("showAssetCount", true);
	%>

	<liferay-ui:panel-container extended="<%= true %>" id='<%= randomNamespace + "facetUserPanelContainer" %>' markupView="lexicon" persistState="<%= true %>">
		<liferay-ui:panel collapsible="<%= true %>" cssClass="<%= cssClass %>" id='<%= randomNamespace + "facetUserPanel" %>' markupView="lexicon" persistState="<%= true %>" title="users">
			<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(facet.getFieldId()) %>" type="hidden" value="<%= fieldParam %>" />
			<aui:input autocomplete="off" name="inputFacetName" type="hidden" value="user" />

			<%
			for (int i = 0; i < termCollectors.size(); i++) {
				TermCollector termCollector = termCollectors.get(i);

				String curUserName = GetterUtil.getString(termCollector.getTerm());

				String checkBoxText = curUserName;

				if (showAssetCount) {
					checkBoxText = LanguageUtil.format(request, "x-(x)", new Object[] {curUserName, termCollector.getFrequency()});
				}

				if (((maxTerms > 0) && (i >= maxTerms)) || ((frequencyThreshold > 0) && (frequencyThreshold > termCollector.getFrequency()))) {
					break;
				}
			%>

				<aui:input cssClass="facet-value" label="<%= checkBoxText %>" name="<%= HtmlUtil.escape(curUserName) %>" type="checkbox" />

				<aui:input autocomplete="off"
					name="<%= HtmlUtil.escape(curUserName) +"_value" %>"
					type="hidden"
					value="<%= HtmlUtil.escapeAttribute(String.valueOf(curUserName)) %>"
				/>

			<%
			}
			%>

		</liferay-ui:panel>
	</liferay-ui:panel-container>
</c:if>