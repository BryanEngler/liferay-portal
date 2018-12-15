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

package com.liferay.portal.search.elasticsearch7.internal.connection;

import com.liferay.portal.search.elasticsearch7.configuration.ElasticsearchConfiguration;
import com.liferay.portal.search.elasticsearch7.internal.index.IndexFactory;
import com.liferay.portal.search.elasticsearch7.internal.settings.SettingsBuilder;
import com.liferay.portal.search.elasticsearch7.settings.ClientSettingsHelper;
import com.liferay.portal.search.elasticsearch7.settings.SettingsContributor;

import java.io.IOException;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;

/**
 * @author Michael C. Han
 */
public abstract class BaseElasticsearchConnection
	implements ElasticsearchConnection {

	@Override
	public void close() {
		if (_client != null) {
			_client.close();

			_client = null;
		}

		if (_restHighLevelClient == null) {
			return;
		}

		try {
			_restHighLevelClient.close();
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

		_restHighLevelClient = null;
	}

	@Override
	public void connect() {
		settingsBuilder = new SettingsBuilder(Settings.builder());

		loadConfigurations();

		loadSettingsContributors();

		_client = createClient();

		_restHighLevelClient = createRestHighLevelClient();
	}

	@Override
	public Client getClient() {
		return _client;
	}

	@Override
	public ClusterHealthResponse getClusterHealthResponse(long timeout) {
		ClusterClient clusterClient = _restHighLevelClient.cluster();

		ClusterHealthRequest clusterHealthRequest = new ClusterHealthRequest();

		clusterHealthRequest.timeout(TimeValue.timeValueMillis(timeout));

		clusterHealthRequest.waitForYellowStatus();

		try {
			return clusterClient.health(
				clusterHealthRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new IllegalStateException(ioe);
		}
	}

	@Override
	public RestHighLevelClient getRestHighLevelClient() {
		return _restHighLevelClient;
	}

	public boolean isConnected() {
		if ((_restHighLevelClient != null) && (_client != null)) {
			return true;
		}

		return false;
	}

	public void setIndexFactory(IndexFactory indexFactory) {
		_indexFactory = indexFactory;
	}

	protected void addSettingsContributor(
		SettingsContributor settingsContributor) {

		_settingsContributors.add(settingsContributor);
	}

	protected abstract Client createClient();

	protected abstract RestHighLevelClient createRestHighLevelClient();

	protected IndexFactory getIndexFactory() {
		return _indexFactory;
	}

	protected abstract void loadConfigurations();

	protected void loadSettingsContributors() {
		ClientSettingsHelper clientSettingsHelper = new ClientSettingsHelper() {

			@Override
			public void put(String setting, String value) {
				settingsBuilder.put(setting, value);
			}

			@Override
			public void putArray(String setting, String... values) {
				settingsBuilder.putList(setting, values);
			}

		};

		for (SettingsContributor settingsContributor : _settingsContributors) {
			settingsContributor.populate(clientSettingsHelper);
		}
	}

	protected void removeSettingsContributor(
		SettingsContributor settingsContributor) {

		_settingsContributors.remove(settingsContributor);
	}

	protected volatile ElasticsearchConfiguration elasticsearchConfiguration;
	protected SettingsBuilder settingsBuilder = new SettingsBuilder(
		Settings.builder());

	private Client _client;
	private IndexFactory _indexFactory;
	private RestHighLevelClient _restHighLevelClient;
	private final Set<SettingsContributor> _settingsContributors =
		new ConcurrentSkipListSet<>();

}