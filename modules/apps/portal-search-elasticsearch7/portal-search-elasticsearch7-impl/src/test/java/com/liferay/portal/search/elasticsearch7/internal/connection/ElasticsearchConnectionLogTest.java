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

package com.liferay.portal.search.elasticsearch7.internal.connection;

import com.liferay.portal.search.elasticsearch7.configuration.RESTClientLoggerLevel;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationWrapper;
import com.liferay.portal.search.elasticsearch7.internal.util.SearchLogHelper;
import com.liferay.portal.search.elasticsearch7.internal.util.SearchLogHelperImpl;
import com.liferay.portal.search.elasticsearch7.internal.util.SearchLogHelperUtil;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Adam Brandizzi
 */
public class ElasticsearchConnectionLogTest {

	@Before
	public void setUp() {
		_searchLogHelper = Mockito.spy(new SearchLogHelperImpl());

		SearchLogHelperUtil.setSearchLogHelper(_searchLogHelper);
	}

	@Test
	public void testLogLevel() throws Exception {
		ElasticsearchConfigurationWrapper
			elasticsearchConfigurationWrapperMock = Mockito.mock(
				ElasticsearchConfigurationWrapper.class);

		Mockito.when(
			elasticsearchConfigurationWrapperMock.restClientLoggerLevel()
		).thenReturn(
			RESTClientLoggerLevel.DEBUG
		);

		ElasticsearchConnectionManager elasticsearchConnectionManager =
			new ElasticsearchConnectionManager() {
				{
					elasticsearchConfigurationWrapper =
						elasticsearchConfigurationWrapperMock;
				}
			};

		elasticsearchConnectionManager.applyConfigurations();

		Mockito.verify(
			_searchLogHelper
		).setRESTClientLoggerLevel(
			RESTClientLoggerLevel.DEBUG
		);
	}

	private SearchLogHelper _searchLogHelper;

}