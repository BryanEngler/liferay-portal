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

package com.liferay.portal.search.opensearch.internal.search.engine.adapter.ccr;

import com.liferay.portal.search.opensearch.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.engine.adapter.ccr.PutFollowCCRRequest;
import com.liferay.portal.search.engine.adapter.ccr.PutFollowCCRResponse;

import java.io.IOException;

//import org.opensearch.action.support.ActiveShardCount;
//import org.opensearch.client.CcrClient;
//import org.opensearch.client.RequestOptions;
//import org.opensearch.client.RestHighLevelClient;
//import org.opensearch.client.ccr.PutFollowRequest;
//import org.opensearch.client.ccr.PutFollowResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(service = PutFollowCCRRequestExecutor.class)
public class PutFollowCCRRequestExecutorImpl
	implements PutFollowCCRRequestExecutor {

	@Override
	public PutFollowCCRResponse execute(
		PutFollowCCRRequest putFollowCCRRequest) {

//		PutFollowRequest putFollowRequest = createPutFollowRequest(
//			putFollowCCRRequest);
//
//		PutFollowResponse putFollowResponse = getPutFollowResponse(
//			putFollowRequest, putFollowCCRRequest);
//
//		return new PutFollowCCRResponse(
//			putFollowResponse.isFollowIndexCreated(),
//			putFollowResponse.isIndexFollowingStarted());
		throw new UnsupportedOperationException();
	}

//	protected PutFollowRequest createPutFollowRequest(
//		PutFollowCCRRequest putFollowCCRRequest) {
//
//		if (putFollowCCRRequest.getWaitForActiveShards() != 0) {
//			return new PutFollowRequest(
//				putFollowCCRRequest.getRemoteClusterAlias(),
//				putFollowCCRRequest.getLeaderIndexName(),
//				putFollowCCRRequest.getFollowerIndexName(),
//				ActiveShardCount.from(
//					putFollowCCRRequest.getWaitForActiveShards()));
//		}
//
//		return new PutFollowRequest(
//			putFollowCCRRequest.getRemoteClusterAlias(),
//			putFollowCCRRequest.getLeaderIndexName(),
//			putFollowCCRRequest.getFollowerIndexName());
//	}
//
//	protected PutFollowResponse getPutFollowResponse(
//		PutFollowRequest putFollowRequest,
//		PutFollowCCRRequest putFollowCCRRequest) {
//
//		RestHighLevelClient restHighLevelClient =
//			_elasticsearchClientResolver.getRestHighLevelClient(
//				putFollowCCRRequest.getConnectionId(),
//				putFollowCCRRequest.isPreferLocalCluster());
//
//		CcrClient ccrClient = restHighLevelClient.ccr();
//
//		try {
//			return ccrClient.putFollow(
//				putFollowRequest, RequestOptions.DEFAULT);
//		}
//		catch (IOException ioException) {
//			throw new RuntimeException(ioException);
//		}
//	}

	@Reference(unbind = "-")
	protected void setElasticsearchClientResolver(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
	}

	private ElasticsearchClientResolver _elasticsearchClientResolver;

}