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

package com.liferay.portal.search.web.internal.modified.facet.display.context;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author André de Oliveira
 */
public class ModifiedFacetCalendarDisplayBuilder {

	public ModifiedFacetCalendarDisplayContext build() {
		if (_rangeString != null) {
			int[] from = parseDate(_rangeString.substring(1, 15));

			_fromDay = from[0];
			_fromMonth = from[1] - 1;
			_fromYear = from[2];
			_fromHour = from[3];
			_fromMinute = from[4];

			int[] to = parseDate(_rangeString.substring(19, 33));

			_toDay = to[0];
			_toMonth = to[1] - 1;
			_toYear = to[2];
			_toHour = to[3];
			_toMinute = to[4];
		}

		ModifiedFacetCalendarDisplayContext
			modifiedFacetCalendarDisplayContext =
				new ModifiedFacetCalendarDisplayContext();

		Date fromDate = null;

		try {
			fromDate = PortalUtil.getDate(
				_fromMonth, _fromDay, _fromYear, _fromHour, _fromMinute, null);
		}
		catch (PortalException pe) {
			if (_log.isDebugEnabled()) {
				_log.debug(pe, pe);
			}
		}

		Date toDate = null;

		try {
			toDate = PortalUtil.getDate(
				_toMonth, _toDay, _toYear, _toHour, _toMinute, null);
		}
		catch (PortalException pe) {
			if (_log.isDebugEnabled()) {
				_log.debug(pe, pe);
			}
		}

		Calendar fromCalendar = CalendarFactoryUtil.getCalendar(
			_timeZone, _locale);

		if (Validator.isNotNull(fromDate)) {
			fromCalendar.setTime(fromDate);
		}
		else {
			fromCalendar.add(Calendar.DATE, -1);
		}

		Calendar toCalendar = CalendarFactoryUtil.getCalendar(
			_timeZone, _locale);

		if (Validator.isNotNull(toDate)) {
			toCalendar.setTime(toDate);
		}

		boolean selected = true;

		modifiedFacetCalendarDisplayContext.setSelected(selected);

		modifiedFacetCalendarDisplayContext.setFromDayValue(
			fromCalendar.get(Calendar.DATE));
		modifiedFacetCalendarDisplayContext.setFromFirstDayOfWeek(
			fromCalendar.getFirstDayOfWeek() - 1);
		modifiedFacetCalendarDisplayContext.setFromMonthValue(
			fromCalendar.get(Calendar.MONTH));
		modifiedFacetCalendarDisplayContext.setFromYearValue(
			fromCalendar.get(Calendar.YEAR));
		modifiedFacetCalendarDisplayContext.setFromAmPmValue(
			fromCalendar.get(Calendar.AM_PM));
		modifiedFacetCalendarDisplayContext.setFromHourValue(
			fromCalendar.get(Calendar.HOUR));
		modifiedFacetCalendarDisplayContext.setFromMinuteValue(
			fromCalendar.get(Calendar.MINUTE));

		modifiedFacetCalendarDisplayContext.setToDayValue(
			toCalendar.get(Calendar.DATE));
		modifiedFacetCalendarDisplayContext.setToFirstDayOfWeek(
			toCalendar.getFirstDayOfWeek() - 1);
		modifiedFacetCalendarDisplayContext.setToMonthValue(
			toCalendar.get(Calendar.MONTH));
		modifiedFacetCalendarDisplayContext.setToYearValue(
			toCalendar.get(Calendar.YEAR));
		modifiedFacetCalendarDisplayContext.setToAmPmValue(
			toCalendar.get(Calendar.AM_PM));
		modifiedFacetCalendarDisplayContext.setToHourValue(
			toCalendar.get(Calendar.HOUR));
		modifiedFacetCalendarDisplayContext.setToMinuteValue(
			toCalendar.get(Calendar.MINUTE));

		boolean fromBeforeTo = false;

		if (fromCalendar.getTimeInMillis() < toCalendar.getTimeInMillis()) {
			fromBeforeTo = true;
		}

		modifiedFacetCalendarDisplayContext.setFromBeforeTo(fromBeforeTo);

		return modifiedFacetCalendarDisplayContext;
	}

	public void setFromDay(int fromDay) {
		_fromDay = fromDay;
	}

	public void setFromHour(int fromHour) {
		_fromHour = fromHour;
	}

	public void setFromMinute(int fromMinute) {
		_fromMinute = fromMinute;
	}

	public void setFromMonth(int fromMonth) {
		_fromMonth = fromMonth;
	}

	public void setFromYear(int fromYear) {
		_fromYear = fromYear;
	}

	public void setLocale(Locale locale) {
		_locale = locale;
	}

	public void setRangeString(String rangeString) {
		_rangeString = rangeString;
	}

	public void setTimeZone(TimeZone timeZone) {
		_timeZone = timeZone;
	}

	public void setToDay(int toDay) {
		_toDay = toDay;
	}

	public void setToHour(int toHour) {
		_toHour = toHour;
	}

	public void setToMinute(int toMinute) {
		_toMinute = toMinute;
	}

	public void setToMonth(int toMonth) {
		_toMonth = toMonth;
	}

	public void setToYear(int toYear) {
		_toYear = toYear;
	}

	protected int[] parseDate(String string) {
		int day = Integer.valueOf(string.substring(6, 8));
		int month = Integer.valueOf(string.substring(4, 6));
		int year = Integer.valueOf(string.substring(0, 4));
		int hour = Integer.valueOf(string.substring(8, 10));
		int minute = Integer.valueOf(string.substring(10, 12));

		return new int[] {day, month, year, hour, minute};
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ModifiedFacetCalendarDisplayBuilder.class);

	private int _fromDay;
	private int _fromHour;
	private int _fromMinute;
	private int _fromMonth;
	private int _fromYear;
	private Locale _locale;
	private String _rangeString;
	private TimeZone _timeZone;
	private int _toDay;
	private int _toHour;
	private int _toMinute;
	private int _toMonth;
	private int _toYear;

}