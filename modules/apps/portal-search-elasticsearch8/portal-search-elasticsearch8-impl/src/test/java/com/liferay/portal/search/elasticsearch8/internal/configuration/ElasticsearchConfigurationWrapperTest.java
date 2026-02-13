/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.configuration;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.Mockito;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author André de Oliveira
 */
public class ElasticsearchConfigurationWrapperTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws IOException {
		_elasticsearchConfigurationWrapper =
			new ElasticsearchConfigurationWrapper();

		Configuration configuration = Mockito.mock(Configuration.class);

		Mockito.when(
			configuration.getProperties()
		).thenReturn(
			HashMapDictionaryBuilder.<String, Object>put(
				"clusterName", "alpha"
			).put(
				"discoveryZenPingUnicastHostsPort", "9300-9400"
			).put(
				"embeddedHttpPort", 9202
			).put(
				"indexMaxResultWindow", 500
			).put(
				"operationMode", "REMOTE"
			).put(
				"restClientLoggerLevel", "ERROR"
			).put(
				"trackTotalHits", Boolean.FALSE
			).build()
		);

		ConfigurationAdmin configurationAdmin = Mockito.mock(
			ConfigurationAdmin.class);

		Mockito.when(
			configurationAdmin.getConfiguration(
				Mockito.anyString(), Mockito.anyString())
		).thenReturn(
			configuration
		);

		ReflectionTestUtil.setFieldValue(
			_elasticsearchConfigurationWrapper, "_configurationAdmin",
			configurationAdmin);
	}

	@Test
	public void testElasticsearchConfigurationWrapperWithEmptyMap()
		throws Exception {

		_elasticsearchConfigurationWrapper.activate(new HashMap<>());

		Assert.assertEquals(
			"alpha", _elasticsearchConfigurationWrapper.clusterName());
		Assert.assertEquals(
			500, _elasticsearchConfigurationWrapper.indexMaxResultWindow());
		Assert.assertEquals(
			Boolean.TRUE,
			_elasticsearchConfigurationWrapper.productionModeEnabled());
		Assert.assertEquals(
			"9202", _elasticsearchConfigurationWrapper.sidecarHttpPort());
		Assert.assertEquals(
			500, _elasticsearchConfigurationWrapper.trackTotalHitsLimit());
	}

	@Test
	public void testElasticsearchConfigurationWrapperWithPopulatedMap()
		throws Exception {

		_elasticsearchConfigurationWrapper.activate(
			HashMapBuilder.<String, Object>put(
				"clusterName", "bravo"
			).put(
				"indexMaxResultWindow", 1000
			).put(
				"productionModeEnabled", "false"
			).put(
				"sidecarHttpPort", 9203
			).put(
				"trackTotalHitsLimit", 700
			).build());

		Assert.assertEquals(
			"bravo", _elasticsearchConfigurationWrapper.clusterName());
		Assert.assertEquals(
			1000, _elasticsearchConfigurationWrapper.indexMaxResultWindow());
		Assert.assertEquals(
			Boolean.FALSE,
			_elasticsearchConfigurationWrapper.productionModeEnabled());
		Assert.assertEquals(
			"9203", _elasticsearchConfigurationWrapper.sidecarHttpPort());
		Assert.assertEquals(
			700, _elasticsearchConfigurationWrapper.trackTotalHitsLimit());
	}

	private ElasticsearchConfigurationWrapper
		_elasticsearchConfigurationWrapper;

}