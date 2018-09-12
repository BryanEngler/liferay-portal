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

package com.liferay.portal.search.elasticsearch6.internal;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseSearchEngine;
import com.liferay.portal.kernel.search.IndexSearcher;
import com.liferay.portal.kernel.search.IndexWriter;
import com.liferay.portal.kernel.search.SearchEngine;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.PortalRunMode;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch6.internal.index.IndexFactory;
import com.liferay.portal.search.elasticsearch6.internal.index.IndexNameBuilder;
import com.liferay.portal.search.elasticsearch6.internal.util.LogUtil;

import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesRequest;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesResponse;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryRequest;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryResponse;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.SnapshotClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.metadata.RepositoryMetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.repositories.RepositoryMissingException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(
	immediate = true,
	property = {
		"search.engine.id=SYSTEM_ENGINE", "search.engine.impl=Elasticsearch"
	},
	service = {ElasticsearchSearchEngine.class, SearchEngine.class}
)
public class ElasticsearchSearchEngine extends BaseSearchEngine {

	@Override
	public synchronized String backup(long companyId, String backupName)
		throws SearchException {

		backupName = StringUtil.toLowerCase(backupName);

		validateBackupName(backupName);

		SnapshotClient snapshotClient =
			elasticsearchConnectionManager.getSnapshotClient();

		CreateSnapshotRequest createSnapshotRequest =
			new CreateSnapshotRequest(_BACKUP_REPOSITORY_NAME, backupName);

		createSnapshotRequest.waitForCompletion(true);

		try {
			createBackupRepository(snapshotClient);

			CreateSnapshotResponse createSnapshotResponse =
				snapshotClient.create(
					createSnapshotRequest, RequestOptions.DEFAULT);

			LogUtil.logActionResponse(_log, createSnapshotResponse);

			return backupName;
		}
		catch (Exception e) {
			throw new SearchException(e);
		}
	}

	@Override
	public void initialize(long companyId) {
		super.initialize(companyId);

		waitForYellowStatus();

		try {
			IndicesClient indicesClient =
				elasticsearchConnectionManager.getIndicesClient();

			indexFactory.createIndices(indicesClient, companyId);

			elasticsearchConnectionManager.registerCompanyId(companyId);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}

		waitForYellowStatus();
	}

	@Override
	public synchronized void removeBackup(long companyId, String backupName)
		throws SearchException {

		SnapshotClient snapshotClient =
			elasticsearchConnectionManager.getSnapshotClient();

		try {
			if (!hasBackupRepository(snapshotClient)) {
				return;
			}

			DeleteSnapshotRequest deleteSnapshotRequest =
				new DeleteSnapshotRequest(_BACKUP_REPOSITORY_NAME, backupName);

			DeleteSnapshotResponse deleteSnapshotResponse =
				snapshotClient.delete(
					deleteSnapshotRequest, RequestOptions.DEFAULT);

			LogUtil.logActionResponse(_log, deleteSnapshotResponse);
		}
		catch (Exception e) {
			throw new SearchException(e);
		}
	}

	@Override
	public void removeCompany(long companyId) {
		super.removeCompany(companyId);

		try {
			indexFactory.deleteIndices(
				elasticsearchConnectionManager.getIndicesClient(), companyId);

			elasticsearchConnectionManager.unregisterCompanyId(companyId);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to delete index for " + companyId, e);
			}
		}
	}

	@Override
	public synchronized void restore(long companyId, String backupName)
		throws SearchException {

		backupName = StringUtil.toLowerCase(backupName);

		validateBackupName(backupName);

		IndicesClient indicesClient =
			elasticsearchConnectionManager.getIndicesClient();

		CloseIndexRequest closeIndexRequest = new CloseIndexRequest(
			indexNameBuilder.getIndexName(companyId));

		try {
			CloseIndexResponse closeIndexResponse =
				indicesClient.close(closeIndexRequest, RequestOptions.DEFAULT);

			LogUtil.logActionResponse(_log, closeIndexResponse);
		}
		catch (Exception e) {
			throw new SearchException(e);
		}

		SnapshotClient snapshotClient =
			elasticsearchConnectionManager.getSnapshotClient();

		RestoreSnapshotRequest restoreSnapshotRequest =
			new RestoreSnapshotRequest(_BACKUP_REPOSITORY_NAME, backupName);

		restoreSnapshotRequest.indices(
			indexNameBuilder.getIndexName(companyId));

		restoreSnapshotRequest.waitForCompletion(true);

		try {
			//no high level REST api yet. coming soon ~6.5.0
			RestoreSnapshotResponse restoreSnapshotResponse =
				//snapshotClient.restore(
				//	restoreSnapshotRequest, RequestOptions.DEFAULT);
				null;

			LogUtil.logActionResponse(_log, restoreSnapshotResponse);
		}
		catch (Exception e) {
			throw new SearchException(e);
		}

		waitForYellowStatus();
	}

	@Override
	@Reference(target = "(search.engine.impl=Elasticsearch)", unbind = "-")
	public void setIndexSearcher(IndexSearcher indexSearcher) {
		super.setIndexSearcher(indexSearcher);
	}

	@Override
	@Reference(target = "(search.engine.impl=Elasticsearch)", unbind = "-")
	public void setIndexWriter(IndexWriter indexWriter) {
		super.setIndexWriter(indexWriter);
	}

	public void unsetElasticsearchConnectionManager(
		ElasticsearchConnectionManager elasticsearchConnectionManager) {

		this.elasticsearchConnectionManager = null;
	}

	public void unsetIndexFactory(IndexFactory indexFactory) {
		this.indexFactory = null;
	}

	@Activate
	protected void activate(Map<String, Object> properties) {
		setVendor(MapUtil.getString(properties, "search.engine.impl"));
	}

	protected void createBackupRepository(SnapshotClient snapshotClient)
		throws Exception {

		if (hasBackupRepository(snapshotClient)) {
			return;
		}

		PutRepositoryRequest putRepositoryRequest =
			new PutRepositoryRequest(_BACKUP_REPOSITORY_NAME);

		Settings.Builder builder = Settings.builder();

		builder.put("location", "es_backup");

		putRepositoryRequest.settings(builder);

		putRepositoryRequest.type("fs");

		PutRepositoryResponse putRepositoryResponse =
			snapshotClient.createRepository(
				putRepositoryRequest, RequestOptions.DEFAULT);

		LogUtil.logActionResponse(_log, putRepositoryResponse);
	}

	protected boolean hasBackupRepository(SnapshotClient snapshotClient)
		throws Exception {

		GetRepositoriesRequest getRepositoriesRequest =
			new GetRepositoriesRequest(new String[]{_BACKUP_REPOSITORY_NAME});

		try {
			GetRepositoriesResponse getRepositoriesResponse =
				snapshotClient.getRepository(
					getRepositoriesRequest, RequestOptions.DEFAULT);

			List<RepositoryMetaData> repositoryMetaDatas =
				getRepositoriesResponse.repositories();

			if (repositoryMetaDatas.isEmpty()) {
				return false;
			}

			return true;
		}
		catch (RepositoryMissingException rme) {
			return false;
		}
	}

	protected void validateBackupName(String backupName)
		throws SearchException {

		if (Validator.isNull(backupName)) {
			throw new SearchException(
				"Backup name must not be an empty string");
		}

		if (StringUtil.contains(backupName, StringPool.COMMA)) {
			throw new SearchException("Backup name must not contain comma");
		}

		if (StringUtil.startsWith(backupName, StringPool.DASH)) {
			throw new SearchException("Backup name must not start with dash");
		}

		if (StringUtil.contains(backupName, StringPool.POUND)) {
			throw new SearchException("Backup name must not contain pounds");
		}

		if (StringUtil.contains(backupName, StringPool.SPACE)) {
			throw new SearchException("Backup name must not contain spaces");
		}

		if (StringUtil.contains(backupName, StringPool.TAB)) {
			throw new SearchException("Backup name must not contain tabs");
		}

		for (char c : backupName.toCharArray()) {
			if (Strings.INVALID_FILENAME_CHARS.contains(c)) {
				throw new SearchException(
					"Backup name must not contain invalid file name " +
						"characters");
			}
		}
	}

	protected void waitForYellowStatus() {
		long timeout = 30 * Time.SECOND;

		if (PortalRunMode.isTestMode()) {
			timeout = Time.HOUR;
		}

		ClusterHealthResponse clusterHealthResponse =
			elasticsearchConnectionManager.getClusterHealthResponse(timeout);

		if (clusterHealthResponse.getStatus() == ClusterHealthStatus.RED) {
			throw new IllegalStateException(
				"Unable to initialize Elasticsearch cluster: " +
					clusterHealthResponse);
		}
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	@Reference
	protected IndexFactory indexFactory;

	@Reference
	protected IndexNameBuilder indexNameBuilder;

	private static final String _BACKUP_REPOSITORY_NAME = "liferay_backup";

	private static final Log _log = LogFactoryUtil.getLog(
		ElasticsearchSearchEngine.class);

}