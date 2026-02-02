/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.connection;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.TimeUnit;
import co.elastic.clients.elasticsearch._types.WaitForActiveShards;
import co.elastic.clients.elasticsearch.cluster.ElasticsearchClusterClient;
import co.elastic.clients.elasticsearch.cluster.HealthRequest;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;

import java.io.IOException;

/**
 * @author André de Oliveira
 */
public class ClusterHealthResponseUtil {

	public static HealthResponse getClusterHealthResponse(
		ElasticsearchClientResolver elasticsearchClientResolver,
		HealthExpectations healthExpectations) {

		ElasticsearchClient elasticsearchClient =
			elasticsearchClientResolver.getElasticsearchClient();

		ElasticsearchClusterClient elasticsearchClusterClient =
			elasticsearchClient.cluster();

		try {
			return elasticsearchClusterClient.health(
				_getHealthRequest(healthExpectations));
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	private static HealthRequest _getHealthRequest(
		HealthExpectations healthExpectations) {

		HealthRequest.Builder builder = new HealthRequest.Builder();

		builder.masterTimeout(
			Time.of(time -> time.time("10" + TimeUnit.Minutes.jsonValue())));
		builder.timeout(
			Time.of(time -> time.time("10" + TimeUnit.Minutes.jsonValue())));
		builder.waitForActiveShards(
			WaitForActiveShards.of(
				waitForActiveShards -> waitForActiveShards.count(
					healthExpectations.getActiveShards())));
		builder.waitForNodes(
			String.valueOf(healthExpectations.getNumberOfNodes()));
		builder.waitForNoRelocatingShards(true);
		builder.waitForStatus(healthExpectations.getHealthStatus());

		return builder.build();
	}

}