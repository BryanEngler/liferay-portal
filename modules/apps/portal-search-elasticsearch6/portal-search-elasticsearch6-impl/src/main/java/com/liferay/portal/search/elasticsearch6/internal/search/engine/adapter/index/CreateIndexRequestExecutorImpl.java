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

package com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter.index;

import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.adapter.index.CreateIndexRequest;
import com.liferay.portal.search.engine.adapter.index.CreateIndexResponse;

import java.io.IOException;

import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(service = CreateIndexRequestExecutor.class)
public class CreateIndexRequestExecutorImpl
	implements CreateIndexRequestExecutor {

	@Override
	public CreateIndexResponse execute(CreateIndexRequest createIndexRequest) {
		org.elasticsearch.action.admin.indices.create.CreateIndexRequest
			elasticsearchCreateIndexRequest = createCreateIndexRequest(
				createIndexRequest);

		org.elasticsearch.action.admin.indices.create.CreateIndexResponse
			elasticsearchCreateIndexResponse = getCreateIndexResponse(
				elasticsearchCreateIndexRequest);

		CreateIndexResponse createIndexResponse = new CreateIndexResponse(
			elasticsearchCreateIndexResponse.isAcknowledged());

		return createIndexResponse;
	}

	protected org.elasticsearch.action.admin.indices.create.CreateIndexRequest
		createCreateIndexRequest(CreateIndexRequest createIndexRequest) {

		org.elasticsearch.action.admin.indices.create.CreateIndexRequest
			elasticsearchCreateIndexRequest =
				new org.elasticsearch.action.admin.indices.create.
					CreateIndexRequest(createIndexRequest.getIndexName());

		elasticsearchCreateIndexRequest.source(
			createIndexRequest.getSource(), XContentType.JSON);

		return elasticsearchCreateIndexRequest;
	}

	protected org.elasticsearch.action.admin.indices.create.CreateIndexResponse
		getCreateIndexResponse(
			org.elasticsearch.action.admin.indices.create.CreateIndexRequest
				elasticsearchCreateIndexRequest) {

		IndicesClient indicesClient =
			elasticsearchConnectionManager.getIndicesClient();

		try {
			return indicesClient.create(
				elasticsearchCreateIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

}