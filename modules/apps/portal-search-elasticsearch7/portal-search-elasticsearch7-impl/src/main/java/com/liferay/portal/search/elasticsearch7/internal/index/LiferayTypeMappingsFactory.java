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

package com.liferay.portal.search.elasticsearch7.internal.index;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.elasticsearch7.internal.settings.SettingsBuilder;
import com.liferay.portal.search.elasticsearch7.internal.util.LogUtil;
import com.liferay.portal.search.elasticsearch7.internal.util.ResourceUtil;
import com.liferay.portal.search.elasticsearch7.settings.TypeMappingsHelper;

import java.io.IOException;

import java.util.LinkedHashMap;
import java.util.Map;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * @author André de Oliveira
 */
public class LiferayTypeMappingsFactory implements TypeMappingsHelper {

	public LiferayTypeMappingsFactory(
		IndicesClient indicesClient, JSONFactory jsonFactory) {

		_indicesClient = indicesClient;
		_jsonFactory = jsonFactory;
	}

	@Override
	public void addTypeMappings(String indexName, String source) {
		PutMappingRequest putMappingRequest = new PutMappingRequest(indexName);

		putMappingRequest.source(
			mergeDynamicTemplates(source, indexName), XContentType.JSON);

		try {
			ActionResponse actionResponse = _indicesClient.putMapping(
				putMappingRequest, RequestOptions.DEFAULT);

			LogUtil.logActionResponse(_log, actionResponse);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public void createLiferayIndexTypeMappings(
		CreateIndexRequest createIndexRequest, String mappings) {

		createIndexRequest.mapping(mappings, XContentType.JSON);
	}

	public void createOptionalDefaultTypeMappings(String indexName) {
		String name = StringUtil.replace(
			LiferayTypeMappingsConstants.LIFERAY_TYPE_MAPPINGS_FILE_NAME,
			".json", "-optional-defaults.json");

		String optionalDefaultTypeMappings = ResourceUtil.getResourceAsString(
			getClass(), name);

		addTypeMappings(indexName, optionalDefaultTypeMappings);
	}

	public void createRequiredDefaultAnalyzers(Settings.Builder builder) {
		SettingsBuilder settingsBuilder = new SettingsBuilder(builder);

		String requiredDefaultAnalyzers = ResourceUtil.getResourceAsString(
			getClass(), IndexSettingsConstants.INDEX_SETTINGS_FILE_NAME);

		settingsBuilder.loadFromSource(requiredDefaultAnalyzers);
	}

	public void createRequiredDefaultTypeMappings(
		CreateIndexRequest createIndexRequest) {

		String requiredDefaultMappings = ResourceUtil.getResourceAsString(
			getClass(),
			LiferayTypeMappingsConstants.LIFERAY_TYPE_MAPPINGS_FILE_NAME);

		createLiferayIndexTypeMappings(
			createIndexRequest, requiredDefaultMappings);
	}

	protected JSONObject createJSONObject(String mappings) {
		try {
			return _jsonFactory.createJSONObject(mappings);
		}
		catch (JSONException jsone) {
			throw new RuntimeException(jsone);
		}
	}

	protected String getMappings(String indexName) {
		GetMappingsRequest getMappingsRequest = new GetMappingsRequest();

		getMappingsRequest.indices(indexName);

		GetMappingsResponse getMappingsResponse = null;

		try {
			getMappingsResponse = _indicesClient.getMapping(
				getMappingsRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

		Map<String, MappingMetaData> mappings = getMappingsResponse.mappings();

		MappingMetaData mappingMetaData = mappings.get(indexName);

		CompressedXContent compressedXContent = mappingMetaData.source();

		return compressedXContent.toString();
	}

	protected JSONArray merge(JSONArray jsonArray1, JSONArray jsonArray2) {
		LinkedHashMap<String, JSONObject> linkedHashMap = new LinkedHashMap<>();

		putAll(linkedHashMap, jsonArray1);

		putAll(linkedHashMap, jsonArray2);

		JSONArray jsonArray3 = _jsonFactory.createJSONArray();

		linkedHashMap.forEach((key, value) -> jsonArray3.put(value));

		return jsonArray3;
	}

	protected String mergeDynamicTemplates(String source, String indexName) {
		JSONObject sourceJSONObject = createJSONObject(source);

		JSONArray sourceDynamicTemplatesJSONArray =
			sourceJSONObject.getJSONArray("dynamic_templates");

		if (sourceDynamicTemplatesJSONArray == null) {
			return sourceJSONObject.toString();
		}

		JSONObject mappingsJSONObject = createJSONObject(
			getMappings(indexName));

		JSONArray dynamicTemplatesJSONArray = mappingsJSONObject.getJSONArray(
			"dynamic_templates");

		sourceJSONObject.put(
			"dynamic_templates",
			merge(dynamicTemplatesJSONArray, sourceDynamicTemplatesJSONArray));

		return sourceJSONObject.toString();
	}

	protected void putAll(Map<String, JSONObject> map, JSONArray jsonArray) {
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			JSONArray namesJSONArray = jsonObject.names();

			String name = (String)namesJSONArray.get(0);

			map.put(name, jsonObject);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LiferayTypeMappingsFactory.class);

	private final IndicesClient _indicesClient;
	private final JSONFactory _jsonFactory;

}