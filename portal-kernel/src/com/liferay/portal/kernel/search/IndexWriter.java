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
	 *             #indexDocument(long, boolean, Document)}
	 */
	@Deprecated
	public void addDocument(SearchContext searchContext, Document document)
		throws SearchException;

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocuments(long, boolean, Collection)}
	 */
	@Deprecated
	public void addDocuments(
			SearchContext searchContext, Collection<Document> documents)
		throws SearchException;

	public void commit(long companyId);

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link #commit(long)}
	 */
	@Deprecated
	public void commit(SearchContext searchContext) throws SearchException;

	public void deleteDocument(
		long companyId, boolean commitImmediately, String uid);

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #deleteDocument(long, boolean, String)}
	 */
	@Deprecated
	public void deleteDocument(SearchContext searchContext, String uid)
		throws SearchException;

	public void deleteDocuments(
		long companyId, boolean commitImmediately, Collection<String> uids);

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #deleteDocuments(long, boolean, Collection)}
	 */
	@Deprecated
	public void deleteDocuments(
			SearchContext searchContext, Collection<String> uids)
		throws SearchException;

	public void deleteEntityDocuments(
		long companyId, boolean commitImmediately, String className);

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #deleteEntityDocuments(long, boolean, String)}
	 */
	@Deprecated
	public void deleteEntityDocuments(
			SearchContext searchContext, String className)
		throws SearchException;

	public void indexDocument(
			long companyId, boolean commitImmediately, Document document)
		throws SearchException;

	public void indexDocuments(
			long companyId, boolean commitImmediately,
			Collection<Document> documents)
		throws SearchException;

	public void partiallyUpdateDocument(
		long companyId, boolean commitImmediately, Document document);

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #partiallyUpdateDocument(long, boolean, Document)}
	 */
	@Deprecated
	public void partiallyUpdateDocument(
			SearchContext searchContext, Document document)
		throws SearchException;

	public void partiallyUpdateDocuments(
		long companyId, boolean commitImmediately,
		Collection<Document> documents);

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #partiallyUpdateDocuments(long, boolean, Collection)}
	 */
	@Deprecated
	public void partiallyUpdateDocuments(
			SearchContext searchContext, Collection<Document> documents)
		throws SearchException;

	public void removeFieldsFromDocument(
			long companyId, boolean commitImmediately, Document document,
			String... fields)
		throws SearchException;

	public void removeFieldsFromDocuments(
			long companyId, boolean commitImmediately,
			Collection<Document> documents, String... fields)
		throws SearchException;

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocument(long, boolean, Document)}
	 */
	@Deprecated
	public void updateDocument(SearchContext searchContext, Document document)
		throws SearchException;

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocuments(long, boolean, Collection)}
	 */
	@Deprecated
	public void updateDocuments(
			SearchContext searchContext, Collection<Document> documents)
		throws SearchException;

}