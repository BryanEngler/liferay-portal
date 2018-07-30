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

package com.liferay.portal.search.solr.internal.facet;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.search.solr.internal.SolrIndexingFixture;
import com.liferay.portal.search.solr.internal.util.DateFormatUtil;
import com.liferay.portal.search.test.util.facet.BaseModifiedFacetTestCase;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;
import com.liferay.portal.util.DateFormatFactoryImpl;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Bryan Engler
 */
public class ModifiedFacetTest extends BaseModifiedFacetTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		setUpDateFormatFactoryUtil();
	}

	@Override
	protected IndexingFixture createIndexingFixture() {
		SolrIndexingFixture solrIndexingFixture = new SolrIndexingFixture();

		solrIndexingFixture.setFacetProcessor(
			new ModifiedFacetProcessor() {
				{
					jsonFactory = new JSONFactoryImpl();
				}
			});

		return solrIndexingFixture;
	}

	@Override
	protected void addDocument(String... values) throws Exception {
		List<String> formattedStrings = new ArrayList<>();

		for (String value : values) {
			formattedStrings.add(
				DateFormatUtil.getFormattedDateString("yyyyMMddHHmmss", value));
		}

		super.addDocument(ArrayUtil.toStringArray(formattedStrings));
	}

	protected void setUpDateFormatFactoryUtil() {
		DateFormatFactoryUtil dateFormatFactoryUtil =
			new DateFormatFactoryUtil();

		dateFormatFactoryUtil.setDateFormatFactory(new DateFormatFactoryImpl());
	}

	@Override
	protected List<String> getExpectedRanges() {
		return Arrays.asList("custom-range=1", "range-one=0", "range-two=1");
	}

	@Override
	protected String getDateMathString() {
		return "[NOW-500YEARS TO NOW]";
	}
}