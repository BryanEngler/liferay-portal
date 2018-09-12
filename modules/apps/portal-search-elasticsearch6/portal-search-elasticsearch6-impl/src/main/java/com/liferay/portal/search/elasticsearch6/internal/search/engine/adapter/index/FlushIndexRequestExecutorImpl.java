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
import com.liferay.portal.search.engine.adapter.index.FlushIndexRequest;
import com.liferay.portal.search.engine.adapter.index.FlushIndexResponse;
import com.liferay.portal.search.engine.adapter.index.IndexRequestShardFailure;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.rest.RestStatus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;

/**
 * @author Michael C. Han
 */
@Component(service = FlushIndexRequestExecutor.class)
public class FlushIndexRequestExecutorImpl
	implements FlushIndexRequestExecutor {

	@Override
	public FlushIndexResponse execute(FlushIndexRequest flushIndexRequest) {
		FlushRequest flushRequest = createFlushRequest(
			flushIndexRequest);

		IndicesClient indicesClient =
			elasticsearchConnectionManager.getIndicesClient();

		try {
			FlushResponse flushResponse = indicesClient.flush(
				flushRequest, RequestOptions.DEFAULT);

			FlushIndexResponse flushIndexResponse = new FlushIndexResponse();

			flushIndexResponse.setFailedShards(flushResponse.getFailedShards());
			flushIndexResponse.setSuccessfulShards(
				flushResponse.getSuccessfulShards());
			flushIndexResponse.setTotalShards(flushResponse.getTotalShards());

			RestStatus restStatus = flushResponse.getStatus();

			flushIndexResponse.setRestStatus(restStatus.getStatus());

			ShardOperationFailedException[] shardOperationFailedExceptions =
				flushResponse.getShardFailures();

			if (ArrayUtil.isNotEmpty(shardOperationFailedExceptions)) {
				for (ShardOperationFailedException shardOperationFailedException :
					shardOperationFailedExceptions) {

					IndexRequestShardFailure indexRequestShardFailure =
						indexRequestShardFailureTranslator.translate(
							shardOperationFailedException);

					flushIndexResponse.addIndexRequestShardFailure(
						indexRequestShardFailure);
				}
			}

			return flushIndexResponse;
		}
		catch (IOException ioe) {
			return null;
		}
	}

	protected FlushRequest createFlushRequest(
		FlushIndexRequest flushIndexRequest) {

		FlushRequest flushRequest = new FlushRequest(
			flushIndexRequest.getIndexNames());

		flushRequest.force(flushIndexRequest.isForce());
		flushRequest.waitIfOngoing(flushIndexRequest.isWaitIfOngoing());

		return flushRequest;
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	@Reference
	protected IndexRequestShardFailureTranslator
		indexRequestShardFailureTranslator;

}