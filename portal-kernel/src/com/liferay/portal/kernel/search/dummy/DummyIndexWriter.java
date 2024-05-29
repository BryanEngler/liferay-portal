/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.search.dummy;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.IndexWriter;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;

import java.util.Collection;

/**
 * @author Marcellus Tavares
 * @author Brian Wing Shun Chan
 */
public class DummyIndexWriter implements IndexWriter {

	@Override
	public void addDocument(SearchContext searchContext, Document document) {
	}

	@Override
	public void addDocuments(
		SearchContext searchContext, Collection<Document> documents) {
	}

	@Override
	public void clearQuerySuggestionDictionaryIndexes(
		SearchContext searchContext) {
	}

	@Override
	public void clearSpellCheckerDictionaryIndexes(
		SearchContext searchContext) {
	}

	@Override
	public void commit(SearchContext searchContext) {
	}

	@Override
	public void commit(long companyId) {
	}

	@Override
	public void deleteDocument(SearchContext searchContext, String uid) {
	}

	@Override
	public void deleteDocument(
		long companyId, boolean commitImmediately, String uid) {
	}

	@Override
	public void deleteDocuments(
		SearchContext searchContext, Collection<String> uids) {
	}

	@Override
	public void deleteDocuments(
		long companyId, boolean commitImmediately, Collection<String> uids) {
	}

	@Override
	public void deleteEntityDocuments(
		SearchContext searchContext, String className) {
	}

	@Override
	public void deleteEntityDocuments(
		long companyId, boolean commitImmediately, String className) {
	}

	@Override
	public void indexDocument(
			long companyId, boolean commitImmediately, Document document)
		throws SearchException {
	}

	@Override
	public void indexDocuments(
			long companyId, boolean commitImmediately,
			Collection<Document> documents)
		throws SearchException {
	}

	@Override
	public void indexKeyword(
		SearchContext searchContext, float weight, String keywordType) {
	}

	@Override
	public void indexQuerySuggestionDictionaries(SearchContext searchContext) {
	}

	@Override
	public void indexQuerySuggestionDictionary(SearchContext searchContext) {
	}

	@Override
	public void indexSpellCheckerDictionaries(SearchContext searchContext) {
	}

	@Override
	public void indexSpellCheckerDictionary(SearchContext searchContext) {
	}

	@Override
	public void partiallyUpdateDocument(
		SearchContext searchContext, Document document) {
	}

	@Override
	public void partiallyUpdateDocument(
		long companyId, boolean commitImmediately, Document document) {
	}

	@Override
	public void partiallyUpdateDocuments(
		SearchContext searchContext, Collection<Document> documents) {
	}

	@Override
	public void partiallyUpdateDocuments(
		long companyId, boolean commitImmediately,
		Collection<Document> documents) {
	}

	@Override
	public void removeFieldsFromDocument(
			long companyId, boolean commitImmediately, Document document,
			String... fields)
		throws SearchException {
	}

	@Override
	public void removeFieldsFromDocuments(
			long companyId, boolean commitImmediately,
			Collection<Document> documents, String... fields)
		throws SearchException {
	}

	@Override
	public void updateDocument(SearchContext searchContext, Document document) {
	}

	@Override
	public void updateDocuments(
		SearchContext searchContext, Collection<Document> documents) {
	}

}