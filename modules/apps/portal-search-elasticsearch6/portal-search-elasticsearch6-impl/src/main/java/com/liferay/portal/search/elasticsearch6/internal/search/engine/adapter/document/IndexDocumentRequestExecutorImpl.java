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

package com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter.document;

import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.adapter.document.BulkableDocumentRequestTranslator;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentResponse;

import java.io.IOException;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Dylan Rebelak
 */
@Component(immediate = true, service = IndexDocumentRequestExecutor.class)
public class IndexDocumentRequestExecutorImpl
	implements IndexDocumentRequestExecutor {

	@Override
	public IndexDocumentResponse execute(
		IndexDocumentRequest indexDocumentRequest) {

		IndexRequest indexRequest = bulkableDocumentRequestTranslator.translate(
			indexDocumentRequest, null);

		IndexResponse indexResponse = getIndexResponse(indexRequest);

		RestStatus restStatus = indexResponse.status();

		IndexDocumentResponse indexDocumentResponse = new IndexDocumentResponse(
			restStatus.getStatus());

		return indexDocumentResponse;
	}

	protected IndexResponse getIndexResponse(IndexRequest indexRequest) {
		RestHighLevelClient restHighLevelClient =
			elasticsearchConnectionManager.getRestHighLevelClient();

		try {
			return restHighLevelClient.index(
				indexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Reference(target = "(search.engine.impl=Elasticsearch)")
	protected BulkableDocumentRequestTranslator
		<DeleteRequest, IndexRequest, UpdateRequest, BulkRequest>
			bulkableDocumentRequestTranslator;

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

}