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

<liferay-ui:panel-container extended="<%= true %>" id='<%= randomNamespace + "facetAssetTagsPanelContainer" %>' markupView="lexicon" persistState="<%= true %>">
	<liferay-ui:panel collapsible="<%= true %>" cssClass="<%= cssClass %>" id='<%= randomNamespace + "facetAssetTagsPanel" %>' markupView="lexicon" persistState="<%= true %>" title="tags">
		<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(facet.getFieldId()) %>" type="hidden" value="<%= fieldParam %>" />
		<aui:input autocomplete="off" name="inputFacetName" type="hidden" value="tag" />

		<ul class="<%= (showAssetCount && displayStyle.equals("cloud")) ? "tag-cloud" : "tag-list" %> list-unstyled">
			<li class="default facet-value">
				<a class="<%= Validator.isNull(fieldParam) ? "text-primary" : "text-default" %>" data-value="" href="javascript:;"><liferay-ui:message key="<%= HtmlUtil.escape(facetConfiguration.getLabel()) %>" /></a>
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

				int popularity = (int)(1 + ((maxCount - (maxCount - (termCollector.getFrequency() - minCount))) * multiplier));

				if (frequencyThreshold > termCollector.getFrequency()) {
					j--;

					continue;
				}
			%>

				<li class="facet-value tag-popularity-<%= popularity %>" name="<%= renderResponse.getNamespace()+"facet_tag_"+ i %>">
					<a class="<%= fieldParam.equals(termCollector.getTerm()) ? "text-primary" : "text-default" %>" data-value="<%= HtmlUtil.escapeAttribute(termCollector.getTerm()) %>" href="javascript:;">
						<%= HtmlUtil.escape(termCollector.getTerm()) %>

						<c:if test="<%= showAssetCount %>">
							<span class="frequency">(<%= termCollector.getFrequency() %>)</span>
						</c:if>
					</a>
				</li>

				<aui:input autocomplete="off"
					name="<%= "facet_tag_"+ i +"_value" %>"
					type="hidden"
					value="<%= HtmlUtil.escape(termCollector.getTerm()) %>"
				/>

			<%
			}
			%>

		</ul>
	</liferay-ui:panel>
</liferay-ui:panel-container>