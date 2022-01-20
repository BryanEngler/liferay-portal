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

package com.liferay.portal.search.opensearch.internal.search.engine.adapter.snapshot;

import com.liferay.portal.search.opensearch.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.engine.adapter.snapshot.CreateSnapshotRepositoryRequest;
import com.liferay.portal.search.engine.adapter.snapshot.CreateSnapshotRepositoryResponse;

import java.io.IOException;

import org.opensearch.action.admin.cluster.repositories.put.PutRepositoryRequest;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.SnapshotClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.repositories.fs.FsRepository;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(service = CreateSnapshotRepositoryRequestExecutor.class)
public class CreateSnapshotRepositoryRequestExecutorImpl
	implements CreateSnapshotRepositoryRequestExecutor {

	@Override
	public CreateSnapshotRepositoryResponse execute(
		CreateSnapshotRepositoryRequest createSnapshotRepositoryRequest) {

		PutRepositoryRequest putRepositoryRequest = createPutRepositoryRequest(
			createSnapshotRepositoryRequest);

		AcknowledgedResponse acknowledgedResponse = getAcknowledgedResponse(
			putRepositoryRequest, createSnapshotRepositoryRequest);

		return new CreateSnapshotRepositoryResponse(
			acknowledgedResponse.isAcknowledged());
	}

	protected PutRepositoryRequest createPutRepositoryRequest(
		CreateSnapshotRepositoryRequest createSnapshotRepositoryRequest) {

		PutRepositoryRequest putRepositoryRequest = new PutRepositoryRequest(
			createSnapshotRepositoryRequest.getName());

		Settings.Builder builder = Settings.builder();

		builder.put(
			FsRepository.COMPRESS_SETTING.getKey(),
			createSnapshotRepositoryRequest.isCompress());

		builder.put(
			FsRepository.LOCATION_SETTING.getKey(),
			createSnapshotRepositoryRequest.getLocation());

		putRepositoryRequest.settings(builder);

		putRepositoryRequest.type(createSnapshotRepositoryRequest.getType());
		putRepositoryRequest.verify(createSnapshotRepositoryRequest.isVerify());

		return putRepositoryRequest;
	}

	protected AcknowledgedResponse getAcknowledgedResponse(
		PutRepositoryRequest putRepositoryRequest,
		CreateSnapshotRepositoryRequest createSnapshotRepositoryRequest) {

		RestHighLevelClient restHighLevelClient =
			_elasticsearchClientResolver.getRestHighLevelClient(
				createSnapshotRepositoryRequest.getConnectionId(),
				createSnapshotRepositoryRequest.isPreferLocalCluster());

		SnapshotClient snapshotClient = restHighLevelClient.snapshot();

		try {
			return snapshotClient.createRepository(
				putRepositoryRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	@Reference(unbind = "-")
	protected void setElasticsearchClientResolver(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
	}

	private ElasticsearchClientResolver _elasticsearchClientResolver;

}