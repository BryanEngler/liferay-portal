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

import com.liferay.portal.kernel.search.BaseSearchEngine;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.SearchEngineHelper;
import com.liferay.portal.kernel.search.SearchEngineHelperUtil;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.search.buffer.IndexerRequest;
import com.liferay.portal.search.buffer.IndexerRequestBuffer;
import com.liferay.portal.search.configuration.IndexerRegistryConfiguration;
import com.liferay.registry.BasicRegistryImpl;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;

/**
 * @author Bryan Engler
 */
public class IndexerRequestBufferOverflowTest extends PowerMockito {

	@Before
	public void setUp() throws Exception {
		setUpPropsUtil();
		setUpRegistryUtil();
		setUpSearchEngineHelperUtil();
	}

	@Test
	public void testOverflow() throws Exception {
		IndexerRequestBuffer indexerRequestBuffer =
			IndexerRequestBuffer.create();

		DefaultIndexerRequestBufferOverflowHandler
			defaultIndexerRequestBufferOverflowHandler =
				createDefaultIndexerRequestBufferOverflowHandler();

		IndexerRequestBufferHandler indexerRequestBufferHandler =
			new IndexerRequestBufferHandler(
				defaultIndexerRequestBufferOverflowHandler,
				new IndexerRegistryConfiguration() {

					@Override
					public boolean buffered() {
						return true;
					}

					@Override
					public String bufferedExecutionMode() {
						return "DEFAULT";
					}

					@Override
					public int maxBufferSize() {
						return 5;
					}

					@Override
					public float minimumBufferAvailabilityPercentage() {
						return .9f;
					}

				});

		BufferTestIndexer bufferTestIndexer = new BufferTestIndexer(
			indexerRequestBufferHandler, indexerRequestBuffer);

		Method method = Indexer.class.getDeclaredMethod(
			"reindex", String.class, long.class);

		IndexerRequest indexerRequest1 = new IndexerRequest(
			method, bufferTestIndexer, "TestOne", 10101L);
		IndexerRequest indexerRequest2 = new IndexerRequest(
			method, bufferTestIndexer, "TestTwo", 10102L);
		IndexerRequest indexerRequest3 = new IndexerRequest(
			method, bufferTestIndexer, "TestThree", 10103L);
		IndexerRequest indexerRequest4 = new IndexerRequest(
			method, bufferTestIndexer, "TestFour", 10104L);
		IndexerRequest indexerRequest5 = new IndexerRequest(
			method, bufferTestIndexer, "TestFive", 10105L);
		IndexerRequest indexerRequest6 = new IndexerRequest(
			method, bufferTestIndexer, "TestSix", 10106L);
		IndexerRequest indexerRequest7 = new IndexerRequest(
			method, bufferTestIndexer, "TestSeven", 10107L);
		IndexerRequest indexerRequest8 = new IndexerRequest(
			method, bufferTestIndexer, "TestEight", 10108L);

		List<IndexerRequest> indexerRequests = new ArrayList<>();

		indexerRequests.add(indexerRequest1);
		indexerRequests.add(indexerRequest2);
		indexerRequests.add(indexerRequest3);
		indexerRequests.add(indexerRequest4);
		indexerRequests.add(indexerRequest5);
		indexerRequests.add(indexerRequest6);
		indexerRequests.add(indexerRequest7);
		indexerRequests.add(indexerRequest8);

		for (IndexerRequest request : indexerRequests) {
			indexerRequestBufferHandler.handleRequest(
				request, indexerRequestBuffer);
		}
	}

	protected static DefaultIndexerRequestBufferOverflowHandler
		createDefaultIndexerRequestBufferOverflowHandler() {

		return new DefaultIndexerRequestBufferOverflowHandler() {
			{
				Map<String, Object> props = new HashMap<>();

				props.put("buffered.execution.mode", "DEFAULT");

				indexerRequestBufferExecutorWatcher =
					new IndexerRequestBufferExecutorWatcher();

				indexerRequestBufferExecutorWatcher.activate(new HashMap<>());

				indexerRequestBufferExecutorWatcher.
					addIndexerRequestBufferExecutor(
						new DefaultIndexerRequestBufferExecutor(), props);
			}
		};
	}

	protected void setUpPropsUtil() {
		Props props = mock(Props.class);

		PropsUtil.setProps(props);
	}

	protected void setUpRegistryUtil() throws Exception {
		Registry registry = new BasicRegistryImpl();

		RegistryUtil.setRegistry(registry);
	}

	protected void setUpSearchEngineHelperUtil() {
		mockStatic(SearchEngineHelperUtil.class, Mockito.CALLS_REAL_METHODS);

		stub(
			method(
				SearchEngineHelperUtil.class, "getDefaultSearchEngineId")
		).toReturn(
			SearchEngineHelper.SYSTEM_ENGINE_ID
		);

		stub(
			method(
				SearchEngineHelperUtil.class, "getEntryClassNames")
		).toReturn(
			new String[0]
		);

		stub(
			method(
				SearchEngineHelperUtil.class, "getSearchEngine", String.class)
		).toReturn(
			new BaseSearchEngine()
		);
	}

}