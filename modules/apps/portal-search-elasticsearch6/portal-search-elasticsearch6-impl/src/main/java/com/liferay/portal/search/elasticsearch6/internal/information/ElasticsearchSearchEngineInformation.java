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

package com.liferay.portal.search.elasticsearch6.internal.information;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.elasticsearch6.internal.ElasticsearchSearchEngine;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnection;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch6.internal.connection.OperationMode;
import com.liferay.portal.search.engine.SearchEngineInformation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.Version;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequestBuilder;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.unit.TimeValue;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adam Brandizzi
 */
@Component(immediate = true, service = SearchEngineInformation.class)
public class ElasticsearchSearchEngineInformation
	implements SearchEngineInformation {

	@Override
	public String getStatusString() {
		StringBundler sb = new StringBundler(9);

		sb.append(elasticsearchSearchEngine.getVendor());
		sb.append(CharPool.SPACE);
		sb.append(Version.CURRENT);

		ElasticsearchConnection elasticsearchConnection =
			elasticsearchConnectionManager.getElasticsearchConnection();

		if (elasticsearchConnection.getOperationMode() ==
				OperationMode.EMBEDDED) {

			sb.append(" (embedded)");

			return sb.toString();
		}

		try {
			Client client = elasticsearchConnectionManager.getClient();

			if (client == null) {
				return sb.toString();
			}

			List<NodeInfo> nodesInfo = _getClusterNodes(client);

			sb.append(" (cluster nodes and versions: ");

			Stream<NodeInfo> stream = nodesInfo.stream();

			String nodes = stream.map(
				nodeInfo -> {
					DiscoveryNode node = nodeInfo.getNode();

					return node.getName() + CharPool.SPACE + node.getVersion();
				}
			).collect(
				Collectors.joining(StringPool.COMMA_AND_SPACE)
			);

			sb.append(nodes);

			sb.append(")");
		}
		catch (Exception e) {
			_log.error(e, e);

			sb.append(" (error: ");
			sb.append(e.toString());
			sb.append(")");
		}

		return sb.toString();
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	@Reference
	protected ElasticsearchSearchEngine elasticsearchSearchEngine;

	private List<NodeInfo> _getClusterNodes(Client client) {
		AdminClient adminClient = client.admin();

		ClusterAdminClient clusterAdminClient = adminClient.cluster();

		NodesInfoRequestBuilder nodesInfoRequestBuilder =
			clusterAdminClient.prepareNodesInfo();

		TimeValue timeout = TimeValue.timeValueMillis(10000);

		NodesInfoResponse nodesInfoResponse = nodesInfoRequestBuilder.get(
			timeout);

		return nodesInfoResponse.getNodes();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ElasticsearchSearchEngineInformation.class);

}