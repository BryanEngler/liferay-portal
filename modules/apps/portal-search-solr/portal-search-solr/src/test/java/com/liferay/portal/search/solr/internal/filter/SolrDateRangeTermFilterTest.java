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
			"Invalid date string: Text '11212000000000' could not be parsed: " +
				"Invalid value for MonthOfYear (valid values 1 - 12): 20");

		super.testDateFormat();
	}

	@Override
	public void testDateFormatWithMultiplePatterns() throws Exception {
		expectedException.expect(SearchException.class);
		expectedException.expectMessage(
			"Invalid date string: Text '2000' could not be parsed, unparsed " +
				"text found at index 0");

		super.testDateFormatWithMultiplePatterns();
	}

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		return new SolrIndexingFixture();
	}

}