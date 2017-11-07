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

package com.liferay.portal.search.solr.internal.index;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.solr.internal.connection.SolrFixture;
import com.liferay.portal.search.solr.internal.document.SolrSingleFieldFixture;
import com.liferay.portal.search.solr.internal.query.QueryFactories;
import com.liferay.portal.search.test.util.japanese.BaseJapaneseSearchTestCase;

import org.junit.Before;

/**
 * @author Bryan Engler
 */
public class SchemaXmlJapaneseTest extends BaseJapaneseSearchTestCase {

	@Before
	public void setUp() throws Exception {
		singleFieldFixture = new SolrSingleFieldFixture(
			_solrFixture.getClient());

		singleFieldFixture.setField(
			_PREFIX + RandomTestUtil.randomString() + "_ja");
		singleFieldFixture.setSingleFieldQueryFactory(QueryFactories.MATCH);
	}

	private static final String _PREFIX =
		SchemaXmlJapaneseTest.class.getSimpleName() + "_";

	private final SolrFixture _solrFixture = new SolrFixture();

}