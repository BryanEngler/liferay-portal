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

package com.liferay.portal.search.elasticsearch7.internal.search.engine.adapter.index;

import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.engine.adapter.index.GetIndexIndexRequest;
import com.liferay.portal.search.engine.adapter.index.GetIndexIndexResponse;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(service = GetIndexIndexRequestExecutor.class)
public class GetIndexIndexRequestExecutorImpl
	implements GetIndexIndexRequestExecutor {

	@Override
	public GetIndexIndexResponse execute(
		GetIndexIndexRequest getIndexIndexRequest) {

		GetIndexRequest getIndexRequest = createGetIndexRequest(
			getIndexIndexRequest);

		GetIndexResponse getIndexResponse = getGetIndexResponse(
			getIndexRequest);

		GetIndexIndexResponse getIndexIndexResponse =
			new GetIndexIndexResponse();

		getIndexIndexResponse.setIndexMappings(
			convertMappings(getIndexResponse.getMappings()));
		getIndexIndexResponse.setIndexNames(getIndexResponse.getIndices());
		getIndexIndexResponse.setSettings(
			convertSettings(getIndexResponse.getSettings()));

		return getIndexIndexResponse;
	}

	protected Map<String, String> convertMappings(
		Map<String, MappingMetaData> indicesMappings) {

		Map<String, String> indexMappings = new HashMap<>();

		for (Map.Entry<String, MappingMetaData> entry :
				indicesMappings.entrySet()) {

			String indexName = entry.getKey();

			MappingMetaData mappingMetaData = entry.getValue();

			CompressedXContent mappingContent = mappingMetaData.source();

			indexMappings.put(indexName, mappingContent.toString());
		}

		return indexMappings;
	}

	protected Map<String, String> convertSettings(
		Map<String, Settings> indicesSettings) {

		Map<String, String> indexSettings = new HashMap<>();

		for (Map.Entry<String, Settings> entry : indicesSettings.entrySet()) {
			String indexName = entry.getKey();
			Settings settings = entry.getValue();

			indexSettings.put(indexName, settings.toString());
		}

		return indexSettings;
	}

	protected GetIndexRequest createGetIndexRequest(
		GetIndexIndexRequest getIndexIndexRequest) {

		return new GetIndexRequest(getIndexIndexRequest.getIndexNames());
	}

	protected GetIndexResponse getGetIndexResponse(
		GetIndexRequest getIndexRequest) {

		RestHighLevelClient restHighLevelClient =
			_elasticsearchClientResolver.getRestHighLevelClient();

		IndicesClient indicesClient = restHighLevelClient.indices();

		try {
			return indicesClient.get(getIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Reference(unbind = "-")
	protected void setElasticsearchClientResolver(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
	}

	private ElasticsearchClientResolver _elasticsearchClientResolver;

}