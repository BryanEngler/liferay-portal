/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.search;

import com.liferay.portal.kernel.search.suggest.SpellCheckIndexWriter;

import java.util.Collection;

/**
 * @author Bruno Farache
 */
public interface IndexWriter extends SpellCheckIndexWriter {

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocument(SearchContext, Document)}
	 */
	@Deprecated
	public void addDocument(SearchContext searchContext, Document document)
		throws SearchException;

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocuments(SearchContext, Collection)}
	 */
	@Deprecated
	public void addDocuments(
			SearchContext searchContext, Collection<Document> documents)
		throws SearchException;

	public void commit(SearchContext searchContext) throws SearchException;

	public void deleteDocument(SearchContext searchContext, String uid)
		throws SearchException;

	public void deleteDocuments(
			SearchContext searchContext, Collection<String> uids)
		throws SearchException;

	public void deleteEntityDocuments(
			SearchContext searchContext, String className)
		throws SearchException;

	public void indexDocument(SearchContext searchContext, Document document)
		throws SearchException;

	public void indexDocuments(
			SearchContext searchContext, Collection<Document> documents)
		throws SearchException;

	public void partiallyUpdateDocument(
			SearchContext searchContext, Document document)
		throws SearchException;

	public void partiallyUpdateDocuments(
			SearchContext searchContext, Collection<Document> documents)
		throws SearchException;

	public void removeFieldsFromDocument(
			SearchContext searchContext, Document document, String... fields)
		throws SearchException;

	public void removeFieldsFromDocuments(
			SearchContext searchContext, Collection<Document> documents,
			String... fields)
		throws SearchException;

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocument(SearchContext, Document)}
	 */
	@Deprecated
	public void updateDocument(SearchContext searchContext, Document document)
		throws SearchException;

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocuments(SearchContext, Collection)}
	 */
	@Deprecated
	public void updateDocuments(
			SearchContext searchContext, Collection<Document> documents)
		throws SearchException;

}