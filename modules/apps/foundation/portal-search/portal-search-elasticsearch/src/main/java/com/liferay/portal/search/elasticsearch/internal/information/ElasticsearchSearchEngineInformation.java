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

package com.liferay.portal.search.elasticsearch.internal.information;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.search.elasticsearch.connection.ElasticsearchConnection;
import com.liferay.portal.search.elasticsearch.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch.connection.OperationMode;
import com.liferay.portal.search.elasticsearch.internal.ElasticsearchSearchEngine;
import com.liferay.portal.search.engine.SearchEngineInformation;

import org.elasticsearch.Version;

import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequestBuilder;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.ClusterAdminClient;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * @author Adam Brandizzi
 */
@Component(immediate = true, service = SearchEngineInformation.class)
public class ElasticsearchSearchEngineInformation
	implements SearchEngineInformation {

	@Override
	public String getStatusString() {
		StringBundler sb = new StringBundler(4);

		sb.append(elasticsearchSearchEngine.getVendor());
		sb.append(CharPool.SPACE);
		sb.append(getVersion());

		ElasticsearchConnection elasticsearchConnection =
			elasticsearchConnectionManager.getElasticsearchConnection();

		if (elasticsearchConnection.getOperationMode() ==
				OperationMode.EMBEDDED) {

			sb.append(" (embedded)");
		}

		return sb.toString();
	}

	protected String getVersion() {
		ClusterAdminClient clusterAdminClient =
			elasticsearchConnectionManager.getClusterAdminClient();

		NodesInfoRequestBuilder nodesInfoRequestBuilder =
			clusterAdminClient.prepareNodesInfo("*");

		NodesInfoResponse nodesInfoResponse = nodesInfoRequestBuilder.get();

		List<NodeInfo> nodes = nodesInfoResponse.getNodes();

		NodeInfo nodeInfo = nodes.get(0);

		Version version = nodeInfo.getVersion();

		return version.toString();
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	@Reference
	protected ElasticsearchSearchEngine elasticsearchSearchEngine;

}