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
int frequencyThreshold = dataJSONObject.getInt("frequencyThreshold");
boolean showAssetCount = dataJSONObject.getBoolean("showAssetCount", true);

String[] values = new String[0];

if (dataJSONObject.has("values")) {
	JSONArray valuesJSONArray = dataJSONObject.getJSONArray("values");

	values = new String[valuesJSONArray.length()];

	for (int i = 0; i < valuesJSONArray.length(); i++) {
		values[i] = valuesJSONArray.getString(i);
	}
}
%>

<liferay-ui:panel-container extended="<%= true %>" id='<%= randomNamespace + "facetAssetEntriesPanelContainer" %>' markupView="lexicon" persistState="<%= true %>">
	<liferay-ui:panel collapsible="<%= true %>" cssClass="<%= cssClass %>" id='<%= randomNamespace + "facetAssetEntriesPanel" %>' markupView="lexicon" persistState="<%= true %>" title="asset-entries">
		<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(facet.getFieldId()) %>" type="hidden" value="<%= fieldParam %>" />
		<aui:input autocomplete="off" name="inputFacetName" type="hidden" value="assetType" />

		<ul class="asset-type list-unstyled">
			<li class="default facet-value">
				<a class="<%= Validator.isNull(fieldParam) ? "text-primary" : "text-default" %>" data-value="" href="javascript:;"><liferay-ui:message key="<%= HtmlUtil.escape(facetConfiguration.getLabel()) %>" /></a>
			</li>

			<%
			List<String> assetTypes = new SortedArrayList<String>(new ModelResourceComparator(locale));

			for (String className : values) {
				if (assetTypes.contains(className) || !ArrayUtil.contains(values, className)) {
					continue;
				}

				assetTypes.add(className);
			}

			for (String assetType : assetTypes) {
				TermCollector termCollector = facetCollector.getTermCollector(assetType);

				int frequency = 0;

				if (termCollector != null) {
					frequency = termCollector.getFrequency();
				}

				if (frequencyThreshold > frequency) {
					continue;
				}

				AssetRendererFactory<?> assetRendererFactory = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(assetType);
			%>

				<li class="facet-value" name="<%= renderResponse.getNamespace()+"assetType_" + HtmlUtil.escapeAttribute(assetType) %>">
					<a class="<%= fieldParam.equals(termCollector.getTerm()) ? "text-primary" : "text-default" %>" data-value="<%= HtmlUtil.escapeAttribute(assetType) %>" href="javascript:;">
						<%= assetRendererFactory.getTypeName(locale) %>

						<c:if test="<%= showAssetCount %>">
							<span class="frequency">(<%= frequency %>)</span>
						</c:if>
					</a>
				</li>

				<aui:input autocomplete="off"
					name="<%= "assetType_" + HtmlUtil.escapeAttribute(assetType) +"_value" %>"
					type="hidden"
					value="<%= HtmlUtil.escapeAttribute(assetType) %>"
				/>

			<%
			}
			%>

		</ul>
	</liferay-ui:panel>
</liferay-ui:panel-container>