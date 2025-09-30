/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.search.engine.adapter.snapshot;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.snapshot.CreateRepositoryRequest;
import co.elastic.clients.elasticsearch.snapshot.CreateRepositoryResponse;
import co.elastic.clients.elasticsearch.snapshot.ElasticsearchSnapshotClient;

import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.engine.adapter.snapshot.CreateSnapshotRepositoryRequest;
import com.liferay.portal.search.engine.adapter.snapshot.CreateSnapshotRepositoryResponse;

import java.io.IOException;

/**
 * @author Michael C. Han
 */
public class CreateSnapshotRepositoryRequestExecutor {

	public CreateSnapshotRepositoryRequestExecutor(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
	}

	public CreateSnapshotRepositoryResponse execute(
		CreateSnapshotRepositoryRequest createSnapshotRepositoryRequest) {

		CreateRepositoryResponse createRepositoryResponse =
			getCreateRepositoryResponse(
				createCreateRepositoryRequest(createSnapshotRepositoryRequest),
				createSnapshotRepositoryRequest);

		return new CreateSnapshotRepositoryResponse(
			createRepositoryResponse.acknowledged());
	}

	protected CreateRepositoryRequest createCreateRepositoryRequest(
		CreateSnapshotRepositoryRequest createSnapshotRepositoryRequest) {

		return CreateRepositoryRequest.of(
			createRepositoryRequest -> createRepositoryRequest.name(
				createSnapshotRepositoryRequest.getName()
			).settings(
				settings -> settings.compress(
					createSnapshotRepositoryRequest.isCompress()
				).location(
					createSnapshotRepositoryRequest.getLocation()
				)
			).type(
				createSnapshotRepositoryRequest.getType()
			).verify(
				createSnapshotRepositoryRequest.isVerify()
			));
	}

	protected CreateRepositoryResponse getCreateRepositoryResponse(
		CreateRepositoryRequest createRepositoryRequest,
		CreateSnapshotRepositoryRequest createSnapshotRepositoryRequest) {

		ElasticsearchClient elasticsearchClient =
			_elasticsearchClientResolver.getElasticsearchClient(
				createSnapshotRepositoryRequest.getConnectionId(),
				createSnapshotRepositoryRequest.isPreferLocalCluster());

		ElasticsearchSnapshotClient elasticsearchSnapshotClient =
			elasticsearchClient.snapshot();

		try {
			return elasticsearchSnapshotClient.createRepository(
				createRepositoryRequest);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	private final ElasticsearchClientResolver _elasticsearchClientResolver;

}