/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.blueprint.parameter.index;

import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.index.CreateIndexRequest;

/**
 * @author Bryan Engler
 */
public class AsahCacheIndexCreator {

	public AsahCacheIndexCreator(SearchEngineAdapter searchEngineAdapter) {
		_searchEngineAdapter = searchEngineAdapter;
	}

	public void create(String indexName) {
		CreateIndexRequest createIndexRequest = new CreateIndexRequest(
			indexName);

		createIndexRequest.setMappings(_readJSON(_INDEX_MAPPINGS_FILE_NAME));
		createIndexRequest.setSettings(_readJSON(_INDEX_SETTINGS_FILE_NAME));

		_searchEngineAdapter.execute(createIndexRequest);
	}

	private String _readJSON(String fileName) {
		try {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				StringUtil.read(
					AsahCacheIndexCreator.class,
					"/META-INF/search/" + fileName));

			return jsonObject.toString();
		}
		catch (JSONException jsonException) {
			_log.error(jsonException);
		}

		return null;
	}

	private static final String _INDEX_MAPPINGS_FILE_NAME =
		"analytics-cloud-cache-mappings.json";

	private static final String _INDEX_SETTINGS_FILE_NAME =
		"analytics-cloud-cache-settings.json";

	private static final Log _log = LogFactoryUtil.getLog(
		AsahCacheIndexCreator.class);

	private final SearchEngineAdapter _searchEngineAdapter;

}