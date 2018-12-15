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
import com.liferay.portal.search.engine.adapter.index.CloseIndexRequest;
import com.liferay.portal.search.engine.adapter.index.CloseIndexResponse;
import com.liferay.portal.search.engine.adapter.index.IndicesOptions;

import java.io.IOException;

import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;

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
		org.elasticsearch.action.admin.indices.close.CloseIndexRequest
			elasticsearchCloseIndexRequest = createCloseIndexRequest(
				closeIndexRequest);

		AcknowledgedResponse acknowledgedResponse = getAcknowledgedResponse(
			elasticsearchCloseIndexRequest);

		CloseIndexResponse closeIndexResponse = new CloseIndexResponse(
			acknowledgedResponse.isAcknowledged());

		return closeIndexResponse;
	}

	protected org.elasticsearch.action.admin.indices.close.CloseIndexRequest
		createCloseIndexRequest(CloseIndexRequest closeIndexRequest) {

		org.elasticsearch.action.admin.indices.close.CloseIndexRequest
			elasticsearchCloseIndexRequest = new
				org.elasticsearch.action.admin.indices.close.CloseIndexRequest(
					closeIndexRequest.getIndexNames());

		IndicesOptions indicesOptions = closeIndexRequest.getIndicesOptions();

		if (indicesOptions != null) {
			elasticsearchCloseIndexRequest.indicesOptions(
				indicesOptionsTranslator.translate(indicesOptions));
		}

		if (closeIndexRequest.getTimeout() > 0) {
			TimeValue timeValue = TimeValue.timeValueMillis(
				closeIndexRequest.getTimeout());

			elasticsearchCloseIndexRequest.masterNodeTimeout(timeValue);
			elasticsearchCloseIndexRequest.timeout(timeValue);
		}

		return elasticsearchCloseIndexRequest;
	}

	protected AcknowledgedResponse getAcknowledgedResponse(
		org.elasticsearch.action.admin.indices.close.CloseIndexRequest
			elasticsearchCloseIndexRequest) {

		IndicesClient indicesClient =
			elasticsearchConnectionManager.getIndicesClient();

		try {
			return indicesClient.close(
				elasticsearchCloseIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	@Reference
	protected IndicesOptionsTranslator indicesOptionsTranslator;

}