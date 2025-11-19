/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.blueprint.parameter.index;

import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.GetDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.GetDocumentResponse;

/**
 * @author Bryan Engler
 */
public class AsahCacheIndexReader {

	public AsahCacheIndexReader(SearchEngineAdapter searchEngineAdapter) {
		_searchEngineAdapter = searchEngineAdapter;
	}

	public AsahCacheEntry fetch(String id, String indexName) {
		Document document = _getDocument(id, indexName);

		if (document == null) {
			return null;
		}

		AsahCacheEntry asahCacheEntry = new AsahCacheEntry();

		asahCacheEntry.setDocumentId(id);
		asahCacheEntry.setLastSyncDate(
			GetterUtil.getDate(
				document.getString("lastSyncDate"),
				DateFormatFactoryUtil.getSimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
		asahCacheEntry.setMostViewedContents(
			document.getString("mostViewedContents"));
		asahCacheEntry.setSize(document.getInteger("size"));

		return asahCacheEntry;
	}

	private Document _getDocument(String id, String indexName) {
		GetDocumentRequest getDocumentRequest = new GetDocumentRequest(
			indexName, id);

		getDocumentRequest.setFetchSource(true);
		getDocumentRequest.setPreferLocalCluster(false);

		GetDocumentResponse getDocumentResponse = _searchEngineAdapter.execute(
			getDocumentRequest);

		if (getDocumentResponse.isExists()) {
			return getDocumentResponse.getDocument();
		}

		return null;
	}

	private final SearchEngineAdapter _searchEngineAdapter;

}