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
import com.liferay.portal.search.engine.adapter.document.BulkDocumentItemResponse;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.BulkableDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.BulkableDocumentRequestTranslator;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;

import java.io.IOException;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.RestStatus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(service = BulkDocumentRequestExecutor.class)
public class BulkDocumentRequestExecutorImpl
	implements BulkDocumentRequestExecutor {

	@Override
	public BulkDocumentResponse execute(
		BulkDocumentRequest bulkDocumentRequest) {

		BulkRequest bulkRequest = createBulkRequest(bulkDocumentRequest);

		BulkResponse bulkResponse = getBulkResponse(bulkRequest);

		TimeValue timeValue = bulkResponse.getTook();

		BulkDocumentResponse bulkDocumentResponse = new BulkDocumentResponse(
			timeValue.getMillis());

		for (BulkItemResponse bulkItemResponse : bulkResponse.getItems()) {
			BulkDocumentItemResponse bulkDocumentItemResponse =
				new BulkDocumentItemResponse();

			bulkDocumentResponse.addBulkDocumentItemResponse(
				bulkDocumentItemResponse);

			bulkDocumentItemResponse.setId(bulkItemResponse.getId());
			bulkDocumentItemResponse.setIndex(bulkItemResponse.getIndex());
			bulkDocumentItemResponse.setFailureMessage(
				bulkItemResponse.getFailureMessage());
			bulkDocumentItemResponse.setType(bulkItemResponse.getType());
			bulkDocumentItemResponse.setVersion(bulkItemResponse.getVersion());

			RestStatus restStatus = bulkItemResponse.status();

			if (bulkItemResponse.isFailed()) {
				bulkDocumentResponse.setErrors(true);

				BulkItemResponse.Failure bulkItemFailureResponse =
					bulkItemResponse.getFailure();

				bulkDocumentItemResponse.setAborted(
					bulkItemFailureResponse.isAborted());
				bulkDocumentItemResponse.setCause(
					bulkItemFailureResponse.getCause());
				restStatus = bulkItemFailureResponse.getStatus();
			}

			bulkDocumentItemResponse.setStatus(restStatus.getStatus());
		}

		return bulkDocumentResponse;
	}

	protected BulkRequest createBulkRequest(
		BulkDocumentRequest bulkDocumentRequest) {

		BulkRequest bulkRequest = new BulkRequest();

		if (bulkDocumentRequest.isRefresh()) {
			bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
		}

		for (BulkableDocumentRequest<?> bulkableDocumentRequest :
				bulkDocumentRequest.getBulkableDocumentRequests()) {

			bulkableDocumentRequest.accept(
				request -> {
					if (request instanceof DeleteDocumentRequest) {
						bulkableDocumentRequestTranslator.translate(
							(DeleteDocumentRequest)request, bulkRequest);
					}
					else if (request instanceof IndexDocumentRequest) {
						bulkableDocumentRequestTranslator.translate(
							(IndexDocumentRequest)request, bulkRequest);
					}
					else if (request instanceof UpdateDocumentRequest) {
						bulkableDocumentRequestTranslator.translate(
							(UpdateDocumentRequest)request, bulkRequest);
					}
					else {
						throw new IllegalArgumentException(
							"No translator available for: " + request);
					}
				});
		}

		return bulkRequest;
	}

	protected BulkResponse getBulkResponse(BulkRequest bulkRequest) {
		RestHighLevelClient restHighLevelClient =
			elasticsearchConnectionManager.getRestHighLevelClient();

		try {
			return restHighLevelClient.bulk(
				bulkRequest, RequestOptions.DEFAULT);
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