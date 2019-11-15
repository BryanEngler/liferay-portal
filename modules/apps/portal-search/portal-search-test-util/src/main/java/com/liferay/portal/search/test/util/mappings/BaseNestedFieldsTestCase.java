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

package com.liferay.portal.search.test.util.mappings;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.FieldArray;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.TermQuery;
import com.liferay.portal.kernel.search.filter.TermFilter;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.MatchQuery;
import com.liferay.portal.kernel.search.generic.NestedQuery;
import com.liferay.portal.kernel.search.generic.TermQueryImpl;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bryan Engler
 */
public abstract class BaseNestedFieldsTestCase extends BaseIndexingTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		addDDMDocument();
	}

	@Test
	public void testDDMFieldsDontMatchUnrelatedKeywordValue() throws Exception {
		assertSearch(
			"ddm__text__41523__TextBox7nkv_en_US",
			"ddmFields.fieldValueKeyword", "true", false, 0);
	}

	@Test
	public void testDDMFieldsDontMatchUnrelatedTextValue() throws Exception {
		assertSearch(
			"ddm__text__41523__TextBox7nkv_en_US", "ddmFields.fieldValueText",
			"bravo", false, 0);
	}

	@Test
	public void testDDMNestedFieldsDontMatchPartialKeyword() throws Exception {
		assertSearch(
			"ddm__keyword__41523__Textggef_en_US",
			"ddmFields.fieldValueKeyword", "alpha", true, 0);
	}

	@Test
	public void testDDMNestedFieldsDontMatchUnrelatedKeywordValue()
		throws Exception {

		assertSearch(
			"ddm__text__41523__TextBox7nkv_en_US",
			"ddmFields.fieldValueKeyword", "true", true, 0);
	}

	@Test
	public void testDDMNestedFieldsDontMatchUnrelatedTextValue()
		throws Exception {

		assertSearch(
			"ddm__text__41523__TextBox7nkv_en_US", "ddmFields.fieldValueText",
			"bravo", true, 0);
	}

	@Test
	public void testDDMNestedFieldsMatchKeyword() throws Exception {
		assertSearch(
			"ddm__keyword__41523__Textggef_en_US",
			"ddmFields.fieldValueKeyword", "alpha keyword", true,
			"alpha keyword");
	}

	@Test
	public void testDDMNestedFieldsMatchLocalizedText() throws Exception {
		assertSearch(
			"ddm__text__41523__TextBoxnj7s_en_US",
			"ddmFields.fieldValueText_en_US", "charlie", true, "charlie text");
	}

	@Test
	public void testDDMNestedFieldsMatchMultipleValues() throws Exception {
		assertSearch(
			"ddm__text__41523__TextBoxo9us_ja_JP",
			"ddmFields.fieldValueText_ja_JP", "作戦大成功", true,
			Arrays.asList("作戦大成功", "新規作戦"));
	}

	@Test
	public void testDDMNestedFieldsMatchText() throws Exception {
		assertSearch(
			"ddm__text__41523__TextBox7nkv_en_US", "ddmFields.fieldValueText",
			"alpha", true, "alpha text");
	}

	protected void addDDMDocument() {
		FieldArray fieldArray = new FieldArray("ddmFields");

		Field nestedObject1 = new Field("");

		nestedObject1.addField(
			new Field(
				"fieldName",
				"ddm__keyword__41523__Booleantua8_en_US_String_sortable"));
		nestedObject1.addField(new Field("fieldValueKeyword", "true"));
		nestedObject1.addField(
			new Field("valueFieldName", "fieldValueKeyword"));

		fieldArray.addField(nestedObject1);

		Field nestedObject2 = new Field("");

		nestedObject2.addField(
			new Field("fieldName", "ddm__keyword__41523__Textggef_en_US"));
		nestedObject2.addField(new Field("fieldValueKeyword", "alpha keyword"));
		nestedObject2.addField(
			new Field("valueFieldName", "fieldValueKeyword"));

		fieldArray.addField(nestedObject2);

		Field nestedObject3 = new Field("");

		nestedObject3.addField(
			new Field("fieldName", "ddm__keyword__41523__Textp47b_en_US"));
		nestedObject3.addField(new Field("fieldValueKeyword", "bravo keyword"));
		nestedObject3.addField(
			new Field("valueFieldName", "fieldValueKeyword"));

		fieldArray.addField(nestedObject3);

		Field nestedObject4 = new Field("");

		nestedObject4.addField(
			new Field("fieldName", "ddm__text__41523__TextBox7nkv_en_US"));
		nestedObject4.addField(new Field("fieldValueText", "alpha text"));
		nestedObject4.addField(new Field("valueFieldName", "fieldValueText"));

		fieldArray.addField(nestedObject4);

		Field nestedObject5 = new Field("");

		nestedObject5.addField(
			new Field("fieldName", "ddm__text__41523__TextBox6yh3_en_US"));
		nestedObject5.addField(new Field("fieldValueText", "bravo text"));
		nestedObject5.addField(new Field("valueFieldName", "fieldValueText"));

		fieldArray.addField(nestedObject5);

		Field nestedObject6 = new Field("");

		nestedObject6.addField(
			new Field("fieldName", "ddm__text__41523__TextBoxnj7s_en_US"));
		nestedObject6.addField(
			new Field("fieldValueText_en_US", "charlie text"));
		nestedObject6.addField(
			new Field("valueFieldName", "fieldValueText_en_US"));

		fieldArray.addField(nestedObject6);

		Field nestedObject7 = new Field("");

		nestedObject7.addField(
			new Field("fieldName", "ddm__text__41523__TextBoxo9us_ja_JP"));
		nestedObject7.addField(
			new Field("fieldValueText_ja_JP", new String[] {"作戦大成功", "新規作戦"}));
		nestedObject7.addField(
			new Field("valueFieldName", "fieldValueText_ja_JP"));

		fieldArray.addField(nestedObject7);

		addDocument(DocumentCreationHelpers.field(fieldArray));
	}

	protected void assertSearch(
			String fieldName, String valueFieldName, String value,
			boolean mappedAsNested, int expectedCount)
		throws Exception {

		BooleanQuery booleanQuery = _buildQuery(
			fieldName, valueFieldName, value, mappedAsNested);

		assertSearch(
			indexingTestHelper -> {
				_search(indexingTestHelper, booleanQuery, mappedAsNested);

				indexingTestHelper.verify(
					hits -> DocumentsAssert.assertCount(
						indexingTestHelper.getRequestString(), hits.getDocs(),
						fieldName, expectedCount));
			});
	}

	protected void assertSearch(
			String fieldName, String valueFieldName, String value,
			boolean mappedAsNested, Object expectedValue)
		throws Exception {

		BooleanQuery booleanQuery = _buildQuery(
			fieldName, valueFieldName, value, mappedAsNested);

		assertSearch(
			indexingTestHelper -> {
				_search(indexingTestHelper, booleanQuery, mappedAsNested);

				indexingTestHelper.verifyResponse(
					searchResponse -> {
						SearchHits searchHits = searchResponse.getSearchHits();

						List<SearchHit> searchHitsList =
							searchHits.getSearchHits();

						SearchHit searchHit = searchHitsList.get(0);

						Document document = searchHit.getDocument();

						List<Object> ddmFieldsValues = document.getValues(
							"ddmFields");

						Object actualValue = null;

						for (Object ddmFieldsValue : ddmFieldsValues) {
							Map<String, Object> nestedObject =
								(Map<String, Object>)ddmFieldsValue;

							String nestedObjectFieldName = GetterUtil.getString(
								nestedObject.get("fieldName"));

							if (nestedObjectFieldName.equals(fieldName)) {
								String nestedObjectValueField =
									GetterUtil.getString(
										nestedObject.get("valueFieldName"));

								actualValue = nestedObject.get(
									nestedObjectValueField);

								break;
							}
						}

						Assert.assertEquals(expectedValue, actualValue);
					});
			});
	}

	private BooleanQuery _buildQuery(
			String fieldName, String valueFieldName, String value,
			boolean mappedAsNested)
		throws Exception {

		BooleanQuery booleanQuery = new BooleanQueryImpl();

		if (mappedAsNested) {
			TermQuery fieldNameTermQuery = new TermQueryImpl(
				"ddmFields.fieldName", fieldName);

			booleanQuery.add(fieldNameTermQuery, BooleanClauseOccur.MUST);
		}
		else {
			MatchQuery fieldNameMatchQuery = new MatchQuery(
				"ddmFields.fieldName", fieldName);

			booleanQuery.add(fieldNameMatchQuery, BooleanClauseOccur.MUST);
		}

		if (fieldName.startsWith("ddm__keyword")) {
			TermQuery fieldValueTermQuery = new TermQueryImpl(
				valueFieldName, value);

			booleanQuery.add(fieldValueTermQuery, BooleanClauseOccur.MUST);
		}
		else {
			MatchQuery fieldValueMatchQuery = new MatchQuery(
				valueFieldName, value);

			booleanQuery.add(fieldValueMatchQuery, BooleanClauseOccur.MUST);
		}

		return booleanQuery;
	}

	private void _search(
		IndexingTestHelper indexingTestHelper, BooleanQuery booleanQuery,
		boolean mappedAsNested) {

		indexingTestHelper.setFilter(
			new TermFilter(Field.ENTRY_CLASS_NAME, getEntryClassName()));

		Query query = booleanQuery;

		if (mappedAsNested) {
			query = new NestedQuery("ddmFields", booleanQuery);
		}

		indexingTestHelper.defineRequest(
			searchRequestBuilder -> searchRequestBuilder.fetchSourceIncludes(
				new String[] {"ddmFields.*"}));

		indexingTestHelper.setQuery(query);

		indexingTestHelper.search();
	}

}