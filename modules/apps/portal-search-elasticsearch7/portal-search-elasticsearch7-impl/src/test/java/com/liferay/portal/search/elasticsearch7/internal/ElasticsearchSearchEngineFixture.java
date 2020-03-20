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

package com.liferay.portal.search.elasticsearch7.internal;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.search.SearchEngine;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch7.internal.connection.OperationMode;
import com.liferay.portal.search.elasticsearch7.internal.index.CompanyIdIndexNameBuilder;
import com.liferay.portal.search.elasticsearch7.internal.index.CompanyIndexFactory;
import com.liferay.portal.search.elasticsearch7.internal.search.engine.adapter.ElasticsearchEngineAdapterFixture;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.test.util.search.engine.SearchEngineFixture;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.SnapshotClient;

/**
 * @author Adam Brandizzi
 */
public class ElasticsearchSearchEngineFixture implements SearchEngineFixture {

	public ElasticsearchSearchEngineFixture(String tmpDir) {
		_tmpDir = tmpDir;
	}

	public ElasticsearchConnectionManager getElasticsearchConnectionManager() {
		return _elasticsearchConnectionManager;
	}

	public ElasticsearchFixture getElasticsearchFixture() {
		return _elasticsearchFixture;
	}

	public ElasticsearchSearchEngine getElasticsearchSearchEngine() {
		return _elasticsearchSearchEngine;
	}

	@Override
	public IndexNameBuilder getIndexNameBuilder() {
		return _indexNameBuilder;
	}

	@Override
	public SearchEngine getSearchEngine() {
		return getElasticsearchSearchEngine();
	}

	public SnapshotClient getSnapshotClient() {
		RestHighLevelClient restHighLevelClient =
			_elasticsearchConnectionManager.getRestHighLevelClient();

		return restHighLevelClient.snapshot();
	}

	@Override
	public void setUp() throws Exception {
		setUpElasticsearchFixture();

		setUpIndexNameBuilder();

		setUpElasticsearchConnnectionManager();

		setUpSearchEngineAdapter();

		setUpElasticsearchSearchEngine();
	}

	@Override
	public void tearDown() throws Exception {
		_elasticsearchFixture.tearDown();
	}

	protected CompanyIndexFactory createCompanyIndexFactory() {
		return new CompanyIndexFactory() {
			{
				indexNameBuilder = _indexNameBuilder;
				jsonFactory = new JSONFactoryImpl();
			}
		};
	}

	protected void setUpElasticsearchConnnectionManager() {
		_elasticsearchConnectionManager = new ElasticsearchConnectionManager();

		_elasticsearchConnectionManager.setEmbeddedElasticsearchConnection(
			_elasticsearchFixture.getEmbeddedElasticsearchConnection());

		_elasticsearchConnectionManager.setOperationMode(
			OperationMode.EMBEDDED);
	}

	protected void setUpElasticsearchFixture() throws Exception {
		_elasticsearchFixture = new ElasticsearchFixture(_tmpDir);

		_elasticsearchFixture.setUp();
	}

	protected void setUpElasticsearchSearchEngine() {
		_elasticsearchSearchEngine = new ElasticsearchSearchEngine() {
			{
				setIndexFactory(createCompanyIndexFactory());
				setIndexNameBuilder(String::valueOf);
				setElasticsearchConnectionManager(
					_elasticsearchConnectionManager);
				setSearchEngineAdapter(_searchEngineAdapter);
			}
		};
	}

	protected void setUpIndexNameBuilder() {
		_indexNameBuilder = new CompanyIdIndexNameBuilder() {
			{
				setIndexNamePrefix(null);
			}
		};
	}

	protected void setUpSearchEngineAdapter() {
		ElasticsearchEngineAdapterFixture elasticsearchEngineAdapterFixture =
			new ElasticsearchEngineAdapterFixture() {
				{
					setElasticsearchClientResolver(_elasticsearchFixture);
				}
			};

		elasticsearchEngineAdapterFixture.setUp();

		_searchEngineAdapter =
			elasticsearchEngineAdapterFixture.getSearchEngineAdapter();
	}

	private ElasticsearchConnectionManager _elasticsearchConnectionManager;
	private ElasticsearchFixture _elasticsearchFixture;
	private ElasticsearchSearchEngine _elasticsearchSearchEngine;
	private IndexNameBuilder _indexNameBuilder;
	private SearchEngineAdapter _searchEngineAdapter;
	private final String _tmpDir;

}