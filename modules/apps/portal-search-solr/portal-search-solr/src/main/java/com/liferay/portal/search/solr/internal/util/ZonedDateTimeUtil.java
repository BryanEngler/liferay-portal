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

package com.liferay.portal.search.solr.internal.util;

import com.liferay.portal.kernel.util.StringUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * @author Eric Yan
 */
public class ZonedDateTimeUtil {

	public static String formatDate(
		String[] fromPatterns, String toPattern, String dateString,
		ZoneId fromTimeZoneId, ZoneId toTimeZoneId) {

		ZonedDateTime zonedDateTime = parseDate(
			fromPatterns, dateString, fromTimeZoneId);

		zonedDateTime = zonedDateTime.withZoneSameInstant(toTimeZoneId);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
			toPattern);

		return zonedDateTime.format(dateTimeFormatter);
	}

	public static DateTimeFormatter getLocalDateTimeFormatter(
		String... patterns) {

		DateTimeFormatterBuilder dateTimeFormatterBuilder =
			new DateTimeFormatterBuilder();

		for (String pattern : patterns) {
			dateTimeFormatterBuilder.appendOptional(
				DateTimeFormatter.ofPattern(StringUtil.trim(pattern)));
		}

		dateTimeFormatterBuilder.parseDefaulting(
			ChronoField.MONTH_OF_YEAR, 1
		).parseDefaulting(
			ChronoField.DAY_OF_MONTH, 1
		).parseDefaulting(
			ChronoField.HOUR_OF_DAY, 0
		).parseDefaulting(
			ChronoField.MINUTE_OF_HOUR, 0
		).parseDefaulting(
			ChronoField.SECOND_OF_MINUTE, 0
		);

		return dateTimeFormatterBuilder.toFormatter();
	}

	public static ZonedDateTime parseDate(
		String[] patterns, String dateString, ZoneId timeZoneId) {

		DateTimeFormatter localDateTimeFormatter = getLocalDateTimeFormatter(
			patterns);

		LocalDateTime localDateTime = LocalDateTime.parse(
			dateString, localDateTimeFormatter);

		return localDateTime.atZone(timeZoneId);
	}

}