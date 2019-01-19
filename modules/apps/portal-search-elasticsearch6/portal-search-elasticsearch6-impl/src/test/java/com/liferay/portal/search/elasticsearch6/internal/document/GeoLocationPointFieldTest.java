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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.elasticsearch6.internal.ElasticsearchIndexingFixture;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch6.internal.connection.IndexCreationHelper;
import com.liferay.portal.search.elasticsearch6.internal.index.LiferayTypeMappingsConstants;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingAction;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author André de Oliveira
 */
public class GeoLocationPointFieldTest extends BaseIndexingTestCase {

	@Test
	public void testCustomField() throws Exception {
		assertGeoLocationPointField(_CUSTOM_FIELD);
	}

	@Test
	public void testDefaultField() throws Exception {
		assertGeoLocationPointField(Field.GEO_LOCATION);
	}

	@Test
	public void testDefaultTemplate() throws Exception {
		assertGeoLocationPointField(_CUSTOM_FIELD.concat("_geolocation"));
	}

	protected void assertGeoLocationPointField(final String fieldName)
		throws Exception {

		final double latitude = randomLatitude();
		final double longitude = randomLongitude();

		addDocument(
			DocumentCreationHelpers.singleGeoLocation(
				fieldName, latitude, longitude));

		Document document = IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			new Callable<Document>() {

				@Override
				public Document call() throws Exception {
					return searchOneDocument();
				}

			});

		Field field = document.getField(fieldName);

		GeoLocationPoint geoLocationPoint = field.getGeoLocationPoint();

		Assert.assertEquals(latitude, geoLocationPoint.getLatitude(), 0);
		Assert.assertEquals(longitude, geoLocationPoint.getLongitude(), 0);
	}

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		return new ElasticsearchIndexingFixture() {
			{
				ElasticsearchFixture elasticsearchFixture =
					new ElasticsearchFixture(getClass());

				setCompanyId(BaseIndexingTestCase.COMPANY_ID);
				setElasticsearchFixture(elasticsearchFixture);
				setIndexCreationHelper(
					new CustomFieldLiferayIndexCreationHelper(
						elasticsearchFixture));
				setLiferayMappingsAddedToIndex(true);
			}
		};
	}

	protected int randomLatitude() {
		return RandomTestUtil.randomInt(90, 180) - 90;
	}

	protected int randomLongitude() {
		return RandomTestUtil.randomInt(180, 360) - 180;
	}

	protected Document searchOneDocument() throws Exception {
		Hits hits = search(createSearchContext());

		Document[] documents = hits.getDocs();

		Assert.assertEquals(Arrays.toString(documents), 1, documents.length);

		return documents[0];
	}

	private static final String _CUSTOM_FIELD = "customField";

	private static class CustomFieldLiferayIndexCreationHelper
		implements IndexCreationHelper {

		public CustomFieldLiferayIndexCreationHelper(
			ElasticsearchClientResolver elasticsearchClientResolver) {

			_elasticsearchClientResolver = elasticsearchClientResolver;
		}

		@Override
		public void contribute(
			CreateIndexRequestBuilder createIndexRequestBuilder) {
		}

		@Override
		public void contributeIndexSettings(Settings.Builder builder) {
		}

		@Override
		public void whenIndexCreated(String indexName) {
			PutMappingRequestBuilder putMappingRequestBuilder =
				PutMappingAction.INSTANCE.newRequestBuilder(
					_elasticsearchClientResolver.getClient());

			String source = StringBundler.concat(
				"{ \"properties\": { \"", _CUSTOM_FIELD, "\" : { \"fields\": ",
				"{ \"geopoint\" : { \"store\": true, \"type\": \"keyword\" } ",
				"}, \"store\": true, \"type\": \"geo_point\" } } }");

			putMappingRequestBuilder.setIndices(
				indexName
			).setSource(
				source, XContentType.JSON
			).setType(
				LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE
			);

			putMappingRequestBuilder.get();
		}

		private final ElasticsearchClientResolver _elasticsearchClientResolver;

	}

}