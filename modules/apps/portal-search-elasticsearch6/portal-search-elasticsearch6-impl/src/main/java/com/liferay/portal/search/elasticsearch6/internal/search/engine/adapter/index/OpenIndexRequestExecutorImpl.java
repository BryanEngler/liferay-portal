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
import com.liferay.portal.search.engine.adapter.index.IndicesOptions;
import com.liferay.portal.search.engine.adapter.index.OpenIndexRequest;
import com.liferay.portal.search.engine.adapter.index.OpenIndexResponse;

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
@Component(service = OpenIndexRequestExecutor.class)
public class OpenIndexRequestExecutorImpl implements OpenIndexRequestExecutor {

	@Override
	public OpenIndexResponse execute(OpenIndexRequest openIndexRequest) {
		org.elasticsearch.action.admin.indices.open.OpenIndexRequest
			elasticsearchOpenIndexRequest = createOpenIndexRequest(
				openIndexRequest);

		AcknowledgedResponse acknowledgedResponse = getAcknowledgedResponse(
			elasticsearchOpenIndexRequest);

		OpenIndexResponse openIndexResponse = new OpenIndexResponse(
			acknowledgedResponse.isAcknowledged());

		return openIndexResponse;
	}

	protected org.elasticsearch.action.admin.indices.open.OpenIndexRequest
		createOpenIndexRequest(OpenIndexRequest openIndexRequest) {

		org.elasticsearch.action.admin.indices.open.OpenIndexRequest
			elasticsearchOpenIndexRequest =
				new
					org.elasticsearch.action.admin.indices.open.
						OpenIndexRequest();

		elasticsearchOpenIndexRequest.indices(openIndexRequest.getIndexNames());

		IndicesOptions indicesOptions = openIndexRequest.getIndicesOptions();

		if (indicesOptions != null) {
			elasticsearchOpenIndexRequest.indicesOptions(
				indicesOptionsTranslator.translate(indicesOptions));
		}

		if (openIndexRequest.getTimeout() > 0) {
			TimeValue timeValue = TimeValue.timeValueMillis(
				openIndexRequest.getTimeout());

			elasticsearchOpenIndexRequest.masterNodeTimeout(timeValue);
			elasticsearchOpenIndexRequest.timeout(timeValue);
		}

		if (openIndexRequest.getWaitForActiveShards() > 0) {
			elasticsearchOpenIndexRequest.waitForActiveShards(
				openIndexRequest.getWaitForActiveShards());
		}

		return elasticsearchOpenIndexRequest;
	}

	protected AcknowledgedResponse getAcknowledgedResponse(
		org.elasticsearch.action.admin.indices.open.OpenIndexRequest
			elasticsearchOpenIndexRequest) {

		IndicesClient indicesClient =
			elasticsearchConnectionManager.getIndicesClient();

		try {
			return indicesClient.open(
				elasticsearchOpenIndexRequest, RequestOptions.DEFAULT);
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