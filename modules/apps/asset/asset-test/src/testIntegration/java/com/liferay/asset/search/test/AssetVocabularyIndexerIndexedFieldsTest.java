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

package com.liferay.asset.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.test.util.FieldValuesAssert;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerTestRule;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luan Maoski
 * @author Lucas Marques
 */
@RunWith(Arquillian.class)
public class AssetVocabularyIndexerIndexedFieldsTest
	extends BaseAssetVocabularyTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		super.setUp();

		setGroup(assetVocabularyFixture.addGroup());
	}

	@Test
	public void testIndexedFields() throws Exception {
		AssetVocabulary assetVocabulary =
			assetVocabularyFixture.createAssetVocabulary();

		String searchTerm = assetVocabulary.getName();

		Document document = assetVocabularyIndexerFixture.searchOnlyOne(
			searchTerm);

		indexedFieldsFixture.postProcessDocument(document);

		Map<String, String> expected = _expectedFieldValues(assetVocabulary);

		FieldValuesAssert.assertFieldValues(expected, document, searchTerm);
	}

	private Map<String, String> _expectedFieldValues(
			AssetVocabulary assetVocabulary)
		throws Exception {

		Map<String, String> map = new HashMap<>();

		map.put(
			Field.ENTRY_CLASS_PK,
			String.valueOf(assetVocabulary.getVocabularyId()));

		map.put(Field.ENTRY_CLASS_NAME, AssetVocabulary.class.getName());
		map.put(
			Field.COMPANY_ID, String.valueOf(assetVocabulary.getCompanyId()));

		map.put(Field.GROUP_ID, String.valueOf(assetVocabulary.getGroupId()));

		map.put(
			Field.ASSET_VOCABULARY_ID,
			String.valueOf(assetVocabulary.getVocabularyId()));

		map.put(
			Field.ASSET_VOCABULARY_ID,
			String.valueOf(assetVocabulary.getVocabularyId()));

		map.put(
			Field.SCOPE_GROUP_ID, String.valueOf(assetVocabulary.getGroupId()));

		map.put(
			Field.STAGING_GROUP,
			String.valueOf(assetVocabularyFixture.getGroup().isStagingGroup()));

		map.put(Field.TITLE, assetVocabulary.getName());

		map.put("title_en_US", assetVocabulary.getName());

		map.put(
			"title_sortable", StringUtil.lowerCase(assetVocabulary.getName()));

		map.put(Field.NAME, assetVocabulary.getName());

		map.put(
			"name_sortable", StringUtil.lowerCase(assetVocabulary.getName()));

		map.put(Field.USER_ID, String.valueOf(assetVocabulary.getUserId()));

		map.put(
			Field.USER_NAME,
			StringUtil.lowerCase(assetVocabulary.getUserName()));

		indexedFieldsFixture.populateUID(
			AssetVocabulary.class.getName(), assetVocabulary.getVocabularyId(),
			map);

		_populateDates(assetVocabulary, map);
		_populateRoles(assetVocabulary, map);

		return map;
	}

	private void _populateDates(
		AssetVocabulary assetVocabulary, Map<String, String> map) {

		indexedFieldsFixture.populateDate(
			Field.MODIFIED_DATE, assetVocabulary.getModifiedDate(), map);
		indexedFieldsFixture.populateDate(
			Field.CREATE_DATE, assetVocabulary.getCreateDate(), map);
	}

	private void _populateRoles(
			AssetVocabulary assetVocabulary, Map<String, String> map)
		throws Exception {

		indexedFieldsFixture.populateRoleIdFields(
			assetVocabulary.getCompanyId(), AssetVocabulary.class.getName(),
			assetVocabulary.getVocabularyId(), assetVocabulary.getGroupId(),
			null, map);
	}

}