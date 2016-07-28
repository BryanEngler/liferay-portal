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
if (termCollectors.isEmpty()) {
	return;
}

String displayStyle = dataJSONObject.getString("displayStyle", "cloud");
int frequencyThreshold = dataJSONObject.getInt("frequencyThreshold");
int maxTerms = dataJSONObject.getInt("maxTerms", 10);
boolean showAssetCount = dataJSONObject.getBoolean("showAssetCount", true);
%>

<liferay-ui:panel-container extended="<%= true %>" id='<%= randomNamespace + "facetAssetCategoriesPanelContainer" %>' markupView="lexicon" persistState="<%= true %>">
	<liferay-ui:panel collapsible="<%= true %>" cssClass="<%= cssClass %>" id='<%= randomNamespace + "facetAssetCategoriesPanel" %>' markupView="lexicon" persistState="<%= true %>" title="categories">
		<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(facet.getFieldId()) %>" type="hidden" value="<%= fieldParam %>" />
		<aui:input autocomplete="off" name="inputFacetName" type="hidden" value="assetCategoryId" />

		<ul class="<%= (showAssetCount && displayStyle.equals("cloud")) ? "tag-cloud" : "tag-list" %> list-unstyled">
			<li class="default facet-value">
				<a data-value="<%= Validator.isNull(fieldParam) ? "text-primary" : "text-default" %>" href="javascript:;"><liferay-ui:message key="<%= HtmlUtil.escape(facetConfiguration.getLabel()) %>" /></a>
			</li>

			<%
			int maxCount = 1;
			int minCount = 1;

			if (showAssetCount && displayStyle.equals("cloud")) {

				// The cloud style may not list tags in the order of frequency,
				// so keep looking through the results until we reach the maximum
				// number of terms or we run out of terms.

				for (int i = 0, j = 0; i < termCollectors.size(); i++, j++) {
					if (j >= maxTerms) {
						break;
					}

					TermCollector termCollector = termCollectors.get(i);

					int frequency = termCollector.getFrequency();

					if (frequencyThreshold > frequency) {
						j--;

						continue;
					}

					maxCount = Math.max(maxCount, frequency);
					minCount = Math.min(minCount, frequency);
				}
			}

			double multiplier = 1;

			if (maxCount != minCount) {
				multiplier = (double)5 / (maxCount - minCount);
			}

			for (int i = 0, j = 0; i < termCollectors.size(); i++, j++) {
				if (j >= maxTerms) {
					break;
				}

				TermCollector termCollector = termCollectors.get(i);

				long assetCategoryId = GetterUtil.getLong(termCollector.getTerm());

				if (assetCategoryId == 0) {
					continue;
				}

				AssetCategory curAssetCategory = AssetCategoryLocalServiceUtil.fetchAssetCategory(assetCategoryId);

				if ((curAssetCategory != null) && AssetCategoryPermission.contains(permissionChecker, curAssetCategory, ActionKeys.VIEW)) {
					int popularity = (int)(1 + ((maxCount - (maxCount - (termCollector.getFrequency() - minCount))) * multiplier));

					if (frequencyThreshold > termCollector.getFrequency()) {
						j--;

						continue;
					}
			%>

					<li class="facet-value tag-popularity-<%= popularity %>" name="<%= renderResponse.getNamespace()+"assetCategoryId_"+ String.valueOf(assetCategoryId) %>">
						<a class="<%= fieldParam.equals(termCollector.getTerm()) ? "text-primary" : "text-default" %>" data-value="<%= HtmlUtil.escapeAttribute(String.valueOf(assetCategoryId)) %>" href="javascript:;">
							<%= HtmlUtil.escape(curAssetCategory.getTitle(locale)) %>

							<c:if test="<%= showAssetCount %>">
								<span class="frequency">(<%= termCollector.getFrequency() %>)</span>
							</c:if>
						</a>
					</li>

					<aui:input autocomplete="off"
						name="<%= "assetCategoryId_"+ String.valueOf(assetCategoryId)+"_value" %>"
						type="hidden"
						value="<%= HtmlUtil.escapeAttribute(String.valueOf(assetCategoryId)) %>"
					/>

			<%
				}
			}
			%>

		</ul>
	</liferay-ui:panel>
</liferay-ui:panel-container>