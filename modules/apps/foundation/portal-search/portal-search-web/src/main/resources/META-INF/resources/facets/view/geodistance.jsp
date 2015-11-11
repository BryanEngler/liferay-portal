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

<%@ page import="com.liferay.portal.kernel.json.JSONArray" %>
<%@ page import="com.liferay.portal.kernel.json.JSONObject" %>
<%@ page import="com.liferay.portal.kernel.language.LanguageUtil" %>
<%@ page import="com.liferay.portal.kernel.search.facet.Facet" %>
<%@ page import="com.liferay.portal.kernel.search.facet.collector.FacetCollector" %>
<%@ page import="com.liferay.portal.kernel.search.facet.collector.TermCollector" %>
<%@ page import="com.liferay.portal.kernel.search.facet.config.FacetConfiguration" %>
<%@ page import="com.liferay.portal.kernel.util.HtmlUtil" %>
<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>
<%@ page import="com.liferay.portal.kernel.util.StringPool" %>

<%@ include file="/facets/init.jsp" %>

<%
Facet facet1 = facet;
JSONObject dataJSONObject1 = dataJSONObject;
FacetCollector facetCollector1 = facetCollector;
FacetConfiguration facetConfiguration1 = facetConfiguration;
javax.portlet.RenderResponse renderResponse1 = renderResponse;
String randomNamespace1 = randomNamespace;

String fieldParamSelection = ParamUtil.getString(request, facet1.getFieldId() + "selection", "0");

JSONArray rangesJSONArray = dataJSONObject1.getJSONArray("ranges");

String geodistanceLabel = StringPool.BLANK;

int index = 0;

if (fieldParamSelection.equals("0")) {
	geodistanceLabel = LanguageUtil.get(request, HtmlUtil.escape(facetConfiguration1.getLabel()));
}
%>

<div class="<%= cssClass %>" data-facetFieldName="<%= HtmlUtil.escapeAttribute(facet.getFieldId()) %>" id="<%= randomNamespace1 %>facet">
	<aui:input name="<%= HtmlUtil.escapeAttribute(facet1.getFieldId()) %>" type="hidden" value="<%= fieldParam %>" />
	<aui:input name='<%= HtmlUtil.escapeAttribute(facet1.getFieldId()) + "selection" %>' type="hidden" value="<%= fieldParamSelection %>" />

	<aui:field-wrapper cssClass='<%= randomNamespace1 + "calendar calendar_" %>' label="" name="<%= HtmlUtil.escapeAttribute(facet1.getFieldId()) %>">
		<ul class="modified nav nav-pills nav-stacked">
			<li class="default<%= (fieldParamSelection.equals("0") ? " active" : StringPool.BLANK) %> facet-value">

				<%
				String taglibClearFacet = "window['" + renderResponse1.getNamespace() + HtmlUtil.escapeJS(facet1.getFieldId()) + "clearFacet'](0);";
				%>

				<aui:a href="javascript:;" onClick="<%= taglibClearFacet %>">
					<aui:icon image="time" /> <liferay-ui:message key="<%= HtmlUtil.escape(facetConfiguration1.getLabel()) %>" />
				</aui:a>
			</li>

			<%
			for (int i = 0; i < rangesJSONArray.length(); i++) {
				JSONObject rangesJSONObject = rangesJSONArray.getJSONObject(i);

				String label = HtmlUtil.escape(rangesJSONObject.getString("label"));
				String range = rangesJSONObject.getString("range");

				index = (i + 1);

				if (fieldParamSelection.equals(String.valueOf(index))) {
					geodistanceLabel = LanguageUtil.get(request, label);
				}
			%>

				<li class="facet-value<%= fieldParamSelection.equals(String.valueOf(index)) ? " active" : StringPool.BLANK %>">

					<%
					String taglibSetRange = "window['" + renderResponse1.getNamespace() + HtmlUtil.escapeJS(facet1.getFieldId()) + "setRange'](" + index + ", '" + HtmlUtil.escapeJS(range) + "');";
					%>

					<aui:a href="javascript:;" onClick="<%= taglibSetRange %>">
						<liferay-ui:message key="<%= label %>" />

						<%
						TermCollector termCollector = facetCollector1.getTermCollector(range);
						%>

						<c:if test="<%= termCollector != null %>">
							<span class="badge badge-info frequency"><%= termCollector.getFrequency() %></span>
						</c:if>
					</aui:a>
				</li>

			<%
			}
			%>

		</ul>
	</aui:field-wrapper>
</div>

<c:if test='<%= !fieldParamSelection.equals("0") %>'>

	<%
	String fieldName = renderResponse1.getNamespace() + facet1.getFieldId();
	%>

	<aui:script use="liferay-token-list">

		<%
		String tokenLabel = geodistanceLabel;
		%>

		Liferay.Search.tokenList.add(
			{
				clearFields: '<%= HtmlUtil.escape(HtmlUtil.escapeAttribute(fieldName)) %>',
				fieldValues: '<%= HtmlUtil.escape(HtmlUtil.escapeAttribute(fieldName)) + "selection|0" %>',
				html: '<%= tokenLabel %>'
			}
		);
	</aui:script>
</c:if>

<aui:script>
	function <portlet:namespace /><%= HtmlUtil.escapeJS(facet1.getFieldId()) %>clearFacet(selection) {
		var form = AUI.$(document.<portlet:namespace />fm);

		form.fm('<%= HtmlUtil.escapeJS(facet1.getFieldId()) %>').val('');
		form.fm('<%= HtmlUtil.escapeJS(facet1.getFieldId()) %>selection').val(selection);

		submitForm(form);
	}

	function <portlet:namespace /><%= HtmlUtil.escapeJS(facet1.getFieldId()) %>setRange(selection, range) {
		var form = AUI.$(document.<portlet:namespace />fm);

		form.fm('<%= HtmlUtil.escapeJS(facet1.getFieldId()) %>').val(range);
		form.fm('<%= HtmlUtil.escapeJS(facet1.getFieldId()) %>selection').val(selection);

		submitForm(form);
	}
</aui:script>