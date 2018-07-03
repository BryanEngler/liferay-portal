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

package com.liferay.portal.search.web.internal.modified.facet.builder;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.DateFormatFactory;
import com.liferay.portal.kernel.util.StringUtil;

import java.text.DateFormat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author Adam Brandizzi
 * @author André de Oliveira
 */
public class DateRangeFactory {

	public DateRangeFactory(DateFormatFactory dateFormatFactory) {
		_dateFormatFactory = dateFormatFactory;
	}

	public String getRangeString(String label, Calendar calendar) {
		return replaceAliases(_rangeMap.get(label), calendar);
	}

	public String getRangeString(String from, String to, TimeZone timeZone) {
		StringBundler sb = new StringBundler(5);

		DateFormat localDateFormat = _dateFormatFactory.getSimpleDateFormat(
			"yyyyMMddHHmmss", timeZone);

		DateFormat utcDateFormat = _dateFormatFactory.getSimpleDateFormat(
			"yyyyMMddHHmmss");

		String fromDayString = _normalizeRangeBoundary(from, "000000");
		String toDayString =_normalizeRangeBoundary(to, "235959");

		Date fromUTCDate = null;
		Date toUTCDate = null;

		try {
			fromUTCDate = localDateFormat.parse(fromDayString);
			toUTCDate = localDateFormat.parse(toDayString);
		}
		catch (ParseException e) {

		}

		String fromUTC = utcDateFormat.format(fromUTCDate);
		String toUTC = utcDateFormat.format(toUTCDate);

		sb.append("[");
		sb.append(fromUTC);
		sb.append(" TO ");
		sb.append(toUTC);
		sb.append("]");

		return sb.toString();
	}

	public Map<String, String> getRangeStrings(Calendar calendar) {
		Map<String, String> map = new LinkedHashMap<>();

		for (String label : _rangeMap.keySet()) {
			map.put(label, getRangeString(label, calendar));
		}

		return map;
	}

	public JSONArray replaceAliases(
		JSONArray rangesJSONArray, Calendar calendar, JSONFactory jsonFactory) {

		JSONArray normalizedRangesJSONArray = jsonFactory.createJSONArray();

		for (int i = 0; i < rangesJSONArray.length(); i++) {
			JSONObject rangeJSONObject = rangesJSONArray.getJSONObject(i);

			JSONObject normalizedJSONObject = jsonFactory.createJSONObject();

			normalizedJSONObject.put(
				"label", rangeJSONObject.getString("label"));
			normalizedJSONObject.put(
				"range",
				replaceAliases(rangeJSONObject.getString("range"), calendar));

			normalizedRangesJSONArray.put(normalizedJSONObject);
		}

		return normalizedRangesJSONArray;
	}

	public String replaceAliases(String rangeString, Calendar calendar) {
		Calendar now = (Calendar)calendar.clone();

		Calendar pastHour = (Calendar)now.clone();

		pastHour.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) - 1);

		Calendar past24Hours = (Calendar)now.clone();

		past24Hours.set(
			Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) - 1);

		Calendar pastWeek = (Calendar)now.clone();

		pastWeek.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) - 7);

		Calendar pastMonth = (Calendar)now.clone();

		pastMonth.set(Calendar.MONTH, now.get(Calendar.MONTH) - 1);

		Calendar pastYear = (Calendar)now.clone();

		pastYear.set(Calendar.YEAR, now.get(Calendar.YEAR) - 1);

		DateFormat dateFormat = _dateFormatFactory.getSimpleDateFormat(
			"yyyyMMddHHmmss");

		rangeString = StringUtil.replace(
			rangeString,
			new String[] {
				"past-hour", "past-24-hours", "past-week", "past-month",
				"past-year", "*"
			},
			new String[] {
				dateFormat.format(pastHour.getTime()),
				dateFormat.format(past24Hours.getTime()),
				dateFormat.format(pastWeek.getTime()),
				dateFormat.format(pastMonth.getTime()),
				dateFormat.format(pastYear.getTime()),
				dateFormat.format(now.getTime())
			});

		return rangeString;
	}

	private String _normalizeRangeBoundary(String dateString, String pad) {
		dateString = dateString.replace("-", "");

		return dateString + pad;
	}

	private static final Map<String, String> _rangeMap =
		new LinkedHashMap<String, String>() {
			{
				put("past-hour", "[past-hour TO *]");
				put("past-24-hours", "[past-24-hours TO *]");
				put("past-week", "[past-week TO *]");
				put("past-month", "[past-month TO *]");
				put("past-year", "[past-year TO *]");
			}
		};

	private final DateFormatFactory _dateFormatFactory;

}