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

package com.liferay.portal.search.solr.internal.filter;

import com.liferay.portal.kernel.search.filter.DateRangeTermFilter;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.TimeZoneUtil;
import com.liferay.portal.search.solr.filter.DateRangeTermFilterTranslator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.TimeZone;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, service = DateRangeTermFilterTranslator.class)
public class DateRangeTermFilterTranslatorImpl
	implements DateRangeTermFilterTranslator {

	@Override
	public Query translate(DateRangeTermFilter dateRangeTermFilter) {
		String multiDateFormatPattern = dateRangeTermFilter.getDateFormat();
		TimeZone timeZone = dateRangeTermFilter.getTimeZone();

		if ((multiDateFormatPattern == null) || (timeZone == null)) {
			throw new IllegalArgumentException(
				"Date format and/or timezone is null " + dateRangeTermFilter);
		}

		String lowerBound = dateRangeTermFilter.getLowerBound();
		String upperBound = dateRangeTermFilter.getUpperBound();

		if (!_dateFormatPattern.equals(multiDateFormatPattern) ||
			!_TIME_ZONE_ID.equals(timeZone.getID())) {

			try {
				String[] dateFormats = StringUtil.split(
					multiDateFormatPattern, "||");

				if (lowerBound != null) {
					ZonedDateTime lowerBoundZonedDateTime = parseFormattedDate(
						lowerBound, dateFormats, timeZone);

					lowerBound = formatDate(lowerBoundZonedDateTime);
				}

				if (upperBound != null) {
					ZonedDateTime upperBoundZonedDateTime = parseFormattedDate(
						upperBound, dateFormats, timeZone);

					upperBound = formatDate(upperBoundZonedDateTime);
				}
			}
			catch (IllegalArgumentException iae) {
				throw new IllegalArgumentException(
					"Invalid date format " + dateRangeTermFilter, iae);
			}
			catch (Exception e) {
				throw new IllegalArgumentException(
					"Invalid date range " + dateRangeTermFilter, e);
			}
		}

		TermRangeQuery termRangeQuery = TermRangeQuery.newStringRange(
			dateRangeTermFilter.getField(), lowerBound, upperBound,
			dateRangeTermFilter.isIncludesLower(),
			dateRangeTermFilter.isIncludesUpper());

		return termRangeQuery;
	}

	@Activate
	protected void activate() {
		_dateFormatPattern = props.get(PropsKeys.INDEX_DATE_FORMAT_PATTERN);
	}

	protected String formatDate(ZonedDateTime zonedDateTime) {
		zonedDateTime = zonedDateTime.withZoneSameInstant(
			ZoneId.of(_TIME_ZONE_ID));

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
			_dateFormatPattern);

		return zonedDateTime.format(dateTimeFormatter);
	}

	protected ZonedDateTime parseFormattedDate(
		String dateString, String[] dateFormats, TimeZone timeZone) {

		for (int i = 0; i < dateFormats.length; i++) {
			try {
				DateTimeFormatter dateTimeFormatter =
					DateTimeFormatter.ofPattern(dateFormats[i]);

				LocalDateTime localDateTime = LocalDateTime.parse(
					dateString, dateTimeFormatter);

				return localDateTime.atZone(ZoneId.of(timeZone.getID()));
			}
			catch (Exception e) {
				continue;
			}
		}

		return null;
	}

	@Reference
	protected Props props;

	private static final TimeZone _TIME_ZONE = TimeZoneUtil.getDefault();

	private static final String _TIME_ZONE_ID = _TIME_ZONE.getID();

	private String _dateFormatPattern;

}