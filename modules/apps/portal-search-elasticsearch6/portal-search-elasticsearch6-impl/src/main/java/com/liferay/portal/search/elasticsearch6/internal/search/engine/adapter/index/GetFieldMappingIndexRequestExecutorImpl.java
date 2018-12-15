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

package com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter.index;

import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.adapter.index.GetFieldMappingIndexRequest;
import com.liferay.portal.search.engine.adapter.index.GetFieldMappingIndexResponse;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Dylan Rebelak
 */
@Component(
	immediate = true, service = GetFieldMappingIndexRequestExecutor.class
)
public class GetFieldMappingIndexRequestExecutorImpl
	implements GetFieldMappingIndexRequestExecutor {

	@Override
	public GetFieldMappingIndexResponse execute(
		GetFieldMappingIndexRequest getFieldMappingIndexRequest) {

		GetFieldMappingsRequest getFieldMappingsRequest =
			createGetFieldMappingsRequest(getFieldMappingIndexRequest);

		GetFieldMappingsResponse getFieldMappingsResponse =
			getGetFieldMappingsResponse(getFieldMappingsRequest);

		Map
			<String,
			 Map
				 <String,
				  Map<String, GetFieldMappingsResponse.FieldMappingMetaData>>>
					mappings = getFieldMappingsResponse.mappings();

		Map<String, String> fieldMappings = new HashMap<>();

		for (String indexName : getFieldMappingIndexRequest.getIndexNames()) {
			Map
				<String,
				 Map<String, GetFieldMappingsResponse.FieldMappingMetaData>>
					map1 = mappings.get(indexName);

			Map<String, GetFieldMappingsResponse.FieldMappingMetaData> map2 =
				map1.get(getFieldMappingIndexRequest.getMappingName());

			fieldMappings.put(indexName, map2.toString());
		}

		return new GetFieldMappingIndexResponse(fieldMappings);
	}

	protected GetFieldMappingsRequest createGetFieldMappingsRequest(
		GetFieldMappingIndexRequest getFieldMappingIndexRequest) {

		GetFieldMappingsRequest getFieldMappingsRequest =
			new GetFieldMappingsRequest();

		getFieldMappingsRequest.fields(getFieldMappingIndexRequest.getFields());
		getFieldMappingsRequest.indices(
			getFieldMappingIndexRequest.getIndexNames());
		getFieldMappingsRequest.types(
			getFieldMappingIndexRequest.getMappingName());

		return getFieldMappingsRequest;
	}

	protected GetFieldMappingsResponse getGetFieldMappingsResponse(
		GetFieldMappingsRequest getFieldMappingsRequest) {

		IndicesClient indicesClient =
			elasticsearchConnectionManager.getIndicesClient();

		try {
			return indicesClient.getFieldMapping(
				getFieldMappingsRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

}