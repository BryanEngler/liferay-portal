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

<%@ taglib uri="http://liferay.com/tld/asset" prefix="liferay-asset" %><%@
taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/clay" prefix="clay" %><%@
taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %><%@
taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.frontend.taglib.clay.servlet.taglib.util.SelectOption" %><%@
page import="com.liferay.portal.kernel.language.LanguageUtil" %><%@
page import="com.liferay.portal.kernel.portlet.PortletProvider" %><%@
page import="com.liferay.portal.kernel.portlet.PortletProviderUtil" %><%@
page import="com.liferay.portal.kernel.search.Document" %><%@
page import="com.liferay.portal.kernel.util.Constants" %><%@
page import="com.liferay.portal.kernel.util.ParamUtil" %><%@
page import="com.liferay.portal.search.constants.SearchRankingConstants" %>

<%@ page import="java.util.ArrayList" %><%@
page import="java.util.List" %>

<%@ page import="javax.portlet.PortletURL" %>

<liferay-frontend:defineObjects />

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
String redirect = ParamUtil.getString(request, "redirect");

String cmd = ParamUtil.getString(request, Constants.CMD, SearchRankingConstants.PIN);
String index = ParamUtil.getString(request, "index");
String keywords = ParamUtil.getString(request, "keywords");
String uid = ParamUtil.getString(request, "uid");

List<SelectOption> selectOptions = new ArrayList<>();
%>

<%
PortletURL resultsRankingEditURL = PortletProviderUtil.getPortletURL(request, Document.class.getName(), PortletProvider.Action.EDIT);

resultsRankingEditURL.setParameter("redirect", redirect);
resultsRankingEditURL.setParameter("resultActionCmd", cmd);
resultsRankingEditURL.setParameter("resultActionUid", uid);
%>

<%-- @TODO Update selectOptions to contain existing ranking words --%>

<portlet:actionURL name="/result/ranking" var="actionURL">
	<portlet:param name="<%= Constants.CMD %>" value="<%= cmd %>" />
	<portlet:param name="redirect" value="" />
</portlet:actionURL>

<div class="task-action">
	<aui:form action="<%= actionURL %>" method="post" name="resultRankingFm" onSubmit="event.preventDefault();">
		<aui:input name="index" type="hidden" value="<%= index %>" />
		<aui:input name="uid" type="hidden" value="<%= uid %>" />

		<div class="modal-body task-action-content">
			<div class="search-results-search-modal-description">
				<c:choose>
					<c:when test="<%= cmd == SearchRankingConstants.PIN %>">
						<liferay-ui:message key="pin-this-result-description" />
					</c:when>
					<c:otherwise>
						<liferay-ui:message key="hide-this-result-description" />
					</c:otherwise>
				</c:choose>
			</div>

			<div class="form-group">
				<clay:select
					label="<%=LanguageUtil.get(request, "ranking") %>"
					name="keywords"
					options="<%= selectOptions %>"
				/>
			</div>

			<div class="form-group">
				<label>
					<liferay-ui:message key="or" />
				</label>

				<div>
					<%
					String newRankingRedirect = "Liferay.Util.getOpener()." + renderResponse.getNamespace() + "refreshPortlet('" + resultsRankingEditURL.toString() + "');";
					%>

					<aui:button
						icon="icon-plus"
						name="newRanking"
						onClick="<%= newRankingRedirect %>"
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
					'<%= actionURL.toString() %>',
					{
						form: {
							id: '<portlet:namespace />resultRankingFm'
						},
						method: 'POST',
						on: {
							success: function() {
								Liferay.Util.getOpener().<portlet:namespace />refreshPortlet('<%= redirect.toString() %>');
								Liferay.Util.getWindow('<portlet:namespace />resultRankingDialog').destroy();
							}
						}
					}
				);
			}
		);
	}
</aui:script>