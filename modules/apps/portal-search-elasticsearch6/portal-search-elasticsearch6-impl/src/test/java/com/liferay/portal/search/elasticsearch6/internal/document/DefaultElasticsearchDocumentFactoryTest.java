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

package com.liferay.portal.search.elasticsearch6.internal.document;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.internal.document.DocumentBuilderImpl;
import com.liferay.portal.search.test.util.indexing.DocumentFixture;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André de Oliveira
 */
public class DefaultElasticsearchDocumentFactoryTest {

	@Before
	public void setUp() throws Exception {
		_documentFixture.setUp();

		_elasticsearchDocumentFactory =
			new DefaultElasticsearchDocumentFactory();
	}

	@After
	public void tearDown() throws Exception {
		_documentFixture.tearDown();
	}

	@Test
	public void testArrayOfArrays() throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder(
		).startObject(
		).field(
			"alpha"
		).startArray(
		).startArray(
		).value(
			"one"
		).value(
			"two"
		).value(
			"three"
		).endArray(
		).startArray(
		).value(
			"four"
		).value(
			"five"
		).value(
			"six"
		).endArray(
		).endArray(
		).endObject();

		String expected = Strings.toString(builder);

		DocumentBuilder documentBuilder = new DocumentBuilderImpl();

		List<Object> valueList1 = new ArrayList<>();

		valueList1.add("one");
		valueList1.add("two");
		valueList1.add("three");

		List<Object> valueList2 = new ArrayList<>();

		valueList2.add("four");
		valueList2.add("five");
		valueList2.add("six");

		List<Object> combinedValueList = new ArrayList<>();

		combinedValueList.add(valueList1);
		combinedValueList.add(valueList2);

		documentBuilder.setValues("alpha", combinedValueList);

		String actual = Strings.toString(
			_elasticsearchDocumentFactory.getElasticsearchDocument(
				documentBuilder.build()));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testArrayOfObjects() throws IOException {
		String expected =
			"{\"group\":\"fans\",\"user\":[{\"last\":\"Smith\",\"first\":" +
			"\"John\"},{\"last\":\"White\",\"first\":\"Alice\"}]}";

		DocumentBuilder documentBuilder = new DocumentBuilderImpl();

		documentBuilder.setString("group", "fans");

		List<Map<String, Object>> userFieldInnerObjectList = new ArrayList<>();

		Map<String, Object> userFieldJohnSmithInnerObject = new HashMap<>();

		userFieldJohnSmithInnerObject.put("first", "John");
		userFieldJohnSmithInnerObject.put("last", "Smith");

		userFieldInnerObjectList.add(userFieldJohnSmithInnerObject);

		Map<String, Object> userFieldAliceWhiteInnerObject = new HashMap<>();

		userFieldAliceWhiteInnerObject.put("first", "Alice");
		userFieldAliceWhiteInnerObject.put("last", "White");

		userFieldInnerObjectList.add(userFieldAliceWhiteInnerObject);

		documentBuilder.setValue("user", userFieldInnerObjectList);

		String actual = Strings.toString(
			_elasticsearchDocumentFactory.getElasticsearchDocument(
				documentBuilder.build()));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testInnerObject() throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder(
		).startObject(
		).field(
			"alpha"
		).startObject(
		).field(
			"position", "1"
		).endObject(
		).endObject();

		String expected = Strings.toString(builder);

		DocumentBuilder documentBuilder = new DocumentBuilderImpl();

		Map<String, Object> positionFieldInnerObject = new HashMap<>();

		positionFieldInnerObject.put("position", "1");

		documentBuilder.setValue("alpha", positionFieldInnerObject);

		String actual = Strings.toString(
			_elasticsearchDocumentFactory.getElasticsearchDocument(
				documentBuilder.build()));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testMultipleInnerObjects() throws IOException {
		String expected =
			"{\"region\":\"US\",\"manager\":{\"name\":{\"last\":\"Smith\"," +
				"\"first\":\"John\"},\"age\":30}}";

		DocumentBuilder documentBuilder = new DocumentBuilderImpl();

		Map<String, Object> nameFieldInnerObject = new HashMap<>();

		nameFieldInnerObject.put("first", "John");
		nameFieldInnerObject.put("last", "Smith");

		Map<String, Object> managerFieldInnerObject = new HashMap<>();

		managerFieldInnerObject.put("age", 30);
		managerFieldInnerObject.put("name", nameFieldInnerObject);

		documentBuilder.setString("region", "US");
		documentBuilder.setValue("manager", managerFieldInnerObject);

		String actual = Strings.toString(
			_elasticsearchDocumentFactory.getElasticsearchDocument(
				documentBuilder.build()));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testMultipleValuesSetAsStrings() throws IOException {
		String expected = "{\"alpha\":[\"one\",\"two\",\"three\"]}";

		DocumentBuilder documentBuilder = new DocumentBuilderImpl();

		documentBuilder.setStrings("alpha", "one", "two", "three");

		String actual = Strings.toString(
			_elasticsearchDocumentFactory.getElasticsearchDocument(
				documentBuilder.build()));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testMultipleValuesSetAsValue() throws IOException {
		String expected = "{\"alpha\":[\"one\",\"two\",\"three\"]}";

		DocumentBuilder documentBuilder = new DocumentBuilderImpl();

		List<Object> valueList = new ArrayList<>();

		valueList.add("one");
		valueList.add("two");
		valueList.add("three");

		documentBuilder.setValue("alpha", valueList);

		String actual = Strings.toString(
			_elasticsearchDocumentFactory.getElasticsearchDocument(
				documentBuilder.build()));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testMultipleValuesSetAsValues() throws IOException {
		String expected = "{\"alpha\":[\"one\",\"two\",\"three\"]}";

		DocumentBuilder documentBuilder = new DocumentBuilderImpl();

		List<Object> valueList = new ArrayList<>();

		valueList.add("one");
		valueList.add("two");
		valueList.add("three");

		documentBuilder.setValues("alpha", valueList);

		String actual = Strings.toString(
			_elasticsearchDocumentFactory.getElasticsearchDocument(
				documentBuilder.build()));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testNull() throws Exception {
		assertDocument(null, "{\"field\":null}");
	}

	@Test
	public void testNullLegacy() throws Exception {
		assertDocumentLegacy(null, "{}");
	}

	@Test
	public void testSpaces() throws Exception {
		assertDocument(StringPool.SPACE, "{\"field\":\" \"}");

		assertDocument(StringPool.THREE_SPACES, "{\"field\":\"   \"}");
	}

	@Test
	public void testSpacesLegacy() throws Exception {
		assertDocumentLegacy(StringPool.SPACE, "{\"field\":\"\"}");

		assertDocumentLegacy(StringPool.THREE_SPACES, "{\"field\":\"\"}");
	}

	@Test
	public void testStringBlank() throws Exception {
		assertDocumentSameAsLegacy(StringPool.BLANK, "{\"field\":\"\"}");
	}

	@Test
	public void testStringNull() throws Exception {
		assertDocumentSameAsLegacy(StringPool.NULL, "{\"field\":\"null\"}");
	}

	protected void assertDocument(String value, String json)
		throws IOException {

		DocumentBuilder documentBuilder = new DocumentBuilderImpl();

		documentBuilder.setStrings(_FIELD, new String[] {value});

		Assert.assertEquals(
			json,
			Strings.toString(
				_elasticsearchDocumentFactory.getElasticsearchDocument(
					documentBuilder.build())));
	}

	protected void assertDocumentLegacy(String value, String json)
		throws Exception {

		Document document = new DocumentImpl();

		document.addText(_FIELD, new String[] {value});

		Assert.assertEquals(
			json,
			_elasticsearchDocumentFactory.getElasticsearchDocument(document));
	}

	protected void assertDocumentSameAsLegacy(String value, String json)
		throws Exception {

		assertDocument(value, json);
		assertDocumentLegacy(value, json);
	}

	private static final String _FIELD = "field";

	private final DocumentFixture _documentFixture = new DocumentFixture();
	private ElasticsearchDocumentFactory _elasticsearchDocumentFactory;

}