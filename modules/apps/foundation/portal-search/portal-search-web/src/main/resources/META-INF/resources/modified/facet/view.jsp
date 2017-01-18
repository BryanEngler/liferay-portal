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

<%@ page import="com.liferay.portal.kernel.language.UnicodeLanguageUtil" %><%@
page import="com.liferay.portal.kernel.util.HtmlUtil" %><%@
page import="com.liferay.portal.kernel.util.StringPool" %><%@
page import="com.liferay.portal.kernel.util.WebKeys" %><%@
page import="com.liferay.portal.search.web.internal.modified.facet.display.context.ModifiedFacetCalendarDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.modified.facet.display.context.ModifiedFacetDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.modified.facet.display.context.ModifiedFacetTermDisplayContext" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<portlet:defineObjects />

<style>
	.facet-checkbox-label {
		display: block;
	}
</style>

<%
ModifiedFacetDisplayContext modifiedFacetDisplayContext = (ModifiedFacetDisplayContext)java.util.Objects.requireNonNull(request.getAttribute(WebKeys.PORTLET_DISPLAY_CONTEXT));

ModifiedFacetTermDisplayContext customRangeTermDisplayContext = modifiedFacetDisplayContext.getCustomRangeTermDisplayContext();

ModifiedFacetCalendarDisplayContext modifiedFacetCalendarDisplayContext = modifiedFacetDisplayContext.getModifiedFacetCalendarDisplayContext();

// Because of JavaScript?!?!?

int i = 0;
%>

<liferay-ui:panel-container extended="<%= true %>" id='<%= renderResponse.getNamespace() + "facetModifiedPanelContainer" %>' markupView="lexicon" persistState="<%= true %>">
	<liferay-ui:panel collapsible="<%= true %>" cssClass="search-facet" id='<%= renderResponse.getNamespace() + "facetModifiedPanel" %>' markupView="lexicon" persistState="<%= true %>" title="last-modified">
		<aui:form method="post" name="modifiedFacetForm">
			<aui:input autocomplete="off" name="inputFacetName" type="hidden" value="modified" />
			<aui:input cssClass="facet-parameter-name" name="facet-parameter-name" type="hidden" value="<%= modifiedFacetDisplayContext.getParameterName() %>" />

			<aui:field-wrapper cssClass='<%= renderResponse.getNamespace() + "calendar calendar_" %>' label="" name="<%= HtmlUtil.escapeAttribute(modifiedFacetDisplayContext.getParameterName()) %>">
				<ul class="list-unstyled modified">

					<%
					for (ModifiedFacetTermDisplayContext modifiedFacetTermDisplayContext : modifiedFacetDisplayContext.getTermDisplayContexts()) {
						i++;
					%>

						<li class="facet-value" name="<%= renderResponse.getNamespace() + "ranger_"+i %>">

							<%
							String rangeCssClass = " text-default ";

							if (modifiedFacetTermDisplayContext.isSelected()) {
								rangeCssClass = " text-primary ";
							}
							%>

							<input
								class="facet-term"
								data-term-id="<%= modifiedFacetTermDisplayContext.getRange() %>"
								id="<portlet:namespace /><%= modifiedFacetTermDisplayContext.getLabel() %>"
								name="<portlet:namespace /><%= modifiedFacetTermDisplayContext.getLabel() %>"
								onChange='Liferay.Search.FacetUtil.changeSelection(event);'
								type="checkbox"
								<%= modifiedFacetTermDisplayContext.isSelected() ? "checked" : StringPool.BLANK %>
							/>

							<span class="term-name">
								<liferay-ui:message key="<%= modifiedFacetTermDisplayContext.getLabel() %>" />
							</span>

							<small class="term-count">
								<span class="frequency">(<%= modifiedFacetTermDisplayContext.getFrequency() %>)</span>
							</small>
						</li>

					<%
					}
					%>

					<li class="facet-value">

						<%
						String customRangeCssClass = renderResponse.getNamespace() + "custom-range-toggle";

						if (customRangeTermDisplayContext.isSelected()) {
							customRangeCssClass += " text-primary";
						}
						else {
							customRangeCssClass += " text-default";
						}
						%>

						<aui:a cssClass="<%= customRangeCssClass %>" href="javascript:;">
							<liferay-ui:message key="custom-range" />&hellip;

							<span class="frequency">(<%= customRangeTermDisplayContext.getFrequency() %>)</span>
						</aui:a>
					</li>

					<div class="<%= !modifiedFacetCalendarDisplayContext.isSelected() ? "hide" : StringPool.BLANK %> modified-custom-range" id="<portlet:namespace />customRange">
						<div class="col-md-6" id="<portlet:namespace />customRangeFrom">
							<aui:field-wrapper label="from">
								<liferay-ui:input-date
									dayParam="fromDay"
									dayValue="<%= modifiedFacetCalendarDisplayContext.getFromDayValue() %>"
									disabled="<%= false %>"
									firstDayOfWeek="<%= modifiedFacetCalendarDisplayContext.getFromFirstDayOfWeek() %>"
									monthParam="fromMonth"
									monthValue="<%= modifiedFacetCalendarDisplayContext.getFromMonthValue() %>"
									name="fromInput"
									yearParam="fromYear"
									yearValue="<%= modifiedFacetCalendarDisplayContext.getFromYearValue() %>"
								/>
							</aui:field-wrapper>
						</div>

						<div class="col-md-6" id="<portlet:namespace />customRangeTo">
							<aui:field-wrapper label="to">
								<liferay-ui:input-date
									dayParam="toDay"
									dayValue="<%= modifiedFacetCalendarDisplayContext.getToDayValue() %>"
									disabled="<%= false %>"
									firstDayOfWeek="<%= modifiedFacetCalendarDisplayContext.getToFirstDayOfWeek() %>"
									monthParam="toMonth"
									monthValue="<%= modifiedFacetCalendarDisplayContext.getToMonthValue() %>"
									name="toInput"
									yearParam="toYear"
									yearValue="<%= modifiedFacetCalendarDisplayContext.getToYearValue() %>"
								/>
							</aui:field-wrapper>
						</div>

							<input
								class="DELETETHIS-facet-term"
								data-term-id=""
								id="<portlet:namespace /><%= "customRange" %>"
								name="<portlet:namespace /><%= "customRange" %>"
								onChange='Liferay.Search.FacetUtil.changeSelection(event);'
								type="hidden"
								<%= modifiedFacetCalendarDisplayContext.isSelected() ? "checked" : StringPool.BLANK %>
							/>

						<%
						String taglibSearchCustomRange = "window['" + renderResponse.getNamespace() + HtmlUtil.escapeJS(modifiedFacetDisplayContext.getParameterName()) + "searchCustomRange'](event);";
						%>

						<aui:button disabled="<%= (!modifiedFacetCalendarDisplayContext.isFromBeforeTo()) %>" name="searchCustomRangeButton" onClick="<%= taglibSearchCustomRange %>" value="search" />
					</div>
				</ul>
			</aui:field-wrapper>

			<c:if test="<%= !modifiedFacetDisplayContext.isNothingSelected() %>">
				<aui:a
					cssClass="text-default"
					href="javascript:;"
					onClick="Liferay.Search.FacetUtil.clearSelections(event);"
				>
					<small><liferay-ui:message key="clear" /></small>
				</aui:a>
			</c:if>
		</aui:form>
	</liferay-ui:panel>
</liferay-ui:panel-container>

<aui:script use="liferay-search-facet-util">
	function <portlet:namespace /><%= HtmlUtil.escapeJS(modifiedFacetDisplayContext.getParameterName()) %>searchCustomRange(event) {
		var A = AUI();
		var Lang = A.Lang;
		var LString = Lang.String;

		var form = $(event.currentTarget).closest('form')[0];

		var dayFrom = form.fm('fromDay').val();
		var monthFrom = Lang.toInt(form.fm('fromMonth').val()) + 1;
		var yearFrom = form.fm('fromYear').val();

		var dayTo = form.fm('toDay').val();
		var monthTo = Lang.toInt(form.fm('toMonth').val()) + 1;
		var yearTo = form.fm('toYear').val();

		var range = '[' + yearFrom + LString.padNumber(monthFrom, 2) + LString.padNumber(dayFrom, 2) + '000000 TO ' + yearTo + LString.padNumber(monthTo, 2) + LString.padNumber(dayTo, 2) + '235959]';

		form.fm('customRange').val(range);
		form.fm('customRange').attr("checked", true);
		form.fm('customRange').attr("data-term-id", range);
		form.fm('<%= HtmlUtil.escapeAttribute(modifiedFacetDisplayContext.getParameterName()) + "selection" %>').val('<%= i + 1 %>');
		form.fm('<%= HtmlUtil.escapeAttribute(modifiedFacetDisplayContext.getParameterName()) %>').val(range);

		var selections = [range];

		FacetUtil.setURLParameters(form, selections);
	}
</aui:script>

<aui:script use="aui-form-validator">
	var Util = Liferay.Util;

	var customRangeFrom = Liferay.component('<%= renderResponse.getNamespace() %>modifiedfromDatePicker');
	var customRangeTo = Liferay.component('<%= renderResponse.getNamespace() %>modifiedtoDatePicker');
	var searchButton = A.one('#<portlet:namespace />searchCustomRangeButton');

	var preventKeyboardDateChange = function(event) {
		if (!event.isKey('TAB')) {
			event.preventDefault();
		}
	};

	A.one('#<portlet:namespace /><%= HtmlUtil.escapeJS(modifiedFacetDisplayContext.getParameterName()) %>from').on('keydown', preventKeyboardDateChange);
	A.one('#<portlet:namespace /><%= HtmlUtil.escapeJS(modifiedFacetDisplayContext.getParameterName()) %>to').on('keydown', preventKeyboardDateChange);

	var DEFAULTS_FORM_VALIDATOR = A.config.FormValidator;

	A.mix(
		DEFAULTS_FORM_VALIDATOR.STRINGS,
		{
			<portlet:namespace />dateRange: '<%= UnicodeLanguageUtil.get(request, "search-custom-range-invalid-date-range") %>'
		},
		true
	);

	A.mix(
		DEFAULTS_FORM_VALIDATOR.RULES,
		{
			<portlet:namespace />dateRange: function(val, fieldNode, ruleValue) {
				return A.Date.isGreaterOrEqual(customRangeTo.getDate(), customRangeFrom.getDate());
			}
		},
		true
	);

	var customRangeValidator = new A.FormValidator(
		{
			boundingBox: document.<portlet:namespace />fm,
			fieldContainer: 'div',
			on: {
				errorField: function(event) {
					Util.toggleDisabled(searchButton, true);
				},
				validField: function(event) {
					Util.toggleDisabled(searchButton, false);
				}
			},
			rules: {
				'<portlet:namespace /><%= HtmlUtil.escapeJS(modifiedFacetDisplayContext.getParameterName()) %>from': {
					<portlet:namespace />dateRange: true
				},
				'<portlet:namespace /><%= HtmlUtil.escapeJS(modifiedFacetDisplayContext.getParameterName()) %>to': {
					<portlet:namespace />dateRange: true
				}
			}
		}
	);

	var onRangeSelectionChange = function(event) {
		customRangeValidator.validate();
	};

	customRangeFrom.on('selectionChange', onRangeSelectionChange);
	customRangeTo.on('selectionChange', onRangeSelectionChange);

	A.one('.<portlet:namespace />custom-range-toggle').on(
		'click',
		function(event) {
			event.halt();

			A.one('#<%= renderResponse.getNamespace() + "customRange" %>').toggle();
		}
	);
</aui:script>