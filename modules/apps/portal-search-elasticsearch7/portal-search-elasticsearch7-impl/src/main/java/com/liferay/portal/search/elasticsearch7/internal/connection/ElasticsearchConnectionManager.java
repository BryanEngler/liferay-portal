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

import com.liferay.osgi.util.ServiceTrackerFactory;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.configuration.CrossClusterReplicationConfigurationWrapper;
import com.liferay.portal.search.elasticsearch7.configuration.ElasticsearchConfiguration;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConnectionConfigurationWrapper;
import com.liferay.portal.search.elasticsearch7.internal.index.IndexFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Michael C. Han
 */
@Component(
	configurationPid = "com.liferay.portal.search.elasticsearch7.configuration.ElasticsearchConfiguration",
	immediate = true,
	service = {
		ElasticsearchClientResolver.class, ElasticsearchConnectionManager.class
	}
)
public class ElasticsearchConnectionManager
	implements ElasticsearchClientResolver {

	public ElasticsearchConnection getElasticsearchConnection() {
		return getElasticsearchConnection(null, false);
	}

	public Map<String, ElasticsearchConnection> getElasticsearchConnections() {
		return _elasticsearchConnections;
	}

	@Override
	public RestHighLevelClient getRestHighLevelClient() {
		return getRestHighLevelClient(null);
	}

	@Override
	public RestHighLevelClient getRestHighLevelClient(String connectionId) {
		return getRestHighLevelClient(connectionId, false);
	}

	@Override
	public RestHighLevelClient getRestHighLevelClient(
		String connectionId, boolean preferLocalCluster) {

		ElasticsearchConnection elasticsearchConnection =
			getElasticsearchConnection(connectionId, preferLocalCluster);

		if (elasticsearchConnection == null) {
			throw new ElasticsearchConnectionNotInitializedException();
		}

		return elasticsearchConnection.getRestHighLevelClient();
	}

	public synchronized void registerCompanyId(long companyId) {
		_companyIds.put(companyId, companyId);
	}

	public void removeElasticsearchConnection(String connectionId) {
		ElasticsearchConnection elasticsearchConnection =
			_elasticsearchConnections.get(connectionId);

		if (elasticsearchConnection != null) {
			elasticsearchConnection.close();

			_elasticsearchConnections.remove(connectionId);
		}
	}

	@Reference(
		cardinality = ReferenceCardinality.MANDATORY,
		target = "(operation.mode=EMBEDDED)",
		unbind = "unsetElasticsearchConnection"
	)
	public void setEmbeddedElasticsearchConnection(
		ElasticsearchConnection elasticsearchConnection) {

		_elasticsearchConnections.put(
			_EMBEDDED_CONNECTION_ID, elasticsearchConnection);
	}

	public void setOperationMode(OperationMode operationMode) {
		_operationMode = operationMode;
	}

	public synchronized void unregisterCompanyId(long companyId) {
		_companyIds.remove(companyId);
	}

	public void unsetElasticsearchConnection(
		ElasticsearchConnection elasticsearchConnection) {

		elasticsearchConnection.close();

		_elasticsearchConnections.remove(_EMBEDDED_CONNECTION_ID);
	}

	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		_bundleContext = bundleContext;
		_elasticsearchConfiguration = ConfigurableUtil.createConfigurable(
			ElasticsearchConfiguration.class, properties);
		_serviceTracker = ServiceTrackerFactory.open(
			bundleContext, ElasticsearchConnectionConfigurationWrapper.class,
			new ElasticsearchConnectionConfigurationWrapperServiceTrackerCustomizer());

		setOperationMode(
			translate(_elasticsearchConfiguration.operationMode()));
		setRestClientLoggerLevel();
	}

	protected synchronized void createCompanyIndexes() {
		for (Long companyId : _companyIds.values()) {
			try {
				RestHighLevelClient restHighLevelClient =
					getRestHighLevelClient();

				indexFactory.createIndices(
					restHighLevelClient.indices(), companyId);
			}
			catch (Exception e) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to reinitialize index for company " + companyId,
						e);
				}
			}
		}
	}

	@Deactivate
	protected void deactivate() {
		Collection<ElasticsearchConnection> elasticsearchConnections =
			_elasticsearchConnections.values();

		for (ElasticsearchConnection elasticsearchConnection :
				elasticsearchConnections) {

			elasticsearchConnection.close();
		}

		_serviceTracker.close();
	}

	protected ElasticsearchConnection getElasticsearchConnection(
		String connectionId, boolean preferLocalCluster) {

		if (!Validator.isBlank(connectionId)) {
			return _elasticsearchConnections.get(connectionId);
		}

		if (isOperationModeEmbedded()) {
			return _elasticsearchConnections.get(_EMBEDDED_CONNECTION_ID);
		}

		if (preferLocalCluster && isCrossClusterReplicationEnabled()) {
			return _elasticsearchConnections.get(
				crossClusterReplicationConfigurationWrapper.
					getCCRLocalClusterConnectionId());
		}

		return _elasticsearchConnections.get(
			_elasticsearchConfiguration.remoteClusterConnectionId());
	}

	protected boolean isCrossClusterReplicationEnabled() {
		if (crossClusterReplicationConfigurationWrapper == null) {
			return false;
		}

		return crossClusterReplicationConfigurationWrapper.getCCREnabled();
	}

	protected boolean isOperationModeEmbedded() {
		return Objects.equals(_operationMode, OperationMode.EMBEDDED);
	}

	@Modified
	protected synchronized void modified(Map<String, Object> properties) {
		_elasticsearchConfiguration = ConfigurableUtil.createConfigurable(
			ElasticsearchConfiguration.class, properties);

		setOperationMode(
			translate(_elasticsearchConfiguration.operationMode()));

		setRestClientLoggerLevel();

		createCompanyIndexes();
	}

	protected void setRestClientLoggerLevel() {
		org.apache.commons.logging.Log log = LogFactory.getLog(
			RestClient.class);

		if (log instanceof Log4JLogger) {
			Log4JLogger log4JLogger = (Log4JLogger)log;

			Logger logger = log4JLogger.getLogger();

			logger.setLevel(
				Level.toLevel(
					_elasticsearchConfiguration.restClientLoggerLevel()));
		}
	}

	protected OperationMode translate(
		com.liferay.portal.search.elasticsearch7.configuration.OperationMode
			operationMode) {

		return OperationMode.valueOf(operationMode.name());
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	protected volatile CrossClusterReplicationConfigurationWrapper
		crossClusterReplicationConfigurationWrapper;

	@Reference(unbind = "-")
	protected IndexFactory indexFactory;

	private static final String _EMBEDDED_CONNECTION_ID = "embedded";

	private static final Log _log = LogFactoryUtil.getLog(
		ElasticsearchConnectionManager.class);

	private BundleContext _bundleContext;
	private final Map<Long, Long> _companyIds = new HashMap<>();
	private volatile ElasticsearchConfiguration _elasticsearchConfiguration;
	private final Map<String, ElasticsearchConnection>
		_elasticsearchConnections = new HashMap<>();
	private OperationMode _operationMode;
	private ServiceTracker
		<ElasticsearchConnectionConfigurationWrapper,
		 ElasticsearchConnectionConfigurationWrapper> _serviceTracker;

	private class
		ElasticsearchConnectionConfigurationWrapperServiceTrackerCustomizer
			implements ServiceTrackerCustomizer
				<ElasticsearchConnectionConfigurationWrapper,
				 ElasticsearchConnectionConfigurationWrapper> {

		@Override
		public ElasticsearchConnectionConfigurationWrapper addingService(
			ServiceReference<ElasticsearchConnectionConfigurationWrapper>
				serviceReference) {

			ElasticsearchConnectionConfigurationWrapper
				elasticsearchConnectionConfigurationWrapper =
					_bundleContext.getService(serviceReference);

			_putElasticsearchConnection(
				elasticsearchConnectionConfigurationWrapper);

			return elasticsearchConnectionConfigurationWrapper;
		}

		@Override
		public void modifiedService(
			ServiceReference<ElasticsearchConnectionConfigurationWrapper>
				serviceReference,
			ElasticsearchConnectionConfigurationWrapper
				elasticsearchConnectionConfigurationWrapper) {

			_putElasticsearchConnection(
				elasticsearchConnectionConfigurationWrapper);
		}

		@Override
		public void removedService(
			ServiceReference<ElasticsearchConnectionConfigurationWrapper>
				serviceReference,
			ElasticsearchConnectionConfigurationWrapper
				elasticsearchConnectionConfigurationWrapper) {

			_elasticsearchConnections.remove(
				elasticsearchConnectionConfigurationWrapper.getConnectionId());
		}

		private void _putElasticsearchConnection(
			ElasticsearchConnectionConfigurationWrapper
				elasticsearchConnectionConfigurationWrapper) {

			String connectionId =
				elasticsearchConnectionConfigurationWrapper.getConnectionId();

			ElasticsearchConnection elasticsearchConnection =
				_elasticsearchConnections.get(connectionId);

			if (elasticsearchConnection != null) {
				elasticsearchConnection.close();
			}

			RemoteElasticsearchConnection remoteElasticsearchConnection =
				new RemoteElasticsearchConnection();

			remoteElasticsearchConnection.
				setElasticsearchConnectionConfigurationWrapper(
					elasticsearchConnectionConfigurationWrapper);

			remoteElasticsearchConnection.connect();

			if (remoteElasticsearchConnection.isConnected()) {
				_elasticsearchConnections.put(
					connectionId, remoteElasticsearchConnection);
			}
		}

	}

}