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
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexResponse;

import java.io.IOException;

import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(service = IndicesExistsIndexRequestExecutor.class)
public class IndicesExistsIndexRequestExecutorImpl
	implements IndicesExistsIndexRequestExecutor {

	@Override
	public IndicesExistsIndexResponse execute(
		IndicesExistsIndexRequest indicesExistsIndexRequest) {

		GetIndexRequest getIndexRequest = createGetIndexRequest(
			indicesExistsIndexRequest);

		boolean exists = indicesExists(getIndexRequest);

		IndicesExistsIndexResponse indicesExistsIndexResponse =
			new IndicesExistsIndexResponse(exists);

		return indicesExistsIndexResponse;
	}

	protected GetIndexRequest createGetIndexRequest(
		IndicesExistsIndexRequest indicesExistsIndexRequest) {

		GetIndexRequest getIndexRequest = new GetIndexRequest();

		getIndexRequest.indices(indicesExistsIndexRequest.getIndexNames());

		return getIndexRequest;
	}

	protected boolean indicesExists(GetIndexRequest getIndexRequest) {
		IndicesClient indicesClient =
			elasticsearchConnectionManager.getIndicesClient();

		try {
			return indicesClient.exists(
				getIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

}