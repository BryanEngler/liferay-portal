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

import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;

import java.time.format.DateTimeFormatter;
import java.text.DateFormat;
import java.util.Date;
import java.text.ParseException;

/**
 * @author Bryan Engler
 */
public class DateFormatUtil {

	public static String getFormattedDateString(
		String format, String dateString) {

		if (dateString == null) {
			return null;
		}

		DateFormat dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			GetterUtil.get(format, "yyyyMMddHHmmss"));

		Date date = null;

		try {
			date = dateFormat.parse(dateString);

			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_INSTANT;

			return dateTimeFormatter.format(date.toInstant());
		}
		catch (ParseException pe) {
			return  dateString;
		}
	}

}