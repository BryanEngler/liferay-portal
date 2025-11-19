/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.blueprint.parameter.contributor;

import com.liferay.analytics.settings.configuration.AnalyticsConfiguration;
import com.liferay.analytics.settings.rest.manager.AnalyticsSettingsManager;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexResponse;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.model.uid.UIDFactory;
import com.liferay.search.experiences.blueprint.parameter.SXPParameter;
import com.liferay.search.experiences.internal.blueprint.parameter.StringSXPParameter;
import com.liferay.search.experiences.internal.blueprint.parameter.index.AsahCacheEntry;
import com.liferay.search.experiences.internal.blueprint.parameter.index.AsahCacheIndexCreator;
import com.liferay.search.experiences.internal.blueprint.parameter.index.AsahCacheIndexReader;
import com.liferay.search.experiences.internal.blueprint.parameter.index.AsahCacheIndexWriter;
import com.liferay.search.experiences.internal.configuration.AsahSXPElementsConfiguration;

import java.net.HttpURLConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author Bryan Engler
 */
public class AsahSXPParameterContributor {

	public AsahSXPParameterContributor(
		AnalyticsSettingsManager analyticsSettingsManager,
		AsahSXPElementsConfiguration asahSXPElementsConfiguration,
		ClassNameLocalService classNameLocalService,
		DocumentBuilderFactory documentBuilderFactory, Http http,
		IndexNameBuilder indexNameBuilder,
		SearchEngineAdapter searchEngineAdapter, UIDFactory uidFactory,
		UserLocalService userLocalService) {

		_analyticsSettingsManager = analyticsSettingsManager;
		_asahSXPElementsConfiguration = asahSXPElementsConfiguration;
		_classNameLocalService = classNameLocalService;
		_documentBuilderFactory = documentBuilderFactory;
		_http = http;
		_indexNameBuilder = indexNameBuilder;
		_searchEngineAdapter = searchEngineAdapter;
		_uidFactory = uidFactory;
		_userLocalService = userLocalService;

		_asahCacheIndexCreator = new AsahCacheIndexCreator(
			_searchEngineAdapter);
		_asahCacheIndexReader = new AsahCacheIndexReader(_searchEngineAdapter);
		_asahCacheIndexWriter = new AsahCacheIndexWriter(
			_documentBuilderFactory, _searchEngineAdapter);
	}

	public void contribute(
		Map<String, Object> uiConfigurationValues, int mostViewedContentSize,
		int userMostViewedContentSize, SearchContext searchContext,
		Map<String, SXPParameter> sxpParameters) {

		if (!_isEnabled(
				_analyticsSettingsManager, searchContext.getCompanyId())) {

			return;
		}

		AnalyticsConfiguration analyticsConfiguration =
			_getAnalyticsConfiguration(
				_analyticsSettingsManager, searchContext.getCompanyId());

		if (analyticsConfiguration == null) {
			return;
		}

		if (mostViewedContentSize > 0) {
			_addSXPParameters(
				analyticsConfiguration, "most_viewed_content_", searchContext,
				mostViewedContentSize, sxpParameters, uiConfigurationValues, 0);
		}

		if (userMostViewedContentSize > 0) {
			long userId = searchContext.getUserId();

			if (userId == 0) {
				return;
			}

			User user = _userLocalService.fetchUser(userId);

			if (user == null) {
				return;
			}

			_addSXPParameters(
				analyticsConfiguration, "user.most_viewed_content_",
				searchContext, userMostViewedContentSize, sxpParameters,
				uiConfigurationValues, userId);
		}
	}

	private void _addSXPParameters(
		AnalyticsConfiguration analyticsConfiguration, String prefix,
		SearchContext searchContext, int size,
		Map<String, SXPParameter> sxpParameters,
		Map<String, Object> uiConfigurationValues, long userId) {

		String indexName =
			_indexNameBuilder.getIndexName(searchContext.getCompanyId()) +
				"-analytics-cloud-cache";

		IndicesExistsIndexResponse indicesExistsIndexResponse =
			_searchEngineAdapter.execute(
				new IndicesExistsIndexRequest(indexName));

		if (!indicesExistsIndexResponse.isExists()) {
			_asahCacheIndexCreator.create(indexName);
		}

		String documentId = _getDocumentId(
			searchContext.getEntryClassNames(), uiConfigurationValues, userId);

		AsahCacheEntry asahCacheEntry = _asahCacheIndexReader.fetch(
			documentId, indexName);

		if (asahCacheEntry == null) {
			asahCacheEntry = new AsahCacheEntry();

			_indexFetchedDataAndPopulateParameters(
				analyticsConfiguration, asahCacheEntry, documentId,
				_asahCacheIndexWriter::create, indexName, prefix,
				searchContext.getEntryClassNames(), size, sxpParameters,
				uiConfigurationValues, userId);

			return;
		}

		Date validCacheDate = new Date(
			System.currentTimeMillis() -
				(_asahSXPElementsConfiguration.cacheTimeToLive() * 60 * 1000));

		if (validCacheDate.after(asahCacheEntry.getLastSyncDate()) ||
			(size > asahCacheEntry.getSize())) {

			_indexFetchedDataAndPopulateParameters(
				analyticsConfiguration, asahCacheEntry, documentId,
				_asahCacheIndexWriter::update, indexName, prefix,
				searchContext.getEntryClassNames(), size, sxpParameters,
				uiConfigurationValues, userId);
		}
		else {
			try {
				_populateSXPParameters(
					JSONFactoryUtil.createJSONArray(
						asahCacheEntry.getMostViewedContents()),
					prefix, sxpParameters);
			}
			catch (JSONException jsonException) {
				_log.error(
					"Unable to create JSONArray for " +
						asahCacheEntry.getMostViewedContents(),
					jsonException);
			}
		}
	}

	private JSONArray _fetchAnalyticsCloudData(
		AnalyticsConfiguration analyticsConfiguration,
		String[] searchableAssetTypes, int size,
		Map<String, Object> uiConfigurationValues, long userId) {

		JSONArray convertedJSONArray = JSONFactoryUtil.createJSONArray();

		JSONArray responseJSONArray = JSONUtil.getValueAsJSONArray(
			_getAnalyticsCloudResponseJSONObject(
				analyticsConfiguration, StringUtil.merge(searchableAssetTypes),
				size, uiConfigurationValues, userId),
			"JSONObject/_embedded", "JSONArray/assetEventMetrics");

		if (responseJSONArray == null) {
			responseJSONArray = JSONFactoryUtil.createJSONArray();
		}

		for (int i = 0; i < size; i++) {
			if (i >= responseJSONArray.length()) {
				convertedJSONArray.put(
					JSONUtil.put(
						"className", "no.result.returned"
					).put(
						"classPK", "0"
					));
			}
			else {
				JSONObject jsonObject = responseJSONArray.getJSONObject(i);

				convertedJSONArray.put(
					JSONUtil.put(
						"className", jsonObject.getString("applicationId")
					).put(
						"classPK", jsonObject.getString("externalReferenceCode")
					));
			}
		}

		return convertedJSONArray;
	}

	private JSONObject _getAnalyticsCloudResponseJSONObject(
		AnalyticsConfiguration analyticsConfiguration,
		String searchableAssetTypes, int size,
		Map<String, Object> uiConfigurationValues, long userId) {

		try {
			Http.Options options = _getOptions(analyticsConfiguration);

			options.setLocation(
				_getURL(
					analyticsConfiguration, searchableAssetTypes, size,
					uiConfigurationValues, userId));

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				_http.URLtoString(options));

			Http.Response response = options.getResponse();

			if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
				return jsonObject;
			}

			if (_log.isDebugEnabled()) {
				_log.debug("Response code " + response.getResponseCode());
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		return JSONFactoryUtil.createJSONObject();
	}

	private AnalyticsConfiguration _getAnalyticsConfiguration(
		AnalyticsSettingsManager analyticsSettingsManager, long companyId) {

		try {
			return analyticsSettingsManager.getAnalyticsConfiguration(
				companyId);
		}
		catch (ConfigurationException configurationException) {
			_log.error(configurationException);
		}

		return null;
	}

	private String _getDocumentId(
		String[] searchableAssetTypes,
		Map<String, Object> uiConfigurationValues, long userId) {

		List<Long> classNameIds = new ArrayList<>();

		for (String searchableAssetType : searchableAssetTypes) {
			classNameIds.add(
				_classNameLocalService.getClassNameId(searchableAssetType));
		}

		Collections.sort(classNameIds);

		StringBundler sb = new StringBundler(classNameIds.size() * 2);

		for (long classNameId : classNameIds) {
			sb.append(StringPool.DASH);
			sb.append(classNameId);
		}

		String userIdString = StringPool.BLANK;

		if (userId > 0) {
			userIdString = "-U" + userId;
		}

		return StringBundler.concat(
			uiConfigurationValues.get("custom_event_name"), "-D",
			uiConfigurationValues.get("date_range"), userIdString, sb);
	}

	private Http.Options _getOptions(
			AnalyticsConfiguration analyticsConfiguration)
		throws Exception {

		Http.Options options = new Http.Options();

		options.addHeader(
			"OSB-Asah-Faro-Backend-Security-Signature",
			analyticsConfiguration.
				liferayAnalyticsFaroBackendSecuritySignature());
		options.addHeader(
			"OSB-Asah-Project-ID",
			analyticsConfiguration.liferayAnalyticsProjectId());

		return options;
	}

	private String _getURL(
		AnalyticsConfiguration analyticsConfiguration,
		String searchableAssetTypes, int size,
		Map<String, Object> uiConfigurationValues, long userId) {

		StringBundler sb = new StringBundler(12);

		sb.append(analyticsConfiguration.liferayAnalyticsFaroBackendURL());
		sb.append("/api/1.0/asset-event-metrics?page=0");

		if (!Validator.isBlank(searchableAssetTypes)) {
			sb.append("&applicationIds=");
			sb.append(StringUtil.replace(searchableAssetTypes, '#', "%23"));
		}

		sb.append("&eventId=");
		sb.append(uiConfigurationValues.get("custom_event_name"));
		sb.append("&rangeKey=");
		sb.append(uiConfigurationValues.get("date_range"));
		sb.append("&size=");
		sb.append(size);

		if (userId > 0) {
			sb.append("&userId=");
			sb.append(userId);
		}

		return sb.toString();
	}

	private void _indexFetchedDataAndPopulateParameters(
		AnalyticsConfiguration analyticsConfiguration,
		AsahCacheEntry asahCacheEntry, String documentId,
		BiConsumer<AsahCacheEntry, String> biConsumer, String indexName,
		String prefix, String[] searchableAssetTypes, int size,
		Map<String, SXPParameter> sxpParameters,
		Map<String, Object> uiConfigurationValues, long userId) {

		JSONArray jsonArray = _fetchAnalyticsCloudData(
			analyticsConfiguration, searchableAssetTypes, size,
			uiConfigurationValues, userId);

		_setAsahCacheEntryValues(asahCacheEntry, documentId, jsonArray);

		biConsumer.accept(asahCacheEntry, indexName);

		_populateSXPParameters(jsonArray, prefix, sxpParameters);
	}

	private boolean _isEnabled(
		AnalyticsSettingsManager analyticsSettingsManager, long companyId) {

		try {
			if (analyticsSettingsManager.isAnalyticsEnabled(companyId)) {
				return true;
			}
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return false;
	}

	private void _populateSXPParameters(
		JSONArray jsonArray, String prefix,
		Map<String, SXPParameter> sxpParameters) {

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			String name = prefix + (i + 1) + "_uid";

			sxpParameters.put(
				name,
				new StringSXPParameter(
					name, true,
					_uidFactory.getUID(
						jsonObject.getString("className"),
						jsonObject.getString("classPK"), 0)));
		}
	}

	private void _setAsahCacheEntryValues(
		AsahCacheEntry asahCacheEntry, String documentId, JSONArray jsonArray) {

		asahCacheEntry.setDocumentId(documentId);
		asahCacheEntry.setLastSyncDate(new Date());
		asahCacheEntry.setMostViewedContents(jsonArray.toString());
		asahCacheEntry.setSize(jsonArray.length());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AsahSXPParameterContributor.class);

	private final AnalyticsSettingsManager _analyticsSettingsManager;
	private final AsahCacheIndexCreator _asahCacheIndexCreator;
	private final AsahCacheIndexReader _asahCacheIndexReader;
	private final AsahCacheIndexWriter _asahCacheIndexWriter;
	private final AsahSXPElementsConfiguration _asahSXPElementsConfiguration;
	private final ClassNameLocalService _classNameLocalService;
	private final DocumentBuilderFactory _documentBuilderFactory;
	private final Http _http;
	private final IndexNameBuilder _indexNameBuilder;
	private final SearchEngineAdapter _searchEngineAdapter;
	private final UIDFactory _uidFactory;
	private final UserLocalService _userLocalService;

}