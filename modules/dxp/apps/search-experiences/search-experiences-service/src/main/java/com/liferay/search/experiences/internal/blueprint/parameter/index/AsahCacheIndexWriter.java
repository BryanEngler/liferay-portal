/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.blueprint.parameter.index;

import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentResponse;

/**
 * @author André de Oliveira
 */
public class AsahCacheIndexWriter {

	public AsahCacheIndexWriter(
		DocumentBuilderFactory documentBuilderFactory,
		SearchEngineAdapter searchEngineAdapter) {

		_documentBuilderFactory = documentBuilderFactory;
		_searchEngineAdapter = searchEngineAdapter;
	}

	public String create(AsahCacheEntry asahCacheEntry, String indexName) {
		IndexDocumentResponse indexDocumentResponse =
			_searchEngineAdapter.execute(
				new IndexDocumentRequest(
					indexName, asahCacheEntry.getDocumentId(),
					_translate(asahCacheEntry)));

		return indexDocumentResponse.getUid();
	}

	public void update(AsahCacheEntry asahCacheEntry, String indexName) {
		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			indexName, asahCacheEntry.getDocumentId(),
			_translate(asahCacheEntry));

		_searchEngineAdapter.execute(indexDocumentRequest);
	}

	private Document _translate(AsahCacheEntry asahCacheEntry) {
		DocumentBuilder documentBuilder = _documentBuilderFactory.builder();

		return documentBuilder.setValue(
			"lastSyncDate", asahCacheEntry.getLastSyncDate()
		).setValue(
			"mostViewedContents", asahCacheEntry.getMostViewedContents()
		).setValue(
			"size", asahCacheEntry.getSize()
		).build();
	}

	private final DocumentBuilderFactory _documentBuilderFactory;
	private final SearchEngineAdapter _searchEngineAdapter;

}