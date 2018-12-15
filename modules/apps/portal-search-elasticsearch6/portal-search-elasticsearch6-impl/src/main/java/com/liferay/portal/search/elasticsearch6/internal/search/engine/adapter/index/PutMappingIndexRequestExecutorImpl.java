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
import com.liferay.portal.search.engine.adapter.index.PutMappingIndexRequest;
import com.liferay.portal.search.engine.adapter.index.PutMappingIndexResponse;

import java.io.IOException;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Dylan Rebelak
 */
@Component(immediate = true, service = PutMappingIndexRequestExecutor.class)
public class PutMappingIndexRequestExecutorImpl
	implements PutMappingIndexRequestExecutor {

	@Override
	public PutMappingIndexResponse execute(
		PutMappingIndexRequest putMappingIndexRequest) {

		PutMappingRequest putMappingRequest = createPutMappingRequest(
			putMappingIndexRequest);

		AcknowledgedResponse acknowledgedResponse = getAcknowledgedResponse(
			putMappingRequest);

		return new PutMappingIndexResponse(
			acknowledgedResponse.isAcknowledged());
	}

	protected PutMappingRequest createPutMappingRequest(
		PutMappingIndexRequest putMappingIndexRequest) {

		PutMappingRequest putMappingRequest = new PutMappingRequest(
			putMappingIndexRequest.getIndexNames());

		putMappingRequest.source(
			putMappingIndexRequest.getMapping(), XContentType.JSON);
		putMappingRequest.type(putMappingIndexRequest.getMappingName());

		return putMappingRequest;
	}

	protected AcknowledgedResponse getAcknowledgedResponse(
		PutMappingRequest putMappingRequest) {

		IndicesClient indicesClient =
			elasticsearchConnectionManager.getIndicesClient();

		try {
			return indicesClient.putMapping(
				putMappingRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

}