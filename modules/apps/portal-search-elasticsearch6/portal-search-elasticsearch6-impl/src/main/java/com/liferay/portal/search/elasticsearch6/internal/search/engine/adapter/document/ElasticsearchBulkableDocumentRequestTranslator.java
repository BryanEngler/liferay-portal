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

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.elasticsearch6.internal.document.DefaultElasticsearchDocumentFactory;
import com.liferay.portal.search.elasticsearch6.internal.document.ElasticsearchDocumentFactory;
import com.liferay.portal.search.engine.adapter.document.BulkableDocumentRequestTranslator;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;

import java.io.IOException;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 */
@Component(
	property = "search.engine.impl=Elasticsearch",
	service = BulkableDocumentRequestTranslator.class
)
public class ElasticsearchBulkableDocumentRequestTranslator
	implements BulkableDocumentRequestTranslator
		<DeleteRequest, IndexRequest, UpdateRequest, BulkRequest> {

	@Override
	public DeleteRequest translate(
		DeleteDocumentRequest deleteDocumentRequest,
		BulkRequest searchEngineAdapterRequest) {

		DeleteRequest deleteRequest = new DeleteRequest();

		deleteRequest.id(deleteDocumentRequest.getUid());
		deleteRequest.index(deleteDocumentRequest.getIndexName());

		if (deleteDocumentRequest.isRefresh()) {
			deleteRequest.setRefreshPolicy(
				WriteRequest.RefreshPolicy.IMMEDIATE);
		}

		deleteRequest.type(deleteDocumentRequest.getType());

		if (searchEngineAdapterRequest != null) {
			searchEngineAdapterRequest.add(deleteRequest);
		}

		return deleteRequest;
	}

	@Override
	public IndexRequest translate(
		IndexDocumentRequest indexDocumentRequest,
		BulkRequest searchEngineAdapterRequest) {

		try {
			Document document = indexDocumentRequest.getDocument();

			IndexRequest indexRequest = new IndexRequest();

			indexRequest.id(document.getUID());
			indexRequest.index(indexDocumentRequest.getIndexName());

			if (indexDocumentRequest.isRefresh()) {
				indexRequest.setRefreshPolicy(
					WriteRequest.RefreshPolicy.IMMEDIATE);
			}

			indexRequest.type(indexDocumentRequest.getType());

			ElasticsearchDocumentFactory elasticsearchDocumentFactory =
				new DefaultElasticsearchDocumentFactory();

			String elasticsearchDocument =
				elasticsearchDocumentFactory.getElasticsearchDocument(document);

			indexRequest.source(elasticsearchDocument, XContentType.JSON);

			if (searchEngineAdapterRequest != null) {
				searchEngineAdapterRequest.add(indexRequest);
			}

			return indexRequest;
		}
		catch (IOException ioe) {
			throw new SystemException(ioe);
		}
	}

	@Override
	public UpdateRequest translate(
		UpdateDocumentRequest updateDocumentRequest,
		BulkRequest searchEngineAdapterRequest) {

		try {
			Document document = updateDocumentRequest.getDocument();

			UpdateRequest updateRequest = new UpdateRequest();

			updateRequest.id(document.getUID());
			updateRequest.index(updateDocumentRequest.getIndexName());

			if (updateDocumentRequest.isRefresh()) {
				updateRequest.setRefreshPolicy(
					WriteRequest.RefreshPolicy.IMMEDIATE);
			}

			updateRequest.type(updateDocumentRequest.getType());

			ElasticsearchDocumentFactory elasticsearchDocumentFactory =
				new DefaultElasticsearchDocumentFactory();

			String elasticsearchDocument =
				elasticsearchDocumentFactory.getElasticsearchDocument(document);

			updateRequest.doc(elasticsearchDocument, XContentType.JSON);

			if (searchEngineAdapterRequest != null) {
				searchEngineAdapterRequest.add(updateRequest);
			}

			return updateRequest;
		}
		catch (IOException ioe) {
			throw new SystemException(ioe);
		}
	}

}