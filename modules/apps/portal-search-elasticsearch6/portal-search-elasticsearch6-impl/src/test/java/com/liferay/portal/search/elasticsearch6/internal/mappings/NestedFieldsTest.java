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

package com.liferay.portal.search.elasticsearch6.internal.mappings;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.elasticsearch6.internal.ElasticsearchIndexingFixture;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch6.internal.index.LiferayTypeMappingsConstants;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.index.GetFieldMappingIndexRequest;
import com.liferay.portal.search.engine.adapter.index.GetFieldMappingIndexResponse;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;
import com.liferay.portal.search.test.util.mappings.BaseNestedFieldsTestCase;

import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Bryan Engler
 */
public class NestedFieldsTest extends BaseNestedFieldsTestCase {

	@Test
	public void testDDMNestedFieldsDynamicMapping() throws Exception {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());

		SearchEngineAdapter searchEngineAdapter = getSearchEngineAdapter();

		GetFieldMappingIndexRequest getFieldMappingIndexRequest =
			new GetFieldMappingIndexRequest(
				new String[] {String.valueOf(getCompanyId())},
				LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE,
				new String[] {
					"ddmFields.fieldValueText_en_US",
					"ddmFields.fieldValueKeyword"
				});

		GetFieldMappingIndexResponse getFieldMappingIndexResponse =
			searchEngineAdapter.execute(getFieldMappingIndexRequest);

		Map<String, String> fieldMappings =
			getFieldMappingIndexResponse.getFieldMappings();

		String mapping = fieldMappings.get(String.valueOf(getCompanyId()));

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(mapping);

		Set<String> keySet = jsonObject.keySet();

		Assert.assertEquals(mapping, 2, keySet.size());

		JSONObject jsonObject2 = jsonObject.getJSONObject(
			"ddmFields.fieldValueText_en_US");

		JSONObject jsonObject3 = jsonObject2.getJSONObject(
			"fieldValueText_en_US");

		String analyzer = jsonObject3.getString("analyzer");

		Assert.assertEquals(mapping, "english", analyzer);
	}

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		return new ElasticsearchIndexingFixture() {
			{
				setElasticsearchFixture(new ElasticsearchFixture(getClass()));
				setLiferayMappingsAddedToIndex(true);
			}
		};
	}

}