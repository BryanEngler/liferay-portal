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

package com.liferay.portal.search.elasticsearch7.internal.information;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.configuration.CrossClusterReplicationConfigurationWrapper;
import com.liferay.portal.search.elasticsearch7.configuration.ElasticsearchConfiguration;
import com.liferay.portal.search.elasticsearch7.configuration.ElasticsearchConnectionConfiguration;
import com.liferay.portal.search.elasticsearch7.configuration.OperationMode;
import com.liferay.portal.search.elasticsearch7.internal.ElasticsearchSearchEngine;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnection;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.ConnectionInformation;
import com.liferay.portal.search.engine.NodeInformation;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.cluster.ClusterHealthStatus;
import com.liferay.portal.search.engine.adapter.cluster.HealthClusterRequest;
import com.liferay.portal.search.engine.adapter.cluster.HealthClusterResponse;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.util.EntityUtils;

import org.elasticsearch.Version;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * @author Adam Brandizzi
 */
@Component(
	configurationPid = "com.liferay.portal.search.elasticsearch7.configuration.ElasticsearchConfiguration",
	immediate = true, service = SearchEngineInformation.class
)
public class ElasticsearchSearchEngineInformation
	implements SearchEngineInformation {

	@Override
	public String getClientVersionString() {
		return Version.CURRENT.toString();
	}

	@Override
	public List<ConnectionInformation> getConnectionInformationList() {
		List<ConnectionInformation> connectionInformationList =
			new LinkedList<>();

		addMainConnection(
			elasticsearchConnectionManager.getElasticsearchConnection(),
			connectionInformationList);

		String filterString = String.format(
			"(&(service.factoryPid=%s)(active=%s)",
			ElasticsearchConnectionConfiguration.class.getName(), true);

		if (!isOperationModeEmbedded()) {
			filterString = filterString.concat(
				String.format(
					"(!(connectionId=%s))",
					elasticsearchConfiguration.remoteClusterConnectionId()));
		}

		if (!isOperationModeEmbedded() && isCrossClusterReplicationEnabled()) {
			String connectionId =
				crossClusterReplicationConfigurationWrapper.
					getCCRLocalClusterConnectionId();

			addCCRConnection(
				elasticsearchConnectionManager.getElasticsearchConnection(
					connectionId),
				connectionInformationList);

			filterString = filterString.concat(
				String.format("(!(connectionId=%s))", connectionId));
		}

		filterString = filterString.concat(")");

		try {
			addActiveConnections(filterString, connectionInformationList);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to get active connections", e);
			}
		}

		return connectionInformationList;
	}

	@Override
	public String getNodesString() {
		try {
			String clusterNodesString = getClusterNodesString(
				elasticsearchConnectionManager.getRestHighLevelClient());

			if (isCrossClusterReplicationEnabled()) {
				String localClusterNodesString = getClusterNodesString(
					elasticsearchConnectionManager.getRestHighLevelClient(
						null, true));

				if (!Validator.isBlank(localClusterNodesString)) {
					StringBundler sb = new StringBundler(11);

					sb.append(
						language.get(
							LocaleThreadLocal.getThemeDisplayLocale(),
							"remote-cluster"));
					sb.append(StringPool.SPACE);
					sb.append(StringPool.EQUAL);
					sb.append(StringPool.SPACE);
					sb.append(clusterNodesString);
					sb.append(StringPool.COMMA_AND_SPACE);
					sb.append(
						language.get(
							LocaleThreadLocal.getThemeDisplayLocale(),
							"local-cluster"));
					sb.append(StringPool.SPACE);
					sb.append(StringPool.EQUAL);
					sb.append(StringPool.SPACE);
					sb.append(localClusterNodesString);

					clusterNodesString = sb.toString();
				}
			}

			return clusterNodesString;
		}
		catch (Exception e) {
			return e.toString();
		}
	}

	@Override
	public String getVendorString() {
		String vendor = elasticsearchSearchEngine.getVendor();

		if (isOperationModeEmbedded()) {
			StringBundler sb = new StringBundler(5);

			sb.append(vendor);
			sb.append(StringPool.SPACE);
			sb.append(StringPool.OPEN_PARENTHESIS);
			sb.append(
				language.get(
					LocaleThreadLocal.getThemeDisplayLocale(), "embedded"));
			sb.append(StringPool.CLOSE_PARENTHESIS);

			return sb.toString();
		}

		return vendor;
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		elasticsearchConfiguration = ConfigurableUtil.createConfigurable(
			ElasticsearchConfiguration.class, properties);
	}

	protected void addActiveConnections(
			String filterString,
			List<ConnectionInformation> connectionInformationList)
		throws Exception {

		Configuration[] configurations = configurationAdmin.listConfigurations(
			filterString);

		for (Configuration configuration : configurations) {
			Dictionary<String, Object> properties =
				configuration.getProperties();

			String connectionId = (String)properties.get("connectionId");

			addConnectionInformation(
				elasticsearchConnectionManager.getElasticsearchConnection(
					connectionId),
				null, connectionInformationList);
		}
	}

	protected void addCCRConnection(
		ElasticsearchConnection elasticsearchConnection,
		List<ConnectionInformation> connectionInformationList) {

		addConnectionInformation(
			elasticsearchConnection, "read", connectionInformationList);
	}

	protected void addConnectionInformation(
		ElasticsearchConnection elasticsearchConnection, String label,
		List<ConnectionInformation> connectionInformationList) {

		if (elasticsearchConnection.getRestHighLevelClient() == null) {
			return;
		}

		ConnectionInformation connectionInformation =
			new ConnectionInformation();

		try {
			_setClusterAndNodeInformation(
				connectionInformation,
				elasticsearchConnection.getRestHighLevelClient());
		}
		catch (Exception e) {
			connectionInformation.setError(e.toString());

			if (_log.isWarnEnabled()) {
				_log.warn("Unable to get node information", e);
			}
		}

		connectionInformation.setConnectionId(
			elasticsearchConnection.getConnectionId());

		try {
			_setHealthInformation(
				connectionInformation,
				elasticsearchConnection.getConnectionId());
		}
		catch (RuntimeException re) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to get health information", re);
			}
		}

		if (!Validator.isBlank(label)) {
			connectionInformation.setLabel(label);
		}

		connectionInformationList.add(connectionInformation);
	}

	protected void addMainConnection(
		ElasticsearchConnection elasticsearchConnection,
		List<ConnectionInformation> connectionInformationList) {

		String label = "read-write";

		if (!isOperationModeEmbedded() && isCrossClusterReplicationEnabled()) {
			label = "write";
		}

		addConnectionInformation(
			elasticsearchConnection, label, connectionInformationList);
	}

	protected String getClusterNodesString(
		RestHighLevelClient restHighLevelClient) {

		try {
			if (restHighLevelClient == null) {
				return StringPool.BLANK;
			}

			ConnectionInformation connectionInformation =
				new ConnectionInformation();

			_setClusterAndNodeInformation(
				connectionInformation, restHighLevelClient);

			String clusterName = connectionInformation.getClusterName();

			List<NodeInformation> nodeInformations =
				connectionInformation.getNodeInformationList();

			Stream<NodeInformation> stream = nodeInformations.stream();

			String nodesString = stream.map(
				nodeInfo -> {
					StringBundler sb = new StringBundler(5);

					sb.append(nodeInfo.getName());
					sb.append(StringPool.SPACE);
					sb.append(StringPool.OPEN_PARENTHESIS);
					sb.append(nodeInfo.getVersion());
					sb.append(StringPool.CLOSE_PARENTHESIS);

					return sb.toString();
				}
			).collect(
				Collectors.joining(StringPool.COMMA_AND_SPACE)
			);

			StringBundler sb = new StringBundler(6);

			sb.append(clusterName);
			sb.append(StringPool.COLON);
			sb.append(StringPool.SPACE);
			sb.append(StringPool.OPEN_BRACKET);
			sb.append(nodesString);
			sb.append(StringPool.CLOSE_BRACKET);

			return sb.toString();
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to get node information", e);
			}

			StringBundler sb = new StringBundler(6);

			sb.append(StringPool.OPEN_PARENTHESIS);
			sb.append(
				language.get(
					LocaleThreadLocal.getThemeDisplayLocale(), "error"));
			sb.append(StringPool.COLON);
			sb.append(StringPool.SPACE);
			sb.append(e.toString());
			sb.append(StringPool.CLOSE_PARENTHESIS);

			return sb.toString();
		}
	}

	protected boolean isCrossClusterReplicationEnabled() {
		if (crossClusterReplicationConfigurationWrapper == null) {
			return false;
		}

		return crossClusterReplicationConfigurationWrapper.isCCREnabled();
	}

	protected boolean isOperationModeEmbedded() {
		OperationMode operationMode =
			elasticsearchConfiguration.operationMode();

		return Objects.equals(operationMode, OperationMode.EMBEDDED);
	}

	@Reference
	protected ConfigurationAdmin configurationAdmin;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	protected volatile CrossClusterReplicationConfigurationWrapper
		crossClusterReplicationConfigurationWrapper;

	protected volatile ElasticsearchConfiguration elasticsearchConfiguration;

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	@Reference
	protected ElasticsearchSearchEngine elasticsearchSearchEngine;

	@Reference
	protected Language language;

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	private void _setClusterAndNodeInformation(
			ConnectionInformation connectionInformation,
			RestHighLevelClient restHighLevelClient)
		throws Exception {

		RestClient restClient = restHighLevelClient.getLowLevelClient();

		String endpoint = "/_nodes";

		Request request = new Request("GET", endpoint);

		request.addParameter("timeout", "10000ms");

		Response response = restClient.performRequest(request);

		String responseBody = EntityUtils.toString(response.getEntity());

		JSONObject responseJSONObject = JSONFactoryUtil.createJSONObject(
			responseBody);

		String clusterName = GetterUtil.getString(
			responseJSONObject.get("cluster_name"));

		connectionInformation.setClusterName(clusterName);

		JSONObject nodesJSONObject = responseJSONObject.getJSONObject("nodes");

		Set<String> nodes = nodesJSONObject.keySet();

		List<NodeInformation> nodeInformationList = new ArrayList<>();

		for (String node : nodes) {
			JSONObject nodeJSONObject = nodesJSONObject.getJSONObject(node);

			NodeInformation nodeInformation = new NodeInformation();

			nodeInformation.setName(
				GetterUtil.getString(nodeJSONObject.get("name")));
			nodeInformation.setVersion(
				GetterUtil.getString(nodeJSONObject.get("version")));

			nodeInformationList.add(nodeInformation);
		}

		connectionInformation.setNodeInformationList(nodeInformationList);
	}

	private void _setHealthInformation(
		ConnectionInformation connectionInformation, String connectionId) {

		HealthClusterRequest healthClusterRequest = new HealthClusterRequest();

		healthClusterRequest.setConnectionId(connectionId);
		healthClusterRequest.setTimeout(1000);

		HealthClusterResponse healthClusterResponse =
			searchEngineAdapter.execute(healthClusterRequest);

		ClusterHealthStatus clusterHealthStatus =
			healthClusterResponse.getClusterHealthStatus();

		connectionInformation.setHealth(clusterHealthStatus.toString());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ElasticsearchSearchEngineInformation.class);

}