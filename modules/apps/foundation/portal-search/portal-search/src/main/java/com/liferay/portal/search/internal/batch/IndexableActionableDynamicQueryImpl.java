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

package com.liferay.portal.search.internal.batch;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskThreadLocal;
import com.liferay.portal.kernel.dao.orm.DefaultActionableDynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.IndexWriterHelper;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.SearchEngineHelperUtil;
import com.liferay.portal.kernel.search.background.task.ReindexStatusMessageSenderUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.batch.IndexableActionableDynamicQuery;
import com.liferay.portal.search.configuration.ReindexConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrew Betts
 * @author Bryan Engler
 */
@Component(
	configurationPid = "com.liferay.portal.search.configuration.ReindexConfiguration",
	immediate = true, service = IndexableActionableDynamicQuery.class
)
public class IndexableActionableDynamicQueryImpl
	extends DefaultActionableDynamicQuery
	implements IndexableActionableDynamicQuery {

	@Override
	public void addDocuments(Document... documents) throws PortalException {
		if (ArrayUtil.isEmpty(documents)) {
			return;
		}

		for (Document document : documents) {
			if (document != null) {
				_documents.add(document);
			}
		}

		long size = _documents.size();

		if (size >= getInterval()) {
			indexInterval();
		}
		else if ((size % _STATUS_INTERVAL) == 0) {
			sendStatusMessage(size);
		}
	}

	@Override
	public void performActions() throws PortalException {
		if (BackgroundTaskThreadLocal.hasBackgroundTask()) {
			_total = super.performCount();
		}

		try {
			super.performActions();
		}
		finally {
			_count = _total;

			sendStatusMessage();
		}
	}

	@Override
	public void setParallel(boolean parallel) {
		if (isParallel() == parallel) {
			return;
		}

		super.setParallel(parallel);

		if (parallel) {
			_documents = new ConcurrentLinkedDeque<>();
		}
	}

	@Override
	public void setSearchEngineId(String searchEngineId) {
		_searchEngineId = searchEngineId;
	}

	@Override
	protected void actionsCompleted() throws PortalException {
		if (Validator.isNotNull(_searchEngineId)) {
			_indexWriterHelper.commit(_searchEngineId, getCompanyId());
		}
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_reindexConfiguration = ConfigurableUtil.createConfigurable(
			ReindexConfiguration.class, properties);
	}

	@Override
	protected long doPerformActions(long previousPrimaryKey)
		throws PortalException {

		try {
			return super.doPerformActions(previousPrimaryKey);
		}
		finally {
			indexInterval();
		}
	}

	protected int getInterval() {
		if (super.getInterval() != Indexer.DEFAULT_INTERVAL) {
			return super.getInterval();
		}

		for (String indexingInterval :
				_reindexConfiguration.indexingIntervals()) {

			String[] intervalClassNameValuePair = StringUtil.split(
				indexingInterval, StringPool.EQUAL);

			String intervalClassName = intervalClassNameValuePair[0];
			String intervalValue = intervalClassNameValuePair[1];

			Class<?> modelClass = getModelClass();

			if (intervalClassName.equals(modelClass.getName())) {
				return Integer.valueOf(intervalValue);
			}
		}

		return Indexer.DEFAULT_INTERVAL;
	}

	protected String getSearchEngineId() {
		return _searchEngineId;
	}

	protected void indexInterval() throws PortalException {
		if ((_documents == null) || _documents.isEmpty()) {
			return;
		}

		if (Validator.isNull(_searchEngineId)) {
			_searchEngineId = SearchEngineHelperUtil.getSearchEngineId(
				_documents);
		}

		_indexWriterHelper.updateDocuments(
			_searchEngineId, getCompanyId(), new ArrayList<>(_documents),
			false);

		_count += _documents.size();

		_documents.clear();

		sendStatusMessage();
	}

	protected void sendStatusMessage() {
		sendStatusMessage(0);
	}

	protected void sendStatusMessage(long documentIntervalCount) {
		if (!BackgroundTaskThreadLocal.hasBackgroundTask()) {
			return;
		}

		Class<?> modelClass = getModelClass();

		ReindexStatusMessageSenderUtil.sendStatusMessage(
			modelClass.getName(), _count + documentIntervalCount, _total);
	}

	private static final long _STATUS_INTERVAL = 1000;

	private long _count;
	private Collection<Document> _documents = new ArrayList<>();

	@Reference
	private IndexWriterHelper _indexWriterHelper;

	private ReindexConfiguration _reindexConfiguration;
	private String _searchEngineId;
	private long _total;

}