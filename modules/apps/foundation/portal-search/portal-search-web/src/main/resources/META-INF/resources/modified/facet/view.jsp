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
%>

<liferay-ui:panel-container
	extended="<%= true %>"
	id='<%= renderResponse.getNamespace() + "facetModifiedPanelContainer" %>'
	markupView="lexicon"
	persistState="<%= true %>"
>
	<liferay-ui:panel
		collapsible="<%= true %>"
		cssClass="search-facet"
		id='<%= renderResponse.getNamespace() + "facetModifiedPanel" %>'
		markupView="lexicon"
		persistState="<%= true %>"
		title="last-modified"
	>
		<aui:form method="post" name="modifiedFacetForm">
			<aui:input autocomplete="off" name="inputFacetName" type="hidden" value="<%= modifiedFacetDisplayContext.getParameterName() %>" />
			<aui:input cssClass="facet-parameter-name" name="facet-parameter-name" type="hidden" value="<%= modifiedFacetDisplayContext.getParameterName() %>" />

			<aui:field-wrapper cssClass='<%= renderResponse.getNamespace() + "calendar calendar_" %>' label="" name="<%= HtmlUtil.escapeAttribute(modifiedFacetDisplayContext.getParameterName()) %>">
				<ul class="list-unstyled modified">

					<%
					for (ModifiedFacetTermDisplayContext modifiedFacetTermDisplayContext : modifiedFacetDisplayContext.getTermDisplayContexts()) {
					%>

						<li class="facet-value" name="<%= renderResponse.getNamespace() + "range_" + modifiedFacetTermDisplayContext.getLabel() %>">
							<input
								class="facet-term"
								data-term-id="<%= modifiedFacetTermDisplayContext.getRange() %>"
								id="<portlet:namespace /><%= modifiedFacetTermDisplayContext.getLabel() %>"
								name="<portlet:namespace /><%= modifiedFacetTermDisplayContext.getLabel() %>"
								onChange="Liferay.Search.FacetUtil.changeSelection(event);"
								type="checkbox"
								<%= modifiedFacetTermDisplayContext.getFrequency() != 0 && modifiedFacetTermDisplayContext.isSelected() ? "checked" : StringPool.BLANK %>
								<%= modifiedFacetTermDisplayContext.getFrequency() == 0 ? "disabled" : StringPool.BLANK %>
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

					<li class="facet-value" name="<%= renderResponse.getNamespace() + "range_" + customRangeTermDisplayContext.getLabel() %>">
						<input
							class="facet-term"
							data-term-id="<%= customRangeTermDisplayContext.getRange() %>"
							id="<portlet:namespace /><%= customRangeTermDisplayContext.getLabel() %>"
							name="<portlet:namespace /><%= customRangeTermDisplayContext.getLabel() %>"
							onChange="<portlet:namespace />searchCustomRange();"
							type="checkbox"
							<%= customRangeTermDisplayContext.isSelected() ? "checked" : StringPool.BLANK %>
						/>

						<aui:a href="javascript:;" id='<%= customRangeTermDisplayContext.getLabel() + "-toggleLink" %>'>
							<liferay-ui:message key="<%= customRangeTermDisplayContext.getLabel() %>" />&hellip;

							<c:if test="<%= customRangeTermDisplayContext.isSelected() %>">
								<span class="<%= customRangeTermDisplayContext.getLabel() %>-frequency frequency">(<%= customRangeTermDisplayContext.getFrequency() %>)</span>
							</c:if>
						</aui:a>
					</li>

					<div class="<%= !customRangeTermDisplayContext.isSelected() ? "hide" : StringPool.BLANK %> <%= modifiedFacetDisplayContext.getParameterName() %>-<%= customRangeTermDisplayContext.getLabel() %>" id="<portlet:namespace />customRangePickers">
						<div class="col-md-6" id="<portlet:namespace />customRangeFrom">
							<aui:field-wrapper label="from">
								<liferay-ui:input-date
									dayParam="fromDay"
									dayValue="<%= modifiedFacetCalendarDisplayContext.getFromDayValue() %>"
									disabled="<%= false %>"
									firstDayOfWeek="<%= modifiedFacetCalendarDisplayContext.getFromFirstDayOfWeek() %>"
									monthParam="fromMonth"
									monthValue="<%= modifiedFacetCalendarDisplayContext.getFromMonthValue() %>"
									name="fromInputDate"
									yearParam="fromYear"
									yearValue="<%= modifiedFacetCalendarDisplayContext.getFromYearValue() %>"
								/>

								<liferay-ui:input-time
									amPmParam="fromAmPm"
									amPmValue="<%= modifiedFacetCalendarDisplayContext.getFromAmPmValue() %>"
									disabled="<%= false %>"
									hourParam="fromHour"
									hourValue="<%= modifiedFacetCalendarDisplayContext.getFromHourValue() %>"
									minuteParam="fromMinute"
									minuteValue="<%= modifiedFacetCalendarDisplayContext.getFromMinuteValue() %>"
									name="fromInputTime"
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
									name="toInputDate"
									yearParam="toYear"
									yearValue="<%= modifiedFacetCalendarDisplayContext.getToYearValue() %>"
								/>

								<liferay-ui:input-time
									amPmParam="toAmPm"
									amPmValue="<%= modifiedFacetCalendarDisplayContext.getToAmPmValue() %>"
									disabled="<%= false %>"
									hourParam="toHour"
									hourValue="<%= modifiedFacetCalendarDisplayContext.getToHourValue() %>"
									minuteParam="toMinute"
									minuteValue="<%= modifiedFacetCalendarDisplayContext.getToMinuteValue() %>"
									name="toInputTime"
								/>
							</aui:field-wrapper>
						</div>
					</div>
				</ul>
			</aui:field-wrapper>

			<c:if test="<%= !modifiedFacetDisplayContext.isNothingSelected() %>">
				<aui:a cssClass="text-default" href="javascript:;" onClick="Liferay.Search.FacetUtil.clearSelections(event);">
					<small><liferay-ui:message key="clear" /></small>
				</aui:a>
			</c:if>
		</aui:form>
	</liferay-ui:panel>
</liferay-ui:panel-container>

<aui:script>
	function <portlet:namespace />searchCustomRange() {
		var A = AUI();
		var Lang = A.Lang;
		var LString = Lang.String;

		var form = AUI.$(document.<portlet:namespace />modifiedFacetForm);

		var dayFrom = form.fm('fromDay').val();
		var monthFrom = Lang.toInt(form.fm('fromMonth').val()) + 1;
		var yearFrom = form.fm('fromYear').val();
		var amPmFrom = form.fm('fromAmPm').val();
		var hourFrom = Lang.toInt(form.fm('fromHour').val());
		var minuteFrom = form.fm('fromMinute').val();

		if (amPmFrom == '1') {
			hourFrom = hourFrom + 12;
		}

		var dayTo = form.fm('toDay').val();
		var monthTo = Lang.toInt(form.fm('toMonth').val()) + 1;
		var yearTo = form.fm('toYear').val();
		var amPmTo = form.fm('toAmPm').val();
		var hourTo = Lang.toInt(form.fm('toHour').val());
		var minuteTo = form.fm('toMinute').val();

		if (amPmTo == '1') {
			hourTo = hourTo + 12;
		}

		var range = '[' + yearFrom + LString.padNumber(monthFrom, 2) + LString.padNumber(dayFrom, 2) + LString.padNumber(hourFrom, 2) + LString.padNumber(minuteFrom, 2) + '00 TO ' + yearTo + LString.padNumber(monthTo, 2) + LString.padNumber(dayTo, 2) + LString.padNumber(hourTo, 2) + LString.padNumber(minuteTo, 2) + '59]';

		A.one('#<portlet:namespace /><%= customRangeTermDisplayContext.getLabel() %>')._node.setAttribute('data-term-id', range)

		form = AUI.$('#<portlet:namespace /><%= customRangeTermDisplayContext.getLabel() %>').closest('form')[0];

		var formCheckboxes = $('#' + form.id + ' input.facet-term');

		var selections = [];

		formCheckboxes.each(
			function(index, value) {
				if (value.checked) {
					selections.push(value.getAttribute('data-term-id'));
				}
			}
		);

		Liferay.Search.FacetUtil.setURLParameters(form, selections);
	}
</aui:script>

<aui:script use="aui-form-validator">
	var Util = Liferay.Util;

	var customRangeFromDate = Liferay.component('<%= renderResponse.getNamespace() %>fromInputDateDatePicker');
	var customRangeFromTime = Liferay.component('<%= renderResponse.getNamespace() %>fromInputTimeTimePicker');
	var customRangeToDate = Liferay.component('<%= renderResponse.getNamespace() %>toInputDateDatePicker');
	var customRangeToTime = Liferay.component('<%= renderResponse.getNamespace() %>toInputTimeTimePicker');
	var checkbox = A.one('#<portlet:namespace /><%= customRangeTermDisplayContext.getLabel() %>');

	var preventKeyboardDateChange = function(event) {
		if (!event.isKey('TAB')) {
			event.preventDefault();
		}
	};

	A.one('#<portlet:namespace />fromInputDate').on('keydown', preventKeyboardDateChange);
	A.one('#<portlet:namespace />toInputDate').on('keydown', preventKeyboardDateChange);

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
				var fromDate = customRangeFromDate.getDate();
				var fromTime = customRangeFromTime.getTime();

				fromDate.setHours(fromTime.getHours());
				fromDate.setMinutes(fromTime.getMinutes());

				var toDate = customRangeToDate.getDate();
				var toTime = customRangeToTime.getTime();

				toDate.setHours(toTime.getHours());
				toDate.setMinutes(toTime.getMinutes());

				return A.Date.isGreaterOrEqual(toDate, fromDate);
			}
		},
		true
	);

	var customRangeValidator = new A.FormValidator(
		{
			boundingBox: document.<portlet:namespace />modifiedFacetForm,
			fieldContainer: 'div',
			on: {
				errorField: function(event) {
					Util.toggleDisabled(checkbox, true);
				},
				validField: function(event) {
					Util.toggleDisabled(checkbox, false);
				}
			},
			rules: {
				'<portlet:namespace />fromInputTime': {
					<portlet:namespace />dateRange: true
				},
				'<portlet:namespace />toInputTime': {
					<portlet:namespace />dateRange: true
				}
			}
		}
	);

	var onRangeSelectionChange = function(event) {
		customRangeValidator.validate();
	};

	customRangeFromDate.on('selectionChange', onRangeSelectionChange);
	customRangeFromTime.on('selectionChange', onRangeSelectionChange);
	customRangeToDate.on('selectionChange', onRangeSelectionChange);
	customRangeToTime.on('selectionChange', onRangeSelectionChange);

	A.one('#<portlet:namespace /><%= customRangeTermDisplayContext.getLabel() %>-toggleLink').on(
		'click',
		function(event) {
			event.halt();

			A.one('#<portlet:namespace /><%= "customRangePickers" %>').toggle();
		}
	);

	A.one('#<portlet:namespace /><%= "fromInputDate" %>').on(
		'click',
		function(event) {
			A.one('#<portlet:namespace /><%= customRangeTermDisplayContext.getLabel() %>')._node.checked=false;

			var label = A.one('.<%= customRangeTermDisplayContext.getLabel() %>-frequency');

			if (label != null) {
				label._node.innerHTML='';
			}
		}
	);

	A.one('#<portlet:namespace /><%= "fromInputTime" %>').on(
		'click',
		function(event) {
			A.one('#<portlet:namespace /><%= customRangeTermDisplayContext.getLabel() %>')._node.checked=false;

			var label = A.one('.<%= customRangeTermDisplayContext.getLabel() %>-frequency');

			if (label != null) {
				label._node.innerHTML='';
			}
		}
	);

	A.one('#<portlet:namespace /><%= "toInputDate" %>').on(
		'click',
		function(event) {
			A.one('#<portlet:namespace /><%= customRangeTermDisplayContext.getLabel() %>')._node.checked=false;

			var label = A.one('.<%= customRangeTermDisplayContext.getLabel() %>-frequency');

			if (label != null) {
				label._node.innerHTML='';
			}
		}
	);

	A.one('#<portlet:namespace /><%= "toInputTime" %>').on(
		'click',
		function(event) {
			A.one('#<portlet:namespace /><%= customRangeTermDisplayContext.getLabel() %>')._node.checked=false;

			var label = A.one('.<%= customRangeTermDisplayContext.getLabel() %>-frequency');

			if (label != null) {
				label._node.innerHTML='';
			}
		}
	);
</aui:script>