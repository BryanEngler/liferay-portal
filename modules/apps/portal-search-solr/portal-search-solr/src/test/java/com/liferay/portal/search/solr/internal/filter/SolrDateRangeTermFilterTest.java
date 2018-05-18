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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.search.solr.internal.SolrIndexingFixture;
import com.liferay.portal.search.test.util.filter.BaseDateRangeTermFilterTestCase;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * @author Eric Yan
 */
public class SolrDateRangeTermFilterTest
	extends BaseDateRangeTermFilterTestCase {

	@Override
	public void testDateFormat() throws Exception {
		expectedException.expect(SearchException.class);
		expectedException.expectMessage(
			StringBundler.concat(
				"Invalid date range {",
				"({(11212000000000>expirationDate<11232000000000), ",
				"(cached=null, executionOption=null)}), yyyyMMddHHmmss, ",
				"sun.util.calendar.ZoneInfo[id=\"UTC\",offset=0,dstSavings=0,",
				"useDaylight=false,transitions=0,lastRule=null])}"));

		super.testDateFormat();
	}

	@Override
	public void testDateFormatWithMultiplePatterns() throws Exception {
		expectedException.expect(SearchException.class);
		expectedException.expectMessage(
			StringBundler.concat(
				"Invalid date range {({(2000>expirationDate<11232000000000), ",
				"(cached=null, executionOption=null)}), yyyyMMddHHmmss, ",
				"sun.util.calendar.ZoneInfo[id=\"UTC\",offset=0,dstSavings=0,",
				"useDaylight=false,transitions=0,lastRule=null])}"));

		super.testDateFormatWithMultiplePatterns();
	}

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		return new SolrIndexingFixture();
	}

}