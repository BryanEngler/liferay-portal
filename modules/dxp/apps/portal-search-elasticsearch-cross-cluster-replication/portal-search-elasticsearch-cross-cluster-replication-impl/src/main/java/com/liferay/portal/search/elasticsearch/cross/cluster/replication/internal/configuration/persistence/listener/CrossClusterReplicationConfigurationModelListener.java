/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch.cross.cluster.replication.internal.configuration.persistence.listener;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskContextMapConstants;
import com.liferay.portal.kernel.cluster.ClusterExecutor;
import com.liferay.portal.kernel.cluster.ClusterNode;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.search.ccr.CrossClusterReplicationConfigurationHelper;
import com.liferay.portal.search.elasticsearch.cross.cluster.replication.internal.background.task.CrossClusterReplicationBackgroudTaskExecutor;
import com.liferay.portal.search.elasticsearch.cross.cluster.replication.internal.configuration.CrossClusterReplicationConfiguration;
import com.liferay.portal.search.engine.SearchEngineInformation;

import java.io.Serializable;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(
	enabled = false,
	property = "model.class.name=com.liferay.portal.search.elasticsearch.cross.cluster.replication.internal.configuration.CrossClusterReplicationConfiguration",
	service = ConfigurationModelListener.class
)
public class CrossClusterReplicationConfigurationModelListener
	implements ConfigurationModelListener {

	@Override
	public void onBeforeSave(String pid, Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		if (_crossClusterReplicationConfigurationHelper.
				isCrossClusterReplicationInitializing()) {

			throw new ConfigurationModelListenerException(
				_getMessage(
					"cross-cluster-replication-is-initializing-warning"),
				CrossClusterReplicationConfiguration.class, getClass(),
				properties);
		}

		boolean ccrEnabled = GetterUtil.getBoolean(
			properties.get("ccrEnabled"));
		String[] ccrLocalClusterConnectionConfigurations =
			GetterUtil.getStringValues(
				properties.get("ccrLocalClusterConnectionConfigurations"));

		if (ccrEnabled) {
			_validateCCRLocalClusterConnectionConfigurations(
				ccrLocalClusterConnectionConfigurations, properties);
		}

		Configuration[] configurations = null;

		try {
			String filterString = String.format("(service.pid=%s)", pid);

			configurations = configurationAdmin.listConfigurations(
				filterString);
		}
		catch (Exception exception) {
			throw new ConfigurationModelListenerException(
				exception.getMessage(),
				CrossClusterReplicationConfiguration.class, getClass(),
				properties);
		}

		if (configurations == null) {
			return;
		}

		Configuration configuration = configurations[0];

		Dictionary<String, Object> previousProperties =
			configuration.getProperties();

		boolean automaticReplicationEnabled = GetterUtil.getBoolean(
			properties.get("automaticReplicationEnabled"));
		String[] excludedIndexes = GetterUtil.getStringValues(
			properties.get("excludedIndexes"));
		boolean previousAutomaticReplicationEnabled = GetterUtil.getBoolean(
			previousProperties.get("automaticReplicationEnabled"), true);
		boolean previousCcrEnabled = GetterUtil.getBoolean(
			previousProperties.get("ccrEnabled"));
		String remoteClusterAlias = (String)properties.get(
			"remoteClusterAlias");
		String remoteClusterSeedNodeTransportAddress = (String)properties.get(
			"remoteClusterSeedNodeTransportAddress");

		if (previousCcrEnabled && previousAutomaticReplicationEnabled) {
			String[] previousCcrLocalClusterConnectionConfigurations =
				GetterUtil.getStringValues(
					previousProperties.get(
						"ccrLocalClusterConnectionConfigurations"));
			String[] previousExcludedIndexes = GetterUtil.getStringValues(
				previousProperties.get("excludedIndexes"));
			String previousRemoteClusterAlias = (String)previousProperties.get(
				"remoteClusterAlias");

			if (ccrEnabled && automaticReplicationEnabled) {
				String previousRemoteClusterSeedNodeTransportAddress =
					(String)previousProperties.get(
						"remoteClusterSeedNodeTransportAddress");

				if (!_equals(
						previousCcrLocalClusterConnectionConfigurations,
						ccrLocalClusterConnectionConfigurations) ||
					!previousRemoteClusterAlias.equals(remoteClusterAlias) ||
					!previousRemoteClusterSeedNodeTransportAddress.equals(
						remoteClusterSeedNodeTransportAddress) ||
					!_equals(previousExcludedIndexes, excludedIndexes)) {

					_restartReplication(
						ccrLocalClusterConnectionConfigurations,
						excludedIndexes, previousExcludedIndexes,
						previousCcrLocalClusterConnectionConfigurations,
						previousRemoteClusterAlias, properties,
						remoteClusterAlias,
						remoteClusterSeedNodeTransportAddress);
				}
			}
			else {
				_stopReplication(
					previousExcludedIndexes,
					previousCcrLocalClusterConnectionConfigurations,
					previousRemoteClusterAlias, properties);

				if (_log.isInfoEnabled()) {
					if (ccrEnabled) {
						_log.info(
							"Read operations from local clusters are enabled");
					}
					else {
						_log.info(
							"Read operations from local clusters are disabled");
					}
				}
			}
		}
		else {
			if (ccrEnabled && automaticReplicationEnabled) {
				_startReplication(
					ccrLocalClusterConnectionConfigurations, excludedIndexes,
					properties, remoteClusterAlias,
					remoteClusterSeedNodeTransportAddress);
			}
			else if (ccrEnabled && !automaticReplicationEnabled) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"Read operations from local clusters are enabled");
				}
			}
			else {
				if (_log.isInfoEnabled()) {
					_log.info(
						"Read operations from local clusters are disabled");
				}
			}
		}
	}

	@Reference
	protected ConfigurationAdmin configurationAdmin;

	private boolean _equals(String[] array1, String[] array2) {
		if (ArrayUtil.isEmpty(array1) && ArrayUtil.isEmpty(array2)) {
			return true;
		}

		if (!ArrayUtil.containsAll(array1, array2) ||
			!ArrayUtil.containsAll(array2, array1)) {

			return false;
		}

		return true;
	}

	private String _getMessage(String key, Object... arguments) {
		try {
			return ResourceBundleUtil.getString(
				_getResourceBundle(), key, arguments);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return null;
		}
	}

	private ResourceBundle _getResourceBundle() {
		return ResourceBundleUtil.getBundle(
			"content.Language", LocaleThreadLocal.getThemeDisplayLocale(),
			getClass());
	}

	private void _restartReplication(
			String[] ccrLocalClusterConnectionConfigurations,
			String[] excludedIndexes, String[] previousExcludedIndexes,
			String[] previousCcrLocalClusterConnectionConfigurations,
			String previousRemoteClusterAlias,
			Dictionary<String, Object> properties, String remoteClusterAlias,
			String remoteClusterSeedNodeTransportAddress)
		throws ConfigurationModelListenerException {

		if (_log.isInfoEnabled()) {
			_log.info("Restarting cross-cluster replication in the background");
		}

		Map<String, Serializable> taskContextMap =
			HashMapBuilder.<String, Serializable>put(
				BackgroundTaskContextMapConstants.DELETE_ON_SUCCESS, true
			).put(
				"ccrLocalClusterConnectionConfigurations",
				ccrLocalClusterConnectionConfigurations
			).put(
				"excludedIndexes", excludedIndexes
			).put(
				"previousCcrLocalClusterConnectionConfigurations",
				previousCcrLocalClusterConnectionConfigurations
			).put(
				"previousExcludedIndexes", previousExcludedIndexes
			).put(
				"previousRemoteClusterAlias", previousRemoteClusterAlias
			).put(
				"remoteClusterAlias", remoteClusterAlias
			).put(
				"remoteClusterSeedNodeTransportAddress",
				remoteClusterSeedNodeTransportAddress
			).build();

		try {
			_backgroundTaskManager.addBackgroundTask(
				UserConstants.USER_ID_DEFAULT,
				BackgroundTaskConstants.GROUP_ID_DEFAULT, "restartReplication",
				CrossClusterReplicationBackgroudTaskExecutor.class.getName(),
				taskContextMap, new ServiceContext());
		}
		catch (PortalException portalException) {
			throw new ConfigurationModelListenerException(
				portalException, CrossClusterReplicationConfiguration.class,
				getClass(), properties);
		}
	}

	private void _startReplication(
			String[] ccrLocalClusterConnectionConfigurations,
			String[] excludedIndexes, Dictionary<String, Object> properties,
			String remoteClusterAlias,
			String remoteClusterSeedNodeTransportAddress)
		throws ConfigurationModelListenerException {

		if (_log.isInfoEnabled()) {
			_log.info("Enabling cross-cluster replication in the background");
		}

		Map<String, Serializable> taskContextMap =
			HashMapBuilder.<String, Serializable>put(
				BackgroundTaskContextMapConstants.DELETE_ON_SUCCESS, true
			).put(
				"ccrLocalClusterConnectionConfigurations",
				ccrLocalClusterConnectionConfigurations
			).put(
				"excludedIndexes", excludedIndexes
			).put(
				"remoteClusterAlias", remoteClusterAlias
			).put(
				"remoteClusterSeedNodeTransportAddress",
				remoteClusterSeedNodeTransportAddress
			).build();

		try {
			_backgroundTaskManager.addBackgroundTask(
				UserConstants.USER_ID_DEFAULT,
				BackgroundTaskConstants.GROUP_ID_DEFAULT, "startReplication",
				CrossClusterReplicationBackgroudTaskExecutor.class.getName(),
				taskContextMap, new ServiceContext());
		}
		catch (PortalException portalException) {
			throw new ConfigurationModelListenerException(
				portalException, CrossClusterReplicationConfiguration.class,
				getClass(), properties);
		}
	}

	private void _stopReplication(
			String[] previousExcludedIndexes,
			String[] previousCcrLocalClusterConnectionConfigurations,
			String previousRemoteClusterAlias,
			Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		if (_log.isInfoEnabled()) {
			_log.info("Disabling cross-cluster replication in the background");
		}

		Map<String, Serializable> taskContextMap =
			HashMapBuilder.<String, Serializable>put(
				BackgroundTaskContextMapConstants.DELETE_ON_SUCCESS, true
			).put(
				"previousCcrLocalClusterConnectionConfigurations",
				previousCcrLocalClusterConnectionConfigurations
			).put(
				"previousExcludedIndexes", previousExcludedIndexes
			).put(
				"previousRemoteClusterAlias", previousRemoteClusterAlias
			).build();

		try {
			_backgroundTaskManager.addBackgroundTask(
				UserConstants.USER_ID_DEFAULT,
				BackgroundTaskConstants.GROUP_ID_DEFAULT, "stopReplication",
				CrossClusterReplicationBackgroudTaskExecutor.class.getName(),
				taskContextMap, new ServiceContext());
		}
		catch (PortalException portalException) {
			throw new ConfigurationModelListenerException(
				portalException, CrossClusterReplicationConfiguration.class,
				getClass(), properties);
		}
	}

	private void _validateCCRLocalClusterConnectionConfigurations(
			String[] ccrLocalClusterConnectionConfigurations,
			Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		if (ArrayUtil.isEmpty(ccrLocalClusterConnectionConfigurations)) {
			throw new ConfigurationModelListenerException(
				_getMessage("please-set-a-hostname-and-connection-id"),
				CrossClusterReplicationConfiguration.class, getClass(),
				properties);
		}

		ClusterNode localClusterNode = _clusterExecutor.getLocalClusterNode();

		if ((localClusterNode == null) &&
			(ccrLocalClusterConnectionConfigurations.length > 1)) {

			throw new ConfigurationModelListenerException(
				_getMessage(
					"please-set-only-one-config-when-liferay-is-not-clustered"),
				CrossClusterReplicationConfiguration.class, getClass(),
				properties);
		}

		List<String> connectionIds = TransformUtil.transform(
			_searchEngineInformation.getConnectionInformationList(),
			connectionInformation -> connectionInformation.getConnectionId());

		for (String ccrLocalClusterConnectionConfiguration :
				ccrLocalClusterConnectionConfigurations) {

			List<String> localClusterConnectionConfigurationParts =
				StringUtil.split(ccrLocalClusterConnectionConfiguration);

			if (localClusterConnectionConfigurationParts.size() != 2) {
				throw new ConfigurationModelListenerException(
					_getMessage("please-set-a-hostname-and-connection-id"),
					CrossClusterReplicationConfiguration.class, getClass(),
					properties);
			}

			if (!connectionIds.contains(
					localClusterConnectionConfigurationParts.get(1))) {

				throw new ConfigurationModelListenerException(
					_getMessage("please-set-a-valid-connection-id"),
					CrossClusterReplicationConfiguration.class, getClass(),
					properties);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CrossClusterReplicationConfigurationModelListener.class);

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

	@Reference
	private ClusterExecutor _clusterExecutor;

	@Reference
	private CrossClusterReplicationConfigurationHelper
		_crossClusterReplicationConfigurationHelper;

	@Reference
	private SearchEngineInformation _searchEngineInformation;

}