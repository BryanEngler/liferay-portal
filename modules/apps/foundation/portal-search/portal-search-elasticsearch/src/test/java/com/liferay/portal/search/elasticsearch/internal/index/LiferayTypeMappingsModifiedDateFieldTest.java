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

package com.liferay.portal.search.elasticsearch.internal.index;

import com.liferay.portal.search.elasticsearch.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch.internal.connection.Index;
import com.liferay.portal.search.elasticsearch.internal.connection.IndexName;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.mapper.MapperParsingException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author Bryan Engler
 */
public class LiferayTypeMappingsModifiedDateFieldTest {

	@Before
	public void setUp() throws Exception {
		Class<?> clazz = getClass();

		_liferayIndexFixture = new LiferayIndexFixture(
			clazz.getSimpleName(), new IndexName(testName.getMethodName()));

		_liferayIndexFixture.setUp();

		_index = _liferayIndexFixture.getIndex();

		_elasticsearchFixture = _liferayIndexFixture.getElasticsearchFixture();
	}

	@After
	public void tearDown() throws Exception {
		_liferayIndexFixture.tearDown();
	}

	@Test
	public void testString() throws Exception {
		index(
			new HashMap<String, Object>() {
				{
					put("modified", "20171115050402");
				}
			});

		assertType("modified", "date");
	}

	@Test
	public void testLong() throws Exception {
		try {
			index(
				new HashMap<String, Object>() {
					{
						put("modified", 20171115050402L);
					}
				});
		}
		catch (MapperParsingException e) {
			return;
		}

		assertType("modified", "date");
	}

	@Test
	public void testDate() throws Exception {
		try {
			index(
				new HashMap<String, Object>() {
					{
						put("modified", new Date());
					}
				});
		}
		catch (MapperParsingException mpe) {
			// IllegalArgumentException[Invalid format: "2017-12-01T20:24:15.353Z"
			// is malformed at "-12-01T20:24:15.353Z"];
			return;
		}

		Assert.fail();
	}

	@Test
	public void testDateString() throws Exception {
		try {
			index(
				new HashMap<String, Object>() {
					{
						put("modified", new Date().toString());
					}
				});
		}
		catch (MapperParsingException mpe) {
			// IllegalArgumentException[Invalid format: "Fri Dec 01 20:28:45 GMT 2017"];
			return;
		}

		Assert.fail();
	}

	@Test
	public void testDateTime() throws Exception {
		try {
			index(
				new HashMap<String, Object>() {
					{
						put("modified", new Date().getTime());
					}
				});
		}
		catch (MapperParsingException mpe) {
			// IllegalFieldValueException[Cannot parse "1512159853360":
			// Value 15 for monthOfYear must be in the range [1,12]];
			return;
		}

		Assert.fail();
	}

	@Rule
	public TestName testName = new TestName();

	protected void assertType(String field, String type) throws Exception {
		FieldMappingAssert.assertType(
			type, field, LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE,
			_index.getName(), _elasticsearchFixture.getIndicesAdminClient());
	}

	protected IndexRequestBuilder getIndexRequestBuilder() {
		Client client = _elasticsearchFixture.getClient();

		IndexRequestBuilder indexRequestBuilder = client.prepareIndex(
			_index.getName(),
			LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE);

		return indexRequestBuilder;
	}

	protected void index(Map<String, Object> map) {
		IndexRequestBuilder indexRequestBuilder = getIndexRequestBuilder();

		indexRequestBuilder.setSource(map);

		indexRequestBuilder.get();
	}

	private ElasticsearchFixture _elasticsearchFixture;
	private Index _index;
	private LiferayIndexFixture _liferayIndexFixture;

}