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

package com.liferay.portal.search.opensearch.internal.search.engine.adapter.index;

import com.liferay.portal.search.opensearch.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.engine.adapter.index.CloseIndexRequest;
import com.liferay.portal.search.engine.adapter.index.CloseIndexResponse;
import com.liferay.portal.search.engine.adapter.index.IndicesOptions;

import java.io.IOException;

import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.TimeValue;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(service = CloseIndexRequestExecutor.class)
public class CloseIndexRequestExecutorImpl
	implements CloseIndexRequestExecutor {

	@Override
	public CloseIndexResponse execute(CloseIndexRequest closeIndexRequest) {
		org.opensearch.client.indices.CloseIndexRequest
			elasticsearchCloseIndexRequest = createCloseIndexRequest(
				closeIndexRequest);

		AcknowledgedResponse acknowledgedResponse = getAcknowledgedResponse(
			elasticsearchCloseIndexRequest, closeIndexRequest);

		return new CloseIndexResponse(acknowledgedResponse.isAcknowledged());
	}

	protected org.opensearch.client.indices.CloseIndexRequest
		createCloseIndexRequest(CloseIndexRequest closeIndexRequest) {

		org.opensearch.client.indices.CloseIndexRequest
			elasticsearchCloseIndexRequest =
				new org.opensearch.client.indices.CloseIndexRequest(
					closeIndexRequest.getIndexNames());

		IndicesOptions indicesOptions = closeIndexRequest.getIndicesOptions();

		if (indicesOptions != null) {
			elasticsearchCloseIndexRequest.indicesOptions(
				_indicesOptionsTranslator.translate(indicesOptions));
		}

		if (closeIndexRequest.getTimeout() > 0) {
			TimeValue timeValue = TimeValue.timeValueMillis(
				closeIndexRequest.getTimeout());

			elasticsearchCloseIndexRequest.setMasterTimeout(timeValue);
			elasticsearchCloseIndexRequest.setTimeout(timeValue);
		}

		return elasticsearchCloseIndexRequest;
	}

	protected AcknowledgedResponse getAcknowledgedResponse(
		org.opensearch.client.indices.CloseIndexRequest
			elasticsearchCloseIndexRequest,
		CloseIndexRequest closeIndexRequest) {

		RestHighLevelClient restHighLevelClient =
			_elasticsearchClientResolver.getRestHighLevelClient(
				closeIndexRequest.getConnectionId(),
				closeIndexRequest.isPreferLocalCluster());

		IndicesClient indicesClient = restHighLevelClient.indices();

		try {
			return indicesClient.close(
				elasticsearchCloseIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	@Reference(unbind = "-")
	protected void setElasticsearchClientResolver(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
	}

	@Reference(unbind = "-")
	protected void setIndicesOptionsTranslator(
		IndicesOptionsTranslator indicesOptionsTranslator) {

		_indicesOptionsTranslator = indicesOptionsTranslator;
	}

	private ElasticsearchClientResolver _elasticsearchClientResolver;
	private IndicesOptionsTranslator _indicesOptionsTranslator;

}