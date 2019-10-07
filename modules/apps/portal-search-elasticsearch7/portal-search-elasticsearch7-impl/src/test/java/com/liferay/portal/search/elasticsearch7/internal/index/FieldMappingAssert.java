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

package com.liferay.portal.search.elasticsearch7.internal.index;

import com.liferay.portal.search.test.util.IdempotentRetryAssert;

import java.io.IOException;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.GetFieldMappingsRequest;
import org.elasticsearch.client.indices.GetFieldMappingsResponse;

import org.junit.Assert;

/**
 * @author Artur Aquino
 * @author André de Oliveira
 */
public class FieldMappingAssert {

	public static void assertAnalyzer(
			String expectedValue, String field, String index,
			IndicesClient indicesClient)
		throws Exception {

		assertFieldMappingMetaData(
			expectedValue, "analyzer", field, index, indicesClient);
	}

	public static void assertFieldMappingMetaData(
			final String expectedValue, final String key, final String field,
			final String index, final IndicesClient indicesClient)
		throws Exception {

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					doAssertFieldMappingMetaData(
						expectedValue, key, field, index, indicesClient);

					return null;
				}

			});
	}

	public static void assertType(
			String expectedValue, String field, String index,
			IndicesClient indicesClient)
		throws Exception {

		assertFieldMappingMetaData(
			expectedValue, "type", field, index, indicesClient);
	}

	protected static void doAssertFieldMappingMetaData(
		String expectedValue, String key, String field, String index,
		IndicesClient indicesClient) {

		GetFieldMappingsResponse.FieldMappingMetaData fieldMappingMetaData =
			getFieldMapping(field, index, indicesClient);

		String value = getFieldMappingMetaDataValue(
			fieldMappingMetaData, field, key);

		Assert.assertEquals(expectedValue, value);
	}

	protected static GetFieldMappingsResponse.FieldMappingMetaData
		getFieldMapping(
			String field, String index, IndicesClient indicesClient) {

		GetFieldMappingsRequest getFieldMappingsRequest =
			new GetFieldMappingsRequest();

		getFieldMappingsRequest.fields(field);
		getFieldMappingsRequest.indices(index);

		try {
			GetFieldMappingsResponse getFieldMappingsResponse =
				indicesClient.getFieldMapping(
					getFieldMappingsRequest, RequestOptions.DEFAULT);

			return getFieldMappingsResponse.fieldMappings(index, field);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@SuppressWarnings("unchecked")
	protected static String getFieldMappingMetaDataValue(
		GetFieldMappingsResponse.FieldMappingMetaData fieldMappingMetaData,
		String field, String key) {

		Map<String, Object> mappings = fieldMappingMetaData.sourceAsMap();

		Map<String, Object> mapping = (Map<String, Object>)mappings.get(field);

		return (String)mapping.get(key);
	}

}