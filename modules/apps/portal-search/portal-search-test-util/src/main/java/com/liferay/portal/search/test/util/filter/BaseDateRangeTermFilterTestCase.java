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

package com.liferay.portal.search.test.util.filter;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.DateRangeTermFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Eric Yan
 */
public abstract class BaseDateRangeTermFilterTestCase
	extends BaseIndexingTestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();

		mockProps();
	}

	@Test
	public void testBeforeLowerBound() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001123000000";
		String upperBoundDate = null;

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testBeforeRange() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001123000000";
		String upperBoundDate = "20001124000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testBeforeUpperBound() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = null;
		String upperBoundDate = "20001123000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testDateFormat() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String dateFormat = "MMddyyyyHHmmss";

		String lowerBoundDate = "11212000000000";
		String upperBoundDate = "11232000000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);

		dateRangeTermFilter.setDateFormat(dateFormat);

		expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testDateFormatWithMultiplePatterns() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String dateFormat = "MMddyyyyHHmmss || yyyy";

		String lowerBoundDate = "2000";
		String upperBoundDate = "11232000000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);

		dateRangeTermFilter.setDateFormat(dateFormat);

		expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testLowerBoundExclusive() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001122000000";
		String upperBoundDate = null;

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testLowerBoundInclusive() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001122000000";
		String upperBoundDate = null;

		boolean lowerBoundInclusive = true;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testPastLowerBound() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001121000000";
		String upperBoundDate = null;

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testPastRange() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001120000000";
		String upperBoundDate = "20001121000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testPastUpperBound() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = null;
		String upperBoundDate = "20001121000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testRange() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001121000000";
		String upperBoundDate = "20001123000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testRangeExclusive() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001122000000";
		String upperBoundDate = "20001122000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testRangeInclusive() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001122000000";
		String upperBoundDate = "20001122000000";

		boolean lowerBoundInclusive = true;
		boolean upperBoundInclusive = true;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testRangeLowerBoundExclusive() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001122000000";
		String upperBoundDate = "20001123000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testRangeLowerBoundInclusive() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001122000000";
		String upperBoundDate = "20001123000000";

		boolean lowerBoundInclusive = true;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testRangeUpperBoundExclusive() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001121000000";
		String upperBoundDate = "20001122000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testRangeUpperBoundInclusive() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001121000000";
		String upperBoundDate = "20001122000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = true;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testTimeZone() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = "20001122010000";
		String upperBoundDate = "20001122030000";

		TimeZone timeZone = TimeZone.getTimeZone(ZoneId.of("Etc/GMT-2"));

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);

		dateRangeTermFilter.setTimeZone(timeZone);

		expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testUpperBoundExclusive() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = null;
		String upperBoundDate = "20001122000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = false;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList();

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	@Test
	public void testUpperBoundInclusive() throws Exception {
		Date date = getDate(2000, 11, 22, 0, 0, 0);

		addDocument(DocumentCreationHelpers.singleDate(FIELD, date));

		String lowerBoundDate = null;
		String upperBoundDate = "20001122000000";

		boolean lowerBoundInclusive = false;
		boolean upperBoundInclusive = true;

		DateRangeTermFilter dateRangeTermFilter = new DateRangeTermFilter(
			FIELD, lowerBoundInclusive, upperBoundInclusive, lowerBoundDate,
			upperBoundDate);

		List<String> expectedValues = Arrays.asList("20001122000000");

		assertSearch(dateRangeTermFilter, FIELD, expectedValues);
	}

	protected void assertSearch(
			Filter filter, String fieldName, List<String> expectedValues)
		throws Exception {

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			() -> doAssertSearch(filter, fieldName, expectedValues));
	}

	protected Void doAssertSearch(
			Filter filter, String fieldName, List<String> expectedValues)
		throws Exception {

		SearchContext searchContext = createSearchContext();

		Hits hits = search(
			searchContext,
			booleanQuery -> setPreBooleanFilter(filter, booleanQuery));

		DocumentsAssert.assertValues(
			(String)searchContext.getAttribute("queryString"), hits.getDocs(),
			fieldName, expectedValues);

		return null;
	}

	protected Date getDate(
		int year, int month, int date, int hrs, int min, int sec) {

		LocalDateTime localDateTime = LocalDateTime.of(
			year, month, date, hrs, min, sec);

		ZonedDateTime zonedDateTime = ZonedDateTime.of(
			localDateTime, ZoneId.systemDefault());

		return Date.from(zonedDateTime.toInstant());
	}

	protected void mockProps() {
		Mockito.when(
			props.get(PropsKeys.INDEX_DATE_FORMAT_PATTERN)
		).thenReturn(
			"yyyyMMddHHmmss"
		);

		PropsUtil.setProps(props);
	}

	protected void setPreBooleanFilter(Filter filter, Query query) {
		BooleanFilter booleanFilter = new BooleanFilter();

		booleanFilter.add(filter, BooleanClauseOccur.MUST);

		query.setPreBooleanFilter(booleanFilter);
	}

	protected static final String FIELD = Field.EXPIRATION_DATE;

	protected Props props = Mockito.mock(Props.class);

}