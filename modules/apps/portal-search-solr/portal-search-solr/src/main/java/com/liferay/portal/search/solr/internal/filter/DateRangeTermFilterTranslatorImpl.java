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
import com.liferay.portal.search.solr.internal.util.ZonedDateTimeUtil;

import java.time.ZoneId;

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
		String dateFormat = dateRangeTermFilter.getDateFormat();
		String lowerBound = dateRangeTermFilter.getLowerBound();
		TimeZone timeZone = dateRangeTermFilter.getTimeZone();
		String upperBound = dateRangeTermFilter.getUpperBound();

		try {
			String[] dateFormats = StringUtil.split(
				dateFormat, _DATE_FORMAT_SEPARATOR);

			ZoneId fromTimeZoneId = timeZone.toZoneId();
			ZoneId toTimeZoneId = _TIME_ZONE.toZoneId();

			if (lowerBound != null) {
				lowerBound = ZonedDateTimeUtil.formatDate(
					dateFormats, _dateFormatPattern, lowerBound, fromTimeZoneId,
					toTimeZoneId);
			}

			if (upperBound != null) {
				upperBound = ZonedDateTimeUtil.formatDate(
					dateFormats, _dateFormatPattern, upperBound, fromTimeZoneId,
					toTimeZoneId);
			}
		}
		catch (Exception e) {
			throw new IllegalArgumentException(
				"Invalid date range " + dateRangeTermFilter, e);
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

	@Reference
	protected Props props;

	private static final String _DATE_FORMAT_SEPARATOR = "||";

	private static final TimeZone _TIME_ZONE = TimeZoneUtil.getDefault();

	private String _dateFormatPattern;

}