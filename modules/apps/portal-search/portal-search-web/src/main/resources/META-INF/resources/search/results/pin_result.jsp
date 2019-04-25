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

<%@ page import="com.liferay.frontend.taglib.clay.servlet.taglib.util.SelectOption" %><%@
page import="com.liferay.portal.kernel.language.LanguageUtil" %><%@
page import="com.liferay.portal.kernel.util.Constants" %><%@
page import="com.liferay.portal.kernel.util.ParamUtil" %>

<%@ page import="java.util.ArrayList" %><%@
page import="java.util.List" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/asset" prefix="liferay-asset" %><%@
taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/clay" prefix="clay" %><%@
taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %><%@
taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<liferay-frontend:defineObjects />
<liferay-theme:defineObjects />
<liferay-trash:defineObjects />
<portlet:defineObjects />

<%
String keywords = ParamUtil.getString(request, "keywords");
String index = ParamUtil.getString(request, "index");
String uid = ParamUtil.getString(request, "uid");
String redirect = ParamUtil.getString(request, "redirect");

List<SelectOption> selectOptions = new ArrayList<>();
%>

<%-- need to update selectOptions to contain existing ranking words --%>

<portlet:actionURL name="/result/ranking" var="pinURL">
	<portlet:param name="<%= Constants.CMD %>" value="pin" />
	<portlet:param name="redirect" value="" />
</portlet:actionURL>

<portlet:renderURL var="newRankingURL">
	<portlet:param name="<%= Constants.CMD %>" value="pin" />
	<portlet:param name="redirect" value="" />
	<portlet:param name="index" value="<%= index %>" />
	<portlet:param name="uid" value="<%= uid %>" />
</portlet:renderURL>

<div class="task-action">
	<aui:form action="<%= pinURL %>" method="post" name="pinFm" onSubmit="event.preventDefault();">
		<aui:input name="index" type="hidden" value="<%= index %>" />
		<aui:input name="uid" type="hidden" value="<%= uid %>" />

		<div class="modal-body task-action-content">
			<div class="search-results-search-modal-description">
				<liferay-ui:message key="pin-this-result-description" />
			</div>
			<div class="form-group">
				<clay:select
					label="<%=LanguageUtil.get(request, "ranking") %>"
					name="keywords"
					options="<%= selectOptions %>"
				/>
			</div>
			<div class="form-group">
				<label><%=LanguageUtil.get(request, "or") %></label>
				<div>
					<aui:button
						icon="icon-plus"
						name="newRanking"
						value="new-ranking"
					/>
				</div>
			</div>
        </div>

		<div class="modal-footer">
			<div class="btn-group">
				<div class="btn-group-item">
					<aui:button
						name="close"
						type="cancel"
					/>
				</div>

				<div class="btn-group-item">
					<aui:button
						name="done"
						style="primary"
						type="submit"
						value="done"
					/>
				</div>
			</div>
		</div>
	</aui:form>
</div>

<aui:script use="aui-base,aui-io-request">
	var done = A.one('#<portlet:namespace />done');

	if (done) {
		done.on(
			'click',
			function(event) {
				A.io.request(
					'<%= pinURL.toString() %>',
					{
						form: {id: '<portlet:namespace />pinFm'},
						method: 'POST',
						on: {
							success: function() {
								Liferay.Util.getOpener().<portlet:namespace />refreshPortlet('<%= redirect.toString() %>');
								Liferay.Util.getWindow('<portlet:namespace />pinResultDialog').destroy();
							}
						}
					}
				);
			}
		);
	}

	var newRanking = A.one('#<portlet:namespace />newRanking');

	if (newRanking) {
		newRanking.on(
			'click',
			function(event) {
				Liferay.Util.getOpener().<portlet:namespace />refreshPortlet('<%= newRankingURL %>');
			}
		);
	}
</aui:script>