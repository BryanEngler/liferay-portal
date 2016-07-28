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

int frequencyThreshold = dataJSONObject.getInt("frequencyThreshold");
int maxTerms = dataJSONObject.getInt("maxTerms", 10);
boolean showAssetCount = dataJSONObject.getBoolean("showAssetCount", true);

Indexer<?> indexer = FolderSearcher.getInstance();

SearchContext searchContext = SearchContextFactory.getInstance(request);
%>

<liferay-ui:panel-container extended="<%= true %>" id='<%= randomNamespace + "facetFoldersPanelContainer" %>' markupView="lexicon" persistState="<%= true %>">
	<liferay-ui:panel collapsible="<%= true %>" cssClass="<%= cssClass %>" id='<%= randomNamespace + "facetFoldersPanel" %>' markupView="lexicon" persistState="<%= true %>" title="distance">
		<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(facet.getFieldId()) %>" type="hidden" value="<%= fieldParam %>" />
		<aui:input autocomplete="off" name="inputFacetName" type="hidden" value="folderId" />

		<ul class="folders list-unstyled">
			<li class="default facet-value">
				<a class="<%= Validator.isNull(fieldParam) ? "text-primary" : "text-default" %>" data-value="" href="javascript:;"><liferay-ui:message key="<%= HtmlUtil.escape(facetConfiguration.getLabel()) %>" /></a>
			</li>

			<%
			long folderId = GetterUtil.getLong(fieldParam);

			for (int i = 0; i < termCollectors.size(); i++) {
				TermCollector termCollector = termCollectors.get(i);

				long curFolderId = GetterUtil.getLong(termCollector.getTerm());

				if (curFolderId == 0) {
					continue;
				}

				searchContext.setFolderIds(new long[] {curFolderId});
				searchContext.setKeywords(StringPool.BLANK);

				Hits results = indexer.search(searchContext);

				if (results.getLength() == 0) {
					continue;
				}

				Document document = results.doc(0);

				Field title = document.getField(Field.TITLE);

				if (((maxTerms > 0) && (i >= maxTerms)) || ((frequencyThreshold > 0) && (frequencyThreshold > termCollector.getFrequency()))) {
					break;
				}
			%>

				<li class="facet-value" name="<%= renderResponse.getNamespace()+"folder_"+curFolderId %>">
					<a class="<%= (folderId == curFolderId) ? "text-primary" : "text-default" %>" data-value="<%= curFolderId %>" href="javascript:;">
						<%= HtmlUtil.escape(title.getValue()) %>

						<c:if test="<%= showAssetCount %>">
							<span class="frequency">(<%= termCollector.getFrequency() %>)</span>
						</c:if>
					</a>
				</li>

				<aui:input autocomplete="off"
					name="<%= "folder_"+curFolderId+"_value" %>"
					type="hidden"
					value="<%= HtmlUtil.escapeAttribute(String.valueOf(curFolderId)) %>"
				/>

			<%
			}
			%>

		</ul>
	</liferay-ui:panel>
</liferay-ui:panel-container>>