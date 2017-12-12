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

package com.liferay.portal.search.elasticsearch.internal.index;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.elasticsearch.internal.util.LogUtil;
import com.liferay.portal.search.elasticsearch.internal.util.ResourceUtil;
import com.liferay.portal.search.elasticsearch.settings.TypeMappingsHelper;

import java.io.IOException;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * @author André de Oliveira
 */
public class LiferayDocumentTypeFactory implements TypeMappingsHelper {

	public LiferayDocumentTypeFactory(IndicesAdminClient indicesAdminClient) {
		_indicesAdminClient = indicesAdminClient;
	}

	@Override
	public void addTypeMappings(String indexName, String source) {
		try {
			JSONObject originalMappings = getLiferayTypeMappings(indexName);

			JSONObject additionalMappings = JSONFactoryUtil.createJSONObject(
				source.toString());

			source = mergeMappings(originalMappings, additionalMappings);

			PutMappingRequestBuilder putMappingRequestBuilder =
				_indicesAdminClient.preparePutMapping(indexName);

			putMappingRequestBuilder.setSource(source, XContentType.JSON);
			putMappingRequestBuilder.setType(
				LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE);

			PutMappingResponse putMappingResponse =
				putMappingRequestBuilder.get();

			try {
				LogUtil.logActionResponse(_log, putMappingResponse);
			}
			catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		catch (JSONException jsone) {
			if (_log.isErrorEnabled()) {
				_log.error("Unable to merge index mappings");
			}
		}
	}

	public void createLiferayDocumentTypeMappings(
		CreateIndexRequestBuilder createIndexRequestBuilder, String mappings) {

		createIndexRequestBuilder.addMapping(
			LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE, mappings,
			XContentType.JSON);
	}

	public void createOptionalDefaultTypeMappings(String indexName) {
		String name = StringUtil.replace(
			LiferayTypeMappingsConstants.
				LIFERAY_DOCUMENT_TYPE_MAPPING_FILE_NAME,
			".json", "-optional-defaults.json");

		String optionalDefaultTypeMappings = ResourceUtil.getResourceAsString(
			getClass(), name);

		addTypeMappings(indexName, optionalDefaultTypeMappings);
	}

	public void createRequiredDefaultAnalyzers(Settings.Builder builder) {
		String requiredDefaultAnalyzers = ResourceUtil.getResourceAsString(
			getClass(), IndexSettingsConstants.INDEX_SETTINGS_FILE_NAME);

		builder.loadFromSource(requiredDefaultAnalyzers, XContentType.JSON);
	}

	public void createRequiredDefaultTypeMappings(
		CreateIndexRequestBuilder createIndexRequestBuilder) {

		String requiredDefaultMappings = ResourceUtil.getResourceAsString(
			getClass(),
			LiferayTypeMappingsConstants.
				LIFERAY_DOCUMENT_TYPE_MAPPING_FILE_NAME);

		createLiferayDocumentTypeMappings(
			createIndexRequestBuilder, requiredDefaultMappings);
	}

	protected JSONObject getLiferayTypeMappings(String indexName)
		throws JSONException {

		GetMappingsRequestBuilder getMappingsRequestBuilder =
			_indicesAdminClient.prepareGetMappings(indexName);

		getMappingsRequestBuilder.setTypes(
			LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE);

		GetMappingsResponse getMappingsResponse =
			getMappingsRequestBuilder.get();

		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>>
			map = getMappingsResponse.mappings();

		ImmutableOpenMap<String, MappingMetaData> mappings = map.get(indexName);

		MappingMetaData mappingMetaData = mappings.get(
			LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE);

		CompressedXContent compressedXContent = mappingMetaData.source();

		JSONObject mappingsJSONObject = JSONFactoryUtil.createJSONObject(
			compressedXContent.toString());

		return mappingsJSONObject.getJSONObject(
			LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE);
	}

	protected Optional<JSONObject> getTemplate(
		String templateName, JSONArray templates) {

		IntStream intStream = IntStream.range(0, templates.length());

		Stream<JSONObject> templatesStream = intStream.mapToObj(
			templates::getJSONObject);

		return templatesStream.filter(
			template -> {
				JSONArray names = template.names();

				String name = (String)names.get(0);

				return name.equals(templateName);
			})
			.findFirst();
	}

	protected String mergeMappings(
		JSONObject originalMappings, JSONObject additionalMappings) {

		JSONObject additionalMappingsDocumentType =
			additionalMappings.getJSONObject(
				LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE);

		if (additionalMappingsDocumentType != null) {
			additionalMappings = additionalMappingsDocumentType;
		}

		JSONObject originalProperties = originalMappings.getJSONObject(
			LiferayTypeMappingsConstants.PROPERTIES);

		JSONObject additionalProperties = additionalMappings.getJSONObject(
			LiferayTypeMappingsConstants.PROPERTIES);

		if (additionalProperties != null) {
			Iterator<String> keys = additionalProperties.keys();

			while (keys.hasNext()) {
				String key = keys.next();

				originalProperties.put(
					key, additionalProperties.getJSONObject(key));
			}
		}

		JSONArray originalTemplates = originalMappings.getJSONArray(
			LiferayTypeMappingsConstants.DYNAMIC_TEMPLATES);

		JSONArray additionalTemplates = additionalMappings.getJSONArray(
			LiferayTypeMappingsConstants.DYNAMIC_TEMPLATES);

		if (additionalTemplates != null) {
			JSONArray newTemplates = JSONFactoryUtil.createJSONArray();

			for (int i = 0; i < originalTemplates.length(); i++) {
				JSONObject originalTemplate = originalTemplates.getJSONObject(
					i);

				JSONArray originalTemplateNames = originalTemplate.names();

				String originalTemplateName = (String)originalTemplateNames.get(
					0);

				Optional<JSONObject> additionalTemplate = getTemplate(
					originalTemplateName, additionalTemplates);

				if (additionalTemplate.isPresent()) {
					newTemplates.put(additionalTemplate.get());
				}
				else {
					newTemplates.put(originalTemplate);
				}
			}

			for (int i = 0; i < additionalTemplates.length(); i++) {
				JSONObject additionalTemplate =
					additionalTemplates.getJSONObject(i);

				JSONArray additionalTemplateNames = additionalTemplate.names();

				String additionalTemplateName =
					(String)additionalTemplateNames.get(0);

				Optional<JSONObject> originalTemplate = getTemplate(
					additionalTemplateName, originalTemplates);

				if (!originalTemplate.isPresent()) {
					newTemplates.put(additionalTemplate);
				}
			}

			originalMappings.put(
				LiferayTypeMappingsConstants.DYNAMIC_TEMPLATES, newTemplates);
		}

		return originalMappings.toString();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LiferayDocumentTypeFactory.class);

	private final IndicesAdminClient _indicesAdminClient;

}