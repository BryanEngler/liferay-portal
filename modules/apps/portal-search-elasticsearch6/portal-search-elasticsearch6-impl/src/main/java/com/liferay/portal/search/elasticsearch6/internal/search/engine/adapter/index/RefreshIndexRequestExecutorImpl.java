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

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.adapter.index.IndexRequestShardFailure;
import com.liferay.portal.search.engine.adapter.index.RefreshIndexRequest;
import com.liferay.portal.search.engine.adapter.index.RefreshIndexResponse;

import java.io.IOException;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(service = RefreshIndexRequestExecutor.class)
public class RefreshIndexRequestExecutorImpl
	implements RefreshIndexRequestExecutor {

	@Override
	public RefreshIndexResponse execute(
		RefreshIndexRequest refreshIndexRequest) {

		RefreshRequest refreshRequest = createRefreshRequest(
			refreshIndexRequest);

		RefreshResponse refreshResponse = getRefreshResponse(refreshRequest);

		RefreshIndexResponse refreshIndexResponse = new RefreshIndexResponse();

		refreshIndexResponse.setFailedShards(refreshResponse.getFailedShards());
		refreshIndexResponse.setSuccessfulShards(
			refreshResponse.getSuccessfulShards());
		refreshIndexResponse.setTotalShards(refreshResponse.getTotalShards());

		ShardOperationFailedException[] shardOperationFailedExceptions =
			refreshResponse.getShardFailures();

		if (ArrayUtil.isNotEmpty(shardOperationFailedExceptions)) {
			for (ShardOperationFailedException shardOperationFailedException :
					shardOperationFailedExceptions) {

				IndexRequestShardFailure indexRequestShardFailure =
					indexRequestShardFailureTranslator.translate(
						shardOperationFailedException);

				refreshIndexResponse.addIndexRequestShardFailure(
					indexRequestShardFailure);
			}
		}

		return refreshIndexResponse;
	}

	protected RefreshRequest createRefreshRequest(
		RefreshIndexRequest refreshIndexRequest) {

		RefreshRequest refreshRequest = new RefreshRequest(
			refreshIndexRequest.getIndexNames());

		return refreshRequest;
	}

	protected RefreshResponse getRefreshResponse(
		RefreshRequest refreshRequest) {

		IndicesClient indicesClient =
			elasticsearchConnectionManager.getIndicesClient();

		try {
			return indicesClient.refresh(
				refreshRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	@Reference
	protected IndexRequestShardFailureTranslator
		indexRequestShardFailureTranslator;

}