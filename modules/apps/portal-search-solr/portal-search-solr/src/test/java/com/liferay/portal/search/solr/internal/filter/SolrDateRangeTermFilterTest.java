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
import com.liferay.portal.kernel.search.filter.FilterVisitor;
import com.liferay.portal.search.solr.internal.SolrIndexingFixture;
import com.liferay.portal.search.test.util.filter.BaseDateRangeTermFilterTestCase;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;

import java.time.ZonedDateTime;

import java.util.TimeZone;

import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author Eric Yan
 */
public class SolrDateRangeTermFilterTest
	extends BaseDateRangeTermFilterTestCase {

	@Override
	protected DateRangeTermFilter createDateRangeTermFilter(
		String fieldName, boolean includesLower, boolean includesUpper,
		ZonedDateTime lowerBoundZonedDateTime,
		ZonedDateTime upperBoundZonedDateTime, String dateFormatPattern,
		TimeZone timeZone) {

		DateRangeTermFilter dateRangeTermFilter =
			super.createDateRangeTermFilter(
				fieldName, includesLower, includesUpper,
				lowerBoundZonedDateTime, upperBoundZonedDateTime,
				dateFormatPattern, timeZone);

		return stubDateRangeTermFilter(dateRangeTermFilter);
	}

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		return new SolrIndexingFixture();
	}

	protected DateRangeTermFilter stubDateRangeTermFilter(
		DateRangeTermFilter dateRangeTermFilter) {

		dateRangeTermFilter = Mockito.spy(dateRangeTermFilter);

		DateRangeTermFilterTranslatorImpl dateRangeTermFilterTranslator =
			new DateRangeTermFilterTranslatorImpl();

		dateRangeTermFilterTranslator.props = props;

		dateRangeTermFilterTranslator.activate();

		Mockito.doReturn(
			dateRangeTermFilterTranslator.translate(dateRangeTermFilter)
		).when(
			dateRangeTermFilter
		).accept(
			Matchers.any(FilterVisitor.class)
		);

		return dateRangeTermFilter;
	}

}