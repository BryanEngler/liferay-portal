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

import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.search.solr.internal.SolrIndexingFixture;
import com.liferay.portal.search.test.util.filter.BaseDateRangeFilterTestCase;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;

import com.liferay.portal.util.DateFormatFactoryImpl;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Eric Yan
 */
public class SolrDateRangeFilterTest extends BaseDateRangeFilterTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		setUpDateFormatFactoryUtil();
	}

	@Test
	public void testMalformed() throws Exception {
		addDocument(getDate(2000, 11, 22));

		dateRangeFilterBuilder.setFrom("11212000000000");
		dateRangeFilterBuilder.setTo("11232000000000");

		assertNoHits();
	}

	@Test
	public void testMalformedMultiple() throws Exception {
		expectedException.expect(HttpSolrClient.RemoteSolrException.class);
		expectedException.expectMessage("Invalid Date String:'2000'");

		addDocument(getDate(2000, 11, 22));

		dateRangeFilterBuilder.setFrom("2000");
		dateRangeFilterBuilder.setTo("11232000000000");

		assertNoHits();
	}

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		return new SolrIndexingFixture();
	}

	protected void setUpDateFormatFactoryUtil() {
		DateFormatFactoryUtil dateFormatFactoryUtil =
			new DateFormatFactoryUtil();

		dateFormatFactoryUtil.setDateFormatFactory(new DateFormatFactoryImpl());
	}

	@Rule
	public ExpectedException expectedException = ExpectedException.none();


}