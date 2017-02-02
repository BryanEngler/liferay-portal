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

package com.liferay.portal.search.internal.buffer;

import com.liferay.portal.kernel.search.BaseIndexer;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.search.buffer.IndexerRequest;
import com.liferay.portal.search.buffer.IndexerRequestBuffer;

import java.lang.reflect.Method;

import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

/**
 * @author Bryan Engler
 */
public class BufferTestIndexer extends BaseIndexer {

	public BufferTestIndexer(
		IndexerRequestBufferHandler indexerRequestBufferHandler,
		IndexerRequestBuffer indexerRequestBuffer) {

		_indexerRequestBufferHandler = indexerRequestBufferHandler;
		_indexerRequestBuffer = indexerRequestBuffer;
	}

	@Override
	public void doReindex(Object object) throws Exception {
	}

	@Override
	public String getClassName() {
		return BufferTestIndexer.class.getName();
	}

	@Override
	public void reindex(String className, long classPK) {
		try {
			SimpleTestIndexer simpleTestIndexer = new SimpleTestIndexer();

			Method method = Indexer.class.getDeclaredMethod(
				"reindex", String.class, long.class);

			IndexerRequest indexerRequest = new IndexerRequest(
				method, simpleTestIndexer, className, classPK);

			_indexerRequestBufferHandler.handleRequest(
				indexerRequest, _indexerRequestBuffer);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void doDelete(Object object) throws Exception {
	}

	@Override
	protected Document doGetDocument(Object object) throws Exception {
		return null;
	}

	@Override
	protected Summary doGetSummary(
			Document document, Locale locale, String snippet,
			PortletRequest portletRequest, PortletResponse portletResponse)
		throws Exception {

		return null;
	}

	@Override
	protected void doReindex(String className, long classPK) throws Exception {
	}

	@Override
	protected void doReindex(String[] ids) throws Exception {
	}

	private final IndexerRequestBuffer _indexerRequestBuffer;
	private final IndexerRequestBufferHandler _indexerRequestBufferHandler;

}