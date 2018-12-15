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

import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.query.QueryTranslator;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.adapter.document.DeleteByQueryDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.DeleteByQueryDocumentResponse;

import java.io.IOException;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Dylan Rebelak
 */
@Component(
	immediate = true, service = DeleteByQueryDocumentRequestExecutor.class
)
public class DeleteByQueryDocumentRequestExecutorImpl
	implements DeleteByQueryDocumentRequestExecutor {

	@Override
	public DeleteByQueryDocumentResponse execute(
		DeleteByQueryDocumentRequest deleteByQueryDocumentRequest) {

		DeleteByQueryRequest deleteByQueryRequest = createDeleteByQueryRequest(
			deleteByQueryDocumentRequest);

		BulkByScrollResponse bulkByScrollResponse = getBulkByScrollResponse(
			deleteByQueryRequest);

		TimeValue timeValue = bulkByScrollResponse.getTook();

		DeleteByQueryDocumentResponse deleteByQueryDocumentResponse =
			new DeleteByQueryDocumentResponse(
				bulkByScrollResponse.getDeleted(), timeValue.getMillis());

		return deleteByQueryDocumentResponse;
	}

	protected DeleteByQueryRequest createDeleteByQueryRequest(
		DeleteByQueryDocumentRequest deleteByQueryDocumentRequest) {

		DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest();

		Query query = deleteByQueryDocumentRequest.getQuery();

		QueryBuilder queryBuilder = queryTranslator.translate(query, null);

		deleteByQueryRequest.setQuery(queryBuilder);

		deleteByQueryRequest.setRefresh(
			deleteByQueryDocumentRequest.isRefresh());
		deleteByQueryRequest.indices(
			deleteByQueryDocumentRequest.getIndexNames());

		return deleteByQueryRequest;
	}

	protected BulkByScrollResponse getBulkByScrollResponse(
		DeleteByQueryRequest deleteByQueryRequest) {

		RestHighLevelClient restHighLevelClient =
			elasticsearchConnectionManager.getRestHighLevelClient();

		try {
			return restHighLevelClient.deleteByQuery(
				deleteByQueryRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	@Reference(target = "(search.engine.impl=Elasticsearch)")
	protected QueryTranslator<QueryBuilder> queryTranslator;

}