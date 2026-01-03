/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch.cross.cluster.replication.internal.background.task;

import com.liferay.petra.string.StringPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.ccr.CrossClusterReplicationHelper;
import com.liferay.portal.search.elasticsearch.cross.cluster.replication.internal.helper.CrossClusterReplicationHelperImpl;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.index.GetIndexIndexRequest;
import com.liferay.portal.search.engine.adapter.index.GetIndexIndexResponse;

import java.io.Serializable;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(
	enabled = false,
	property = "background.task.executor.class.name=com.liferay.portal.search.elasticsearch.cross.cluster.replication.internal.background.task.CrossClusterReplicationBackgroudTaskExecutor",
	service = BackgroundTaskExecutor.class
)
public class CrossClusterReplicationBackgroudTaskExecutor
	extends BaseBackgroundTaskExecutor {

	@Override
	public BackgroundTaskExecutor clone() {
		return this;
	}

	@Override
	public BackgroundTaskResult execute(BackgroundTask backgroundTask)
		throws Exception {

		String name = backgroundTask.getName();

		if (name.equals("restartReplication")) {
			BackgroundTaskResult backgroundTaskResult = _stopReplication(
				backgroundTask);

			if (backgroundTaskResult != BackgroundTaskResult.SUCCESS) {
				return new BackgroundTaskResult(
					BackgroundTaskConstants.STATUS_FAILED);
			}

			return _startReplication(backgroundTask);
		}
		else if (name.equals("stopReplication")) {
			return _stopReplication(backgroundTask);
		}
		else if (name.equals("startReplication")) {
			return _startReplication(backgroundTask);
		}

		throw new IllegalArgumentException("Unsupported name " + name);
	}

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(
		BackgroundTask backgroundTask) {

		return null;
	}

	@Reference
	protected CrossClusterReplicationHelper crossClusterReplicationHelper;

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	private void _addRemoteAndFollowIndexes(
		String[] ccrLocalClusterConnectionConfigurations,
		String[] excludedIndexes, String remoteClusterAlias,
		String remoteClusterSeedNodeTransportAddress) {

		if (_log.isInfoEnabled()) {
			_log.info("Creating follower indexes");
		}

		Log log = LogFactoryUtil.getLog(
			CrossClusterReplicationHelperImpl.class);

		if (!log.isInfoEnabled()) {
			if (_log.isInfoEnabled()) {
				_log.info(
					"For more information, enable INFO logs on " +
						CrossClusterReplicationHelperImpl.class);
			}
		}

		String[] indexNames = _getIndexNames(null);

		for (String ccrLocalClusterConnectionConfiguration :
				ccrLocalClusterConnectionConfigurations) {

			List<String> localClusterConnectionConfigurationParts =
				StringUtil.split(ccrLocalClusterConnectionConfiguration);

			String localClusterConnectionId =
				localClusterConnectionConfigurationParts.get(1);

			try {
				crossClusterReplicationHelper.addRemoteCluster(
					remoteClusterAlias, remoteClusterSeedNodeTransportAddress,
					localClusterConnectionId);

				for (String indexName : indexNames) {
					if (indexName.startsWith(StringPool.PERIOD) ||
						_isExcludedIndex(indexName, excludedIndexes)) {

						continue;
					}

					crossClusterReplicationHelper.follow(
						remoteClusterAlias, indexName,
						localClusterConnectionId);
				}
			}
			catch (RuntimeException runtimeException) {
				_log.error(
					"Unable to add remote cluster and/or follow indexes for " +
						"connection " + localClusterConnectionId);

				throw runtimeException;
			}
		}
	}

	private String[] _getIndexNames(String connectionId) {
		GetIndexIndexRequest getIndexIndexRequest = new GetIndexIndexRequest(
			StringPool.STAR);

		getIndexIndexRequest.setConnectionId(connectionId);
		getIndexIndexRequest.setPreferLocalCluster(false);

		GetIndexIndexResponse getIndexIndexResponse =
			searchEngineAdapter.execute(getIndexIndexRequest);

		return getIndexIndexResponse.getIndexNames();
	}

	private boolean _isExcludedIndex(
		String indexName, String[] excludedIndexes) {

		return ArrayUtil.contains(excludedIndexes, indexName);
	}

	private BackgroundTaskResult _startReplication(
		BackgroundTask backgroundTask) {

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		String[] ccrLocalClusterConnectionConfigurations =
			(String[])taskContextMap.get(
				"ccrLocalClusterConnectionConfigurations");
		String[] excludedIndexes = (String[])taskContextMap.get(
			"excludedIndexes");
		String remoteClusterAlias = (String)taskContextMap.get(
			"remoteClusterAlias");
		String remoteClusterSeedNodeTransportAddress =
			(String)taskContextMap.get("remoteClusterSeedNodeTransportAddress");

		try {
			_addRemoteAndFollowIndexes(
				ccrLocalClusterConnectionConfigurations, excludedIndexes,
				remoteClusterAlias, remoteClusterSeedNodeTransportAddress);
		}
		catch (RuntimeException runtimeException) {
			_log.error(runtimeException);

			return new BackgroundTaskResult(
				BackgroundTaskConstants.STATUS_FAILED);
		}

		if (_log.isInfoEnabled()) {
			_log.info("Read operations from local clusters are enabled");
		}

		return BackgroundTaskResult.SUCCESS;
	}

	private BackgroundTaskResult _stopReplication(
		BackgroundTask backgroundTask) {

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		String[] previousCcrLocalClusterConnectionConfigurations =
			(String[])taskContextMap.get(
				"previousCcrLocalClusterConnectionConfigurations");
		String[] previousExcludedIndexes = (String[])taskContextMap.get(
			"previousExcludedIndexes");
		String previousRemoteClusterAlias = (String)taskContextMap.get(
			"previousRemoteClusterAlias");

		try {
			_unfollowIndexesAndDeleteRemoteCluster(
				previousCcrLocalClusterConnectionConfigurations,
				previousExcludedIndexes, previousRemoteClusterAlias);
		}
		catch (RuntimeException runtimeException) {
			_log.error(runtimeException);

			return new BackgroundTaskResult(
				BackgroundTaskConstants.STATUS_FAILED);
		}

		return BackgroundTaskResult.SUCCESS;
	}

	private void _unfollowIndexesAndDeleteRemoteCluster(
		String[] ccrLocalClusterConnectionConfigurations,
		String[] excludedIndexes, String remoteClusterAlias) {

		if (_log.isInfoEnabled()) {
			_log.info("Deleting follower indexes");
		}

		for (String ccrLocalClusterConnectionConfiguration :
				ccrLocalClusterConnectionConfigurations) {

			List<String> localClusterConnectionConfigurationParts =
				StringUtil.split(ccrLocalClusterConnectionConfiguration);

			String localClusterConnectionId =
				localClusterConnectionConfigurationParts.get(1);

			try {
				for (String indexName :
						_getIndexNames(localClusterConnectionId)) {

					if (indexName.startsWith(StringPool.PERIOD) ||
						_isExcludedIndex(indexName, excludedIndexes)) {

						continue;
					}

					crossClusterReplicationHelper.unfollow(
						indexName, localClusterConnectionId);
				}

				crossClusterReplicationHelper.deleteRemoteCluster(
					remoteClusterAlias, localClusterConnectionId);
			}
			catch (RuntimeException runtimeException) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to unfollow indexes and/or delete remote " +
							"cluster for connection " +
								localClusterConnectionId,
						runtimeException);
				}
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CrossClusterReplicationBackgroudTaskExecutor.class);

}