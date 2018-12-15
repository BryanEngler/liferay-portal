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

package com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter.cluster;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.adapter.cluster.ClusterHealthStatus;
import com.liferay.portal.search.engine.adapter.cluster.StatsClusterRequest;
import com.liferay.portal.search.engine.adapter.cluster.StatsClusterResponse;

import org.apache.http.util.EntityUtils;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Dylan Rebelak
 */
@Component(immediate = true, service = StatsClusterRequestExecutor.class)
public class StatsClusterRequestExecutorImpl
	implements StatsClusterRequestExecutor {

	@Override
	public StatsClusterResponse execute(
		StatsClusterRequest statsClusterRequest) {

		RestClient restLowLevelClient =
			elasticsearchConnectionManager.getRestLowLevelClient();

		Request request = new Request("GET", "/_cluster/stats");

		try {
			Response response = restLowLevelClient.performRequest(request);

			String responseBody = EntityUtils.toString(response.getEntity());

			JSONObject responseJSONObject = JSONFactoryUtil.createJSONObject(
				responseBody);

			String status = GetterUtil.getString(
				responseJSONObject.get("status"));

			ClusterHealthStatus clusterHealthStatus =
				clusterHealthStatusTranslator.translate(status);

			return new StatsClusterResponse(clusterHealthStatus, responseBody);
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
	}

	@Reference
	protected ClusterHealthStatusTranslator clusterHealthStatusTranslator;

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

}