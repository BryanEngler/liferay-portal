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

package com.liferay.portal.search.elasticsearch6.internal.connection;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.InputStream;

import java.net.URL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequestBuilder;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.ClusterClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.transport.BoundTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.http.HttpInfo;

import org.hamcrest.CoreMatchers;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.MockitoAnnotations;

/**
 * @author André de Oliveira
 */
public class EmbeddedElasticsearchConnectionHttpTest {

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		_clusterName = RandomTestUtil.randomString();

		Map<String, Object> properties = new HashMap<String, Object>() {
			{
				put("clusterName", _clusterName);
				put("networkHost", "_site_");
			}
		};

		_elasticsearchFixture = new ElasticsearchFixture(
			EmbeddedElasticsearchConnectionHttpTest.class.getSimpleName(),
			properties);

		_elasticsearchFixture.setUp();
	}

	@After
	public void tearDown() throws Exception {
		_elasticsearchFixture.tearDown();
	}

	@Test
	public void testHttpLocallyAvailableRegardlessOfNetworkHost()
		throws Exception {

		String status = toString(new URL("http://localhost:" + getHttpPort()));

		Assert.assertThat(
			status,
			CoreMatchers.containsString(
				"\"cluster_name\" : \"" + _clusterName));
	}

	protected int getHttpPort() {
		RestHighLevelClient restHighLevelClient =
			_elasticsearchFixture.getRestHighLevelClient();

		ClusterClient clusterClient = restHighLevelClient.cluster();

		NodesInfoRequest nodesInfoRequest = new NodesInfoRequest();

		NodesInfoResponse nodesInfoResponse =
			//clusterClient.nodeInfo(nodesInfoRequest);
			new NodesInfoResponse();

		//no high level REST api yet. use low level client?
		RestClient restLowLevelClient =
			_elasticsearchFixture.getRestHighLevelClient()
				.getLowLevelClient();

		List<NodeInfo> nodeInfos = nodesInfoResponse.getNodes();

		NodeInfo nodeInfo = nodeInfos.get(0);

		HttpInfo httpInfo = nodeInfo.getHttp();

		BoundTransportAddress boundTransportAddress = httpInfo.address();

		TransportAddress transportAddress =
			boundTransportAddress.publishAddress();

		int port = transportAddress.getPort();

		return port;
	}

	protected String toString(URL url) throws Exception {
		try (InputStream inputStream = url.openStream()) {
			return StringUtil.read(inputStream);
		}
	}

	private String _clusterName;
	private ElasticsearchFixture _elasticsearchFixture;

}