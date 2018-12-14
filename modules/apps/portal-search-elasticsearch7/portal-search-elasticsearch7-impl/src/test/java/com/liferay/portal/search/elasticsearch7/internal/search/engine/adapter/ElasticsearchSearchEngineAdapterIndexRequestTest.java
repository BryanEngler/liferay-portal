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

package com.liferay.portal.search.elasticsearch7.internal.search.engine.adapter;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch7.internal.search.engine.adapter.index.IndexRequestExecutorFixture;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.index.CloseIndexRequest;
import com.liferay.portal.search.engine.adapter.index.CloseIndexResponse;
import com.liferay.portal.search.engine.adapter.index.CreateIndexRequest;
import com.liferay.portal.search.engine.adapter.index.CreateIndexResponse;
import com.liferay.portal.search.engine.adapter.index.DeleteIndexRequest;
import com.liferay.portal.search.engine.adapter.index.DeleteIndexResponse;
import com.liferay.portal.search.engine.adapter.index.FlushIndexRequest;
import com.liferay.portal.search.engine.adapter.index.FlushIndexResponse;
import com.liferay.portal.search.engine.adapter.index.GetFieldMappingIndexRequest;
import com.liferay.portal.search.engine.adapter.index.GetFieldMappingIndexResponse;
import com.liferay.portal.search.engine.adapter.index.GetIndexIndexRequest;
import com.liferay.portal.search.engine.adapter.index.GetIndexIndexResponse;
import com.liferay.portal.search.engine.adapter.index.GetMappingIndexRequest;
import com.liferay.portal.search.engine.adapter.index.GetMappingIndexResponse;
import com.liferay.portal.search.engine.adapter.index.IndexRequestExecutor;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexResponse;
import com.liferay.portal.search.engine.adapter.index.IndicesOptions;
import com.liferay.portal.search.engine.adapter.index.OpenIndexRequest;
import com.liferay.portal.search.engine.adapter.index.OpenIndexResponse;
import com.liferay.portal.search.engine.adapter.index.PutMappingIndexRequest;
import com.liferay.portal.search.engine.adapter.index.PutMappingIndexResponse;
import com.liferay.portal.search.engine.adapter.index.RefreshIndexRequest;
import com.liferay.portal.search.engine.adapter.index.RefreshIndexResponse;
import com.liferay.portal.search.engine.adapter.index.UpdateIndexSettingsIndexRequest;
import com.liferay.portal.search.engine.adapter.index.UpdateIndexSettingsIndexResponse;

import java.io.IOException;

import java.util.Arrays;
import java.util.Map;

import org.apache.http.util.EntityUtils;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Dylan Rebelak
 */
public class ElasticsearchSearchEngineAdapterIndexRequestTest {

	@Before
	public void setUp() throws Exception {
		setUpJSONFactoryUtil();

		_elasticsearchFixture = new ElasticsearchFixture(
			ElasticsearchSearchEngineAdapterIndexRequestTest.class.
				getSimpleName());

		_elasticsearchFixture.setUp();

		AdminClient adminClient = _elasticsearchFixture.getAdminClient();

		_indicesAdminClient = adminClient.indices();

		RestHighLevelClient restHighLevelClient =
			_elasticsearchFixture.getRestHighLevelClient();

		_indicesClient = restHighLevelClient.indices();

		_searchEngineAdapter = createSearchEngineAdapter(_elasticsearchFixture);

		createIndex(_INDEX_NAME);
	}

	@After
	public void tearDown() throws Exception {
		deleteIndex(_INDEX_NAME);

		_elasticsearchFixture.tearDown();
	}

	@Test
	public void testExecuteCloseIndexRequest() {
		CloseIndexRequest closeIndexRequest = new CloseIndexRequest(
			_INDEX_NAME);

		IndicesOptions indicesOptions = new IndicesOptions();

		indicesOptions.setIgnoreUnavailable(true);

		closeIndexRequest.setIndicesOptions(indicesOptions);

		CloseIndexResponse closeIndexResponse = _searchEngineAdapter.execute(
			closeIndexRequest);

		Assert.assertTrue(
			"Close request not acknowledged",
			closeIndexResponse.isAcknowledged());

		assertIndexMetaDataState(_INDEX_NAME, IndexMetaData.State.CLOSE);
	}

	@Test
	public void testExecuteCreateIndexRequest() {
		CreateIndexRequest createIndexRequest = new CreateIndexRequest(
			"test_index_2");

		StringBundler sb = new StringBundler(14);

		sb.append("{\n");
		sb.append("    \"settings\": {\n");
		sb.append("        \"number_of_shards\": 1\n");
		sb.append("    },\n");
		sb.append("    \"mappings\": {\n");
		sb.append("        \"type1\": {\n");
		sb.append("            \"properties\": {\n");
		sb.append("                \"field1\": {\n");
		sb.append("                    \"type\": \"text\"\n");
		sb.append("                }\n");
		sb.append("            }\n");
		sb.append("        }\n");
		sb.append("    }\n");
		sb.append("}");

		createIndexRequest.setSource(sb.toString());

		CreateIndexResponse createIndexResponse = _searchEngineAdapter.execute(
			createIndexRequest);

		Assert.assertTrue(createIndexResponse.isAcknowledged());

		Assert.assertTrue(indiciesExists("test_index_2"));

		deleteIndex("test_index_2");
	}

	@Test
	public void testExecuteDeleteIndexRequest() {
		createIndex("test_index_2");

		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(
			"test_index_2");

		DeleteIndexResponse deleteIndexResponse = _searchEngineAdapter.execute(
			deleteIndexRequest);

		Assert.assertTrue(deleteIndexResponse.isAcknowledged());

		Assert.assertFalse(indiciesExists("test_index_2"));
	}

	@Test
	public void testExecuteFlushIndexRequest() {
		FlushIndexRequest flushIndexRequest = new FlushIndexRequest(
			_INDEX_NAME);

		FlushIndexResponse flushIndexResponse = _searchEngineAdapter.execute(
			flushIndexRequest);

		Assert.assertEquals(0, flushIndexResponse.getFailedShards());
	}

	@Ignore
	@Test
	public void testExecuteGetFieldMappingIndexRequest() {
		String mappingName = "testGetFieldMapping";

		String mappingSource =
			"{\"properties\":{\"testField\":{\"type\":\"keyword\"}, " +
				"\"otherTestField\":{\"type\":\"keyword\"}}}";

		_putMapping(mappingSource);

		String[] fields = {"otherTestField"};

		GetFieldMappingIndexRequest getFieldMappingIndexRequest =
			new GetFieldMappingIndexRequest(
				new String[] {_INDEX_NAME}, mappingName, fields);

		GetFieldMappingIndexResponse getFieldMappingIndexResponse =
			_searchEngineAdapter.execute(getFieldMappingIndexRequest);

		String fieldMappings = String.valueOf(
			getFieldMappingIndexResponse.getFieldMappings());

		Assert.assertTrue(fieldMappings.contains(mappingSource));
	}

	@Test
	public void testExecuteGetIndexIndexRequest() {
		GetIndexIndexRequest getIndexIndexRequest = new GetIndexIndexRequest(
			_INDEX_NAME);

		GetIndexIndexResponse getIndexIndexResponse =
			_searchEngineAdapter.execute(getIndexIndexRequest);

		String[] indexNames = getIndexIndexResponse.getIndexNames();

		Assert.assertEquals(Arrays.toString(indexNames), 1, indexNames.length);
		Assert.assertEquals(_INDEX_NAME, indexNames[0]);
	}

	@Test
	public void testExecuteGetMappingIndexRequest() throws JSONException {
		String mappingName = "testGetMapping";

		String mappingSource =
			"{\"properties\":{\"testField\":{\"type\":\"keyword\"}}}";

		_putMapping(mappingSource);

		GetMappingIndexRequest getMappingIndexRequest =
			new GetMappingIndexRequest(new String[] {_INDEX_NAME}, mappingName);

		GetMappingIndexResponse getMappingIndexResponse =
			_searchEngineAdapter.execute(getMappingIndexRequest);

		Map<String, String> indexMappings =
			getMappingIndexResponse.getIndexMappings();

		String string = indexMappings.toString();

		Assert.assertTrue(string.contains(mappingSource));
	}

	@Test
	public void testExecuteIndicesExistsIndexRequest() {
		IndicesExistsIndexRequest indicesExistsIndexRequest =
			new IndicesExistsIndexRequest(_INDEX_NAME);

		IndicesExistsIndexResponse indicesExistsIndexResponse =
			_searchEngineAdapter.execute(indicesExistsIndexRequest);

		Assert.assertTrue(indicesExistsIndexResponse.isExists());

		IndicesExistsIndexRequest indicesExistsIndexRequest2 =
			new IndicesExistsIndexRequest("test_index_2");

		IndicesExistsIndexResponse indicesExistsIndexResponse2 =
			_searchEngineAdapter.execute(indicesExistsIndexRequest2);

		Assert.assertFalse(indicesExistsIndexResponse2.isExists());
	}

	@Test
	public void testExecuteOpenIndexRequest() {
		org.elasticsearch.action.admin.indices.close.CloseIndexRequest
			elasticsearchCloseIndexRequest =
				new org.elasticsearch.action.admin.indices.close.
					CloseIndexRequest(_INDEX_NAME);

		try {
			_indicesClient.close(
				elasticsearchCloseIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

		assertIndexMetaDataState(_INDEX_NAME, IndexMetaData.State.CLOSE);

		OpenIndexRequest openIndexRequest = new OpenIndexRequest(_INDEX_NAME);

		IndicesOptions indicesOptions = new IndicesOptions();

		indicesOptions.setIgnoreUnavailable(true);

		openIndexRequest.setIndicesOptions(indicesOptions);

		OpenIndexResponse openIndexResponse = _searchEngineAdapter.execute(
			openIndexRequest);

		Assert.assertTrue(
			"Open request not acknowledged",
			openIndexResponse.isAcknowledged());

		assertIndexMetaDataState(_INDEX_NAME, IndexMetaData.State.OPEN);
	}

	@Test
	public void testExecutePutMappingIndexRequest() {
		String mappingName = "testPutMapping";
		String mappingSource =
			"{\"properties\":{\"testField\":{\"type\":\"keyword\"}}}";

		PutMappingIndexRequest putMappingIndexRequest =
			new PutMappingIndexRequest(
				new String[] {_INDEX_NAME}, mappingName, mappingSource);

		PutMappingIndexResponse putMappingIndexResponse =
			_searchEngineAdapter.execute(putMappingIndexRequest);

		Assert.assertTrue(putMappingIndexResponse.isAcknowledged());

		GetMappingsRequestBuilder getMappingsRequestBuilder =
			_indicesAdminClient.prepareGetMappings(_INDEX_NAME);

		getMappingsRequestBuilder.setTypes(mappingName);

		GetMappingsResponse getMappingsResponse =
			getMappingsRequestBuilder.get();

		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>>
			immutableOpenMap1 = getMappingsResponse.getMappings();

		ImmutableOpenMap<String, MappingMetaData> immutableOpenMap2 =
			immutableOpenMap1.get(_INDEX_NAME);

		MappingMetaData mappingMetaData = immutableOpenMap2.get(mappingName);

		String mappingMetaDataSource = String.valueOf(mappingMetaData.source());

		Assert.assertTrue(mappingMetaDataSource.contains(mappingSource));
	}

	@Test
	public void testExecuteRefreshIndexRequest() {
		RefreshIndexRequest refreshIndexRequest = new RefreshIndexRequest(
			_INDEX_NAME);

		RefreshIndexResponse refreshIndexResponse =
			_searchEngineAdapter.execute(refreshIndexRequest);

		Assert.assertEquals(0, refreshIndexResponse.getFailedShards());
	}

	@Ignore
	@Test
	public void testExecuteUpdateIndexSettingsIndexRequest() {
		createIndex("test_index_2");

		UpdateIndexSettingsIndexRequest updateIndexSettingsIndexRequest =
			new UpdateIndexSettingsIndexRequest("test_index_2");

		StringBundler sb = new StringBundler(5);

		sb.append("{\n");
		sb.append("    \"index\": {\n");
		sb.append("        \"refresh_interval\": \"2s\"\n");
		sb.append("    }\n");
		sb.append("}");

		updateIndexSettingsIndexRequest.setSettings(sb.toString());

		UpdateIndexSettingsIndexResponse indexSettingsIndexResponse =
			_searchEngineAdapter.execute(updateIndexSettingsIndexRequest);

		Assert.assertTrue(indexSettingsIndexResponse.isAcknowledged());

		GetSettingsRequest getSettingsRequest = new GetSettingsRequest();

		getSettingsRequest.indices("test_index_2");

		try {
			GetSettingsResponse getSettingsResponse =
				_indicesClient.getSettings(
					getSettingsRequest, RequestOptions.DEFAULT);

			String refreshInterval = getSettingsResponse.getSetting(
				"test_index_2", "refresh_interval");

			Assert.assertEquals("2s", refreshInterval);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

		deleteIndex("test_index_2");
	}

	protected static IndexRequestExecutor createIndexRequestExecutor(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		IndexRequestExecutorFixture indexRequestExecutorFixture =
			new IndexRequestExecutorFixture() {
				{
					setElasticsearchClientResolver(elasticsearchClientResolver);
				}
			};

		indexRequestExecutorFixture.setUp();

		return indexRequestExecutorFixture.getIndexRequestExecutor();
	}

	protected static SearchEngineAdapter createSearchEngineAdapter(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		return new ElasticsearchSearchEngineAdapterImpl() {
			{
				setIndexRequestExecutor(
					createIndexRequestExecutor(elasticsearchClientResolver));
			}
		};
	}

	protected void assertIndexMetaDataState(
		String indexName, IndexMetaData.State indexMetaDataState) {

		RestHighLevelClient restHighLevelClient =
			_elasticsearchFixture.getRestHighLevelClient();

		RestClient restLowLevelClient = restHighLevelClient.getLowLevelClient();

		Request request = new Request(
			"GET", "/_cluster/state/metadata/" + indexName);

		try {
			Response response = restLowLevelClient.performRequest(request);

			String responseBody = EntityUtils.toString(response.getEntity());

			JSONObject responseJSONObject = JSONFactoryUtil.createJSONObject(
				responseBody);

			JSONObject metadataJSONObject = responseJSONObject.getJSONObject(
				"metadata");

			JSONObject indicesJSONObject = metadataJSONObject.getJSONObject(
				"indices");

			JSONObject indexJSONObject = indicesJSONObject.getJSONObject(
				indexName);

			String state = GetterUtil.getString(indexJSONObject.get("state"));

			Assert.assertEquals(translateState(indexMetaDataState), state);
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
	}

	protected void createIndex(String indexName) {
		org.elasticsearch.action.admin.indices.create.CreateIndexRequest
			elasticsearchCreateIndexRequest =
				new org.elasticsearch.action.admin.indices.create.
					CreateIndexRequest(indexName);

		try {
			_indicesClient.create(
				elasticsearchCreateIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	protected AcknowledgedResponse deleteIndex(String indexName) {
		org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
			elasticsearchDeleteIndexRequest =
				new org.elasticsearch.action.admin.indices.delete.
					DeleteIndexRequest(indexName);

		try {
			return _indicesClient.delete(
				elasticsearchDeleteIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	protected boolean indiciesExists(String indexName) {
		GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);

		try {
			return _indicesClient.exists(
				getIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	protected void setUpJSONFactoryUtil() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	protected String translateState(IndexMetaData.State state) {
		if (state == IndexMetaData.State.OPEN) {
			return "open";
		}

		if (state == IndexMetaData.State.CLOSE) {
			return "close";
		}

		throw new IllegalArgumentException("Unknown state: " + state);
	}

	private void _putMapping(String mappingSource) {
		PutMappingRequest putMappingRequest = new PutMappingRequest(
			_INDEX_NAME);

		putMappingRequest.source(mappingSource, XContentType.JSON);
		//putMappingRequest.type(mappingName);

		try {
			_indicesClient.putMapping(
				putMappingRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private static final String _INDEX_NAME = "test_request_index";

	private ElasticsearchFixture _elasticsearchFixture;
	private IndicesAdminClient _indicesAdminClient;
	private IndicesClient _indicesClient;
	private SearchEngineAdapter _searchEngineAdapter;

}