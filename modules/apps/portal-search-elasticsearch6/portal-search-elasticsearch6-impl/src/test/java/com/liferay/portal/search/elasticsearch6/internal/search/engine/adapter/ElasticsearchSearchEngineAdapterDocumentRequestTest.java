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

package com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter;

import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch6.internal.connection.TestElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter.document.DocumentRequestExecutorFixture;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentItemResponse;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.DeleteByQueryDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.DeleteByQueryDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.DocumentRequestExecutor;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.UpdateByQueryDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateByQueryDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentResponse;
import com.liferay.portal.search.test.util.indexing.DocumentFixture;

import java.io.IOException;

import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Dylan Rebelak
 */
public class ElasticsearchSearchEngineAdapterDocumentRequestTest {

	@Before
	public void setUp() throws Exception {
		_elasticsearchFixture = new ElasticsearchFixture(
			ElasticsearchSearchEngineAdapterDocumentRequestTest.class.
				getSimpleName());

		_elasticsearchFixture.setUp();

		_elasticsearchConnectionManager =
			new TestElasticsearchConnectionManager(_elasticsearchFixture);

		_restHighLevelClient =
			_elasticsearchConnectionManager.getRestHighLevelClient();

		_searchEngineAdapter = createSearchEngineAdapter(
			_elasticsearchConnectionManager);

		_documentFixture.setUp();

		createIndex();
	}

	@After
	public void tearDown() throws Exception {
		deleteIndex();

		_documentFixture.tearDown();

		_elasticsearchFixture.tearDown();
	}

	@Test
	public void testExecuteBulkDocumentRequest() {
		Document document1 = new DocumentImpl();

		document1.addKeyword(Field.UID, "1");
		document1.addKeyword(_FIELD_NAME, Boolean.TRUE.toString());

		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			_INDEX_NAME, document1);

		indexDocumentRequest.setType(_MAPPING_NAME);

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		bulkDocumentRequest.addBulkableDocumentRequest(indexDocumentRequest);

		Document document2 = new DocumentImpl();

		document2.addKeyword(Field.UID, "2");
		document2.addKeyword(_FIELD_NAME, Boolean.FALSE.toString());

		IndexDocumentRequest indexDocumentRequest2 = new IndexDocumentRequest(
			_INDEX_NAME, document2);

		indexDocumentRequest2.setType(_MAPPING_NAME);

		bulkDocumentRequest.addBulkableDocumentRequest(indexDocumentRequest2);

		BulkDocumentResponse bulkDocumentResponse =
			_searchEngineAdapter.execute(bulkDocumentRequest);

		Assert.assertFalse(bulkDocumentResponse.hasErrors());

		List<BulkDocumentItemResponse> bulkDocumentItemResponses =
			bulkDocumentResponse.getBulkDocumentItemResponses();

		Assert.assertEquals(
			bulkDocumentItemResponses.toString(), 2,
			bulkDocumentItemResponses.size());

		BulkDocumentItemResponse bulkDocumentItemResponse1 =
			bulkDocumentItemResponses.get(0);

		Assert.assertEquals("1", bulkDocumentItemResponse1.getId());

		BulkDocumentItemResponse bulkDocumentItemResponse2 =
			bulkDocumentItemResponses.get(1);

		Assert.assertEquals("2", bulkDocumentItemResponse2.getId());

		DeleteDocumentRequest deleteDocumentRequest = new DeleteDocumentRequest(
			_INDEX_NAME, "1");

		deleteDocumentRequest.setType(_MAPPING_NAME);

		BulkDocumentRequest bulkDocumentRequest2 = new BulkDocumentRequest();

		bulkDocumentRequest2.addBulkableDocumentRequest(deleteDocumentRequest);

		Document document2Update = new DocumentImpl();

		document2Update.addKeyword(Field.UID, "2");
		document2Update.addKeyword(_FIELD_NAME, Boolean.TRUE.toString());

		UpdateDocumentRequest updateDocumentRequest = new UpdateDocumentRequest(
			_INDEX_NAME, "2", document2Update);

		updateDocumentRequest.setType(_MAPPING_NAME);

		bulkDocumentRequest2.addBulkableDocumentRequest(updateDocumentRequest);

		BulkDocumentResponse bulkDocumentResponse2 =
			_searchEngineAdapter.execute(bulkDocumentRequest2);

		Assert.assertFalse(bulkDocumentResponse2.hasErrors());

		List<BulkDocumentItemResponse> bulkDocumentItemResponses2 =
			bulkDocumentResponse2.getBulkDocumentItemResponses();

		Assert.assertEquals(
			bulkDocumentItemResponses2.toString(), 2,
			bulkDocumentItemResponses2.size());

		BulkDocumentItemResponse bulkDocumentItemResponse3 =
			bulkDocumentItemResponses2.get(0);

		Assert.assertEquals("1", bulkDocumentItemResponse3.getId());

		BulkDocumentItemResponse bulkDocumentItemResponse4 =
			bulkDocumentItemResponses2.get(1);

		Assert.assertEquals("2", bulkDocumentItemResponse4.getId());

		GetResponse getResponse1 = _getDocument("1");

		Assert.assertFalse(getResponse1.isExists());

		GetResponse getResponse2 = _getDocument("2");

		Assert.assertTrue(getResponse2.isExists());

		Map<String, Object> map2 = getResponse2.getSource();

		Assert.assertEquals(Boolean.TRUE.toString(), map2.get(_FIELD_NAME));
	}

	@Ignore
	@Test
	public void testExecuteDeleteByQueryDocumentRequest() {
		String documentSource1 = "{\"" + _FIELD_NAME + "\":\"true\"}";
		String documentSource2 = "{\"" + _FIELD_NAME + "\":\"false\"}";

		_indexDocument(documentSource1, "1");
		_indexDocument(documentSource2, "2");

		BooleanQuery query = new BooleanQueryImpl();

		query.addExactTerm(_FIELD_NAME, true);

		DeleteByQueryDocumentRequest deleteByQueryDocumentRequest =
			new DeleteByQueryDocumentRequest(query, new String[] {_INDEX_NAME});

		DeleteByQueryDocumentResponse deleteByQueryDocumentResponse =
			_searchEngineAdapter.execute(deleteByQueryDocumentRequest);

		Assert.assertEquals(1, deleteByQueryDocumentResponse.getDeleted());
	}

	@Test
	public void testExecuteDeleteDocumentRequest() {
		String documentSource = "{\"" + _FIELD_NAME + "\":\"true\"}";
		String id = "1";

		_indexDocument(documentSource, id);

		GetResponse getResponse1 = _getDocument(id);

		Assert.assertTrue(getResponse1.isExists());

		DeleteDocumentRequest deleteDocumentRequest = new DeleteDocumentRequest(
			_INDEX_NAME, id);

		deleteDocumentRequest.setType(_MAPPING_NAME);

		DeleteDocumentResponse deleteDocumentResponse =
			_searchEngineAdapter.execute(deleteDocumentRequest);

		Assert.assertEquals(
			RestStatus.OK.getStatus(), deleteDocumentResponse.getStatus());

		GetResponse getResponse2 = _getDocument(id);

		Assert.assertFalse(getResponse2.isExists());
	}

	@Test
	public void testExecuteIndexDocumentRequest() {
		Document document = new DocumentImpl();

		document.addKeyword(Field.UID, "1");

		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			_INDEX_NAME, document);

		indexDocumentRequest.setType(_MAPPING_NAME);

		IndexDocumentResponse indexDocumentResponse =
			_searchEngineAdapter.execute(indexDocumentRequest);

		Assert.assertEquals(
			RestStatus.CREATED.getStatus(), indexDocumentResponse.getStatus());
	}

	@Ignore
	@Test
	public void testExecuteUpdateByQueryDocumentRequest() {
		String documentSource = "{\"" + _FIELD_NAME + "\":\"true\"}";

		_indexDocument(documentSource, "1");

		BooleanQuery query = new BooleanQueryImpl();

		query.addExactTerm(_FIELD_NAME, true);

		UpdateByQueryDocumentRequest updateByQueryDocumentRequest =
			new UpdateByQueryDocumentRequest(
				query, null, new String[] {_INDEX_NAME});

		UpdateByQueryDocumentResponse updateByQueryDocumentResponse =
			_searchEngineAdapter.execute(updateByQueryDocumentRequest);

		Assert.assertEquals(1, updateByQueryDocumentResponse.getUpdated());
	}

	@Test
	public void testExecuteUpdateDocumentRequest() {
		String documentSource = "{\"" + _FIELD_NAME + "\":\"true\"}";
		String id = "1";

		_indexDocument(documentSource, id);

		GetResponse getResponse1 = _getDocument(id);

		Map<String, Object> map1 = getResponse1.getSource();

		Assert.assertEquals(Boolean.TRUE.toString(), map1.get(_FIELD_NAME));

		Document document = new DocumentImpl();

		document.addKeyword(Field.UID, id);
		document.addKeyword(_FIELD_NAME, false);

		UpdateDocumentRequest updateDocumentRequest = new UpdateDocumentRequest(
			_INDEX_NAME, id, document);

		updateDocumentRequest.setType(_MAPPING_NAME);

		UpdateDocumentResponse updateDocumentResponse =
			_searchEngineAdapter.execute(updateDocumentRequest);

		Assert.assertEquals(
			RestStatus.OK.getStatus(), updateDocumentResponse.getStatus());

		GetResponse getResponse2 = _getDocument(id);

		Map<String, Object> map2 = getResponse2.getSource();

		Assert.assertEquals(Boolean.FALSE.toString(), map2.get(_FIELD_NAME));
	}

	protected DocumentRequestExecutor createDocumentRequestExecutor(
		ElasticsearchConnectionManager elasticsearchConnectionManager) {

		DocumentRequestExecutorFixture documentRequestExecutorFixture =
			new DocumentRequestExecutorFixture(elasticsearchConnectionManager);

		return documentRequestExecutorFixture.createExecutor();
	}

	protected void createIndex() {
		IndicesClient indicesClient =
			_elasticsearchConnectionManager.getIndicesClient();

		CreateIndexRequest createIndexRequest = new CreateIndexRequest(
			_INDEX_NAME);

		createIndexRequest.mapping(
			_MAPPING_NAME, _MAPPING_SOURCE, XContentType.JSON);

		try {
			indicesClient.create(createIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	protected SearchEngineAdapter createSearchEngineAdapter(
		ElasticsearchConnectionManager elasticsearchConnectionManager) {

		return new ElasticsearchSearchEngineAdapterImpl() {
			{
				documentRequestExecutor = createDocumentRequestExecutor(
					elasticsearchConnectionManager);
			}
		};
	}

	protected void deleteIndex() {
		IndicesClient indicesClient =
			_elasticsearchConnectionManager.getIndicesClient();

		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(
			_INDEX_NAME);

		try {
			indicesClient.delete(deleteIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private GetResponse _getDocument(String id) {
		GetRequest getRequest = new GetRequest();

		getRequest.id(id);
		getRequest.index(_INDEX_NAME);

		try {
			return _restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private void _indexDocument(String documentSource, String id) {
		IndexRequest indexRequest = new IndexRequest(
			_INDEX_NAME, _MAPPING_NAME);

		indexRequest.id(id);
		indexRequest.source(documentSource, XContentType.JSON);

		try {
			_restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private static final String _FIELD_NAME = "matchDocument";

	private static final String _INDEX_NAME = "test_request_index";

	private static final String _MAPPING_NAME = "testDocumentMapping";

	private static final String _MAPPING_SOURCE =
		"{\"properties\":{\"matchDocument\":{\"type\":\"boolean\"}}}";

	private final DocumentFixture _documentFixture = new DocumentFixture();
	private ElasticsearchConnectionManager _elasticsearchConnectionManager;
	private ElasticsearchFixture _elasticsearchFixture;
	private RestHighLevelClient _restHighLevelClient;
	private SearchEngineAdapter _searchEngineAdapter;

}