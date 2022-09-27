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

package com.liferay.portal.search.web.internal.facet.display.context.builder;

import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.DefaultTermCollector;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.search.web.internal.facet.display.context.AssetTagsSearchFacetDisplayContext;
import com.liferay.portal.search.web.internal.facet.display.context.AssetTagsSearchFacetTermDisplayContext;
import com.liferay.portal.search.web.internal.tag.facet.configuration.TagFacetPortletInstanceConfiguration;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.portlet.RenderRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Wade Cao
 */
public class AssetTagsSearchFacetDisplayContextBuilderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		PortletDisplay portletDisplay = Mockito.mock(PortletDisplay.class);

		Mockito.doReturn(
			portletDisplay
		).when(
			_themeDisplay
		).getPortletDisplay();

		Mockito.doReturn(
			_tagFacetPortletInstanceConfiguration
		).when(
			portletDisplay
		).getPortletInstanceConfiguration(
			Mockito.any()
		);

		RenderRequest renderRequest = Mockito.mock(RenderRequest.class);

		Mockito.doReturn(
			_themeDisplay
		).when(
			renderRequest
		).getAttribute(
			Mockito.anyString()
		);

		_assetTagsSearchFacetDisplayContextBuilder =
			new AssetTagsSearchFacetDisplayContextBuilder(renderRequest);

		_assetTagsSearchFacetDisplayContextBuilder.setDisplayStyle("cloud");
	}

	@Test
	public void testBuildWithEmptyTermCollectors() throws Exception {
		_assetTagsSearchFacetDisplayContextBuilder.setFrequenciesVisible(false);

		FacetConfiguration facetConfiguration = Mockito.mock(
			FacetConfiguration.class);

		String facetLabel = RandomTestUtil.randomString();

		_setUpFacetConfigurationLabel(facetConfiguration, facetLabel);

		Facet facet = Mockito.mock(Facet.class);

		_setUpFacetFacetConfiguration(facet, facetConfiguration);
		_setUpFacetTermCollectors(facet, Collections.emptyList());

		long displayStyleGroupId = RandomTestUtil.randomLong();

		_setUpThemeDisplayScopeGroupId(displayStyleGroupId);

		_assetTagsSearchFacetDisplayContextBuilder.setFacet(facet);

		String paginationStartParameterName = RandomTestUtil.randomString();

		_assetTagsSearchFacetDisplayContextBuilder.
			setPaginationStartParameterName(paginationStartParameterName);

		String parameterName = RandomTestUtil.randomString();

		_assetTagsSearchFacetDisplayContextBuilder.setParameterName(
			parameterName);

		AssetTagsSearchFacetDisplayContext assetTagsSearchFacetDisplayContext =
			_assetTagsSearchFacetDisplayContextBuilder.build();

		Assert.assertEquals(
			displayStyleGroupId,
			assetTagsSearchFacetDisplayContext.getDisplayStyleGroupId());
		Assert.assertEquals(
			facetLabel, assetTagsSearchFacetDisplayContext.getFacetLabel());
		Assert.assertEquals(
			paginationStartParameterName,
			assetTagsSearchFacetDisplayContext.
				getPaginationStartParameterName());
		Assert.assertEquals(
			parameterName,
			assetTagsSearchFacetDisplayContext.getParameterName());
		Assert.assertEquals(
			_tagFacetPortletInstanceConfiguration,
			assetTagsSearchFacetDisplayContext.
				getTagFacetPortletInstanceConfiguration());

		List<String> parameterValues =
			assetTagsSearchFacetDisplayContext.getParameterValues();

		Assert.assertFalse(
			assetTagsSearchFacetDisplayContext.isCloudWithCount());

		Assert.assertTrue(parameterValues.isEmpty());
		Assert.assertTrue(
			assetTagsSearchFacetDisplayContext.isNothingSelected());
		Assert.assertTrue(assetTagsSearchFacetDisplayContext.isRenderNothing());

		List<AssetTagsSearchFacetTermDisplayContext>
			assetTagsSearchFacetTermDisplayContexts =
				assetTagsSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertTrue(assetTagsSearchFacetTermDisplayContexts.isEmpty());

		_assetTagsSearchFacetDisplayContextBuilder.setFrequenciesVisible(true);

		String parameterValue = RandomTestUtil.randomString();

		_assetTagsSearchFacetDisplayContextBuilder.setParameterValue(
			parameterValue);

		assetTagsSearchFacetDisplayContext =
			_assetTagsSearchFacetDisplayContextBuilder.build();

		parameterValues =
			assetTagsSearchFacetDisplayContext.getParameterValues();

		Assert.assertEquals(
			parameterValues.toString(), 1, parameterValues.size());
		Assert.assertEquals(parameterValue, parameterValues.get(0));

		Assert.assertTrue(
			assetTagsSearchFacetDisplayContext.isCloudWithCount());
		Assert.assertFalse(
			assetTagsSearchFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(
			assetTagsSearchFacetDisplayContext.isRenderNothing());

		assetTagsSearchFacetTermDisplayContexts =
			assetTagsSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			assetTagsSearchFacetTermDisplayContexts.toString(), 1,
			assetTagsSearchFacetTermDisplayContexts.size());

		AssetTagsSearchFacetTermDisplayContext
			assetTagsSearchFacetTermDisplayContext =
				assetTagsSearchFacetTermDisplayContexts.get(0);

		Assert.assertEquals(
			0, assetTagsSearchFacetTermDisplayContext.getFrequency());
		Assert.assertEquals(
			0, assetTagsSearchFacetTermDisplayContext.getPopularity());
		Assert.assertEquals(
			parameterValue, assetTagsSearchFacetTermDisplayContext.getValue());

		Assert.assertTrue(
			assetTagsSearchFacetTermDisplayContext.isFrequencyVisible());
		Assert.assertTrue(assetTagsSearchFacetTermDisplayContext.isSelected());
	}

	@Test
	public void testBuildWithTermCollectors() throws Exception {
		_assetTagsSearchFacetDisplayContextBuilder.setFrequenciesVisible(true);

		FacetConfiguration facetConfiguration = Mockito.mock(
			FacetConfiguration.class);

		String facetLabel = RandomTestUtil.randomString();

		_setUpFacetConfigurationLabel(facetConfiguration, facetLabel);

		Facet facet = Mockito.mock(Facet.class);

		_setUpFacetFacetConfiguration(facet, facetConfiguration);

		String parameterValue1 = "term1";
		String parameterValue2 = "term2";

		TermCollector termCollector1 = new DefaultTermCollector(
			parameterValue1, 2);
		TermCollector termCollector2 = new DefaultTermCollector(
			parameterValue2, 1);

		_setUpFacetTermCollectors(
			facet, Arrays.asList(termCollector1, termCollector2));

		long displayStyleGroupId = RandomTestUtil.randomLong();

		_setUpThemeDisplayScopeGroupId(displayStyleGroupId);

		_assetTagsSearchFacetDisplayContextBuilder.setFacet(facet);
		_assetTagsSearchFacetDisplayContextBuilder.setOrder("count:asc");

		String paginationStartParameterName = RandomTestUtil.randomString();

		_assetTagsSearchFacetDisplayContextBuilder.
			setPaginationStartParameterName(paginationStartParameterName);

		String parameterName = RandomTestUtil.randomString();

		_assetTagsSearchFacetDisplayContextBuilder.setParameterName(
			parameterName);

		_assetTagsSearchFacetDisplayContextBuilder.setParameterValue(
			parameterValue1);

		AssetTagsSearchFacetDisplayContext assetTagsSearchFacetDisplayContext =
			_assetTagsSearchFacetDisplayContextBuilder.build();

		Assert.assertEquals(
			displayStyleGroupId,
			assetTagsSearchFacetDisplayContext.getDisplayStyleGroupId());
		Assert.assertEquals(
			facetLabel, assetTagsSearchFacetDisplayContext.getFacetLabel());
		Assert.assertEquals(
			paginationStartParameterName,
			assetTagsSearchFacetDisplayContext.
				getPaginationStartParameterName());
		Assert.assertEquals(
			parameterName,
			assetTagsSearchFacetDisplayContext.getParameterName());
		Assert.assertEquals(
			_tagFacetPortletInstanceConfiguration,
			assetTagsSearchFacetDisplayContext.
				getTagFacetPortletInstanceConfiguration());

		List<String> parameterValues =
			assetTagsSearchFacetDisplayContext.getParameterValues();

		Assert.assertEquals(
			parameterValues.toString(), 1, parameterValues.size());
		Assert.assertEquals(parameterValue1, parameterValues.get(0));

		Assert.assertTrue(
			assetTagsSearchFacetDisplayContext.isCloudWithCount());
		Assert.assertFalse(
			assetTagsSearchFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(
			assetTagsSearchFacetDisplayContext.isRenderNothing());

		_assertAssetTagsSearchFacetTermDisplayContexts(
			assetTagsSearchFacetDisplayContext.getTermDisplayContexts(),
			new String[] {parameterValue2, parameterValue1}, new int[] {1, 6},
			new boolean[] {false, true}, termCollector2, termCollector1);

		_assetTagsSearchFacetDisplayContextBuilder.setOrder("count:dec");

		assetTagsSearchFacetDisplayContext =
			_assetTagsSearchFacetDisplayContextBuilder.build();

		_assertAssetTagsSearchFacetTermDisplayContexts(
			assetTagsSearchFacetDisplayContext.getTermDisplayContexts(),
			new String[] {parameterValue1, parameterValue2}, new int[] {6, 1},
			new boolean[] {true, false}, termCollector1, termCollector2);

		_assetTagsSearchFacetDisplayContextBuilder.setOrder("key:asc");

		assetTagsSearchFacetDisplayContext =
			_assetTagsSearchFacetDisplayContextBuilder.build();

		_assertAssetTagsSearchFacetTermDisplayContexts(
			assetTagsSearchFacetDisplayContext.getTermDisplayContexts(),
			new String[] {parameterValue1, parameterValue2}, new int[] {6, 1},
			new boolean[] {true, false}, termCollector1, termCollector2);

		_assetTagsSearchFacetDisplayContextBuilder.setOrder("key:desc");

		assetTagsSearchFacetDisplayContext =
			_assetTagsSearchFacetDisplayContextBuilder.build();

		_assertAssetTagsSearchFacetTermDisplayContexts(
			assetTagsSearchFacetDisplayContext.getTermDisplayContexts(),
			new String[] {parameterValue2, parameterValue1}, new int[] {1, 6},
			new boolean[] {false, true}, termCollector2, termCollector1);
	}

	private void _assertAssetTagsSearchFacetTermDisplayContexts(
		List<AssetTagsSearchFacetTermDisplayContext>
			assetTagsSearchFacetTermDisplayContexts,
		String[] parameterValues, int[] popularities, boolean[] selecteds,
		TermCollector... termCollectors) {

		int i = 0;

		for (AssetTagsSearchFacetTermDisplayContext
				assetTagsSearchFacetTermDisplayContext :
					assetTagsSearchFacetTermDisplayContexts) {

			Assert.assertEquals(
				termCollectors[i].getFrequency(),
				assetTagsSearchFacetTermDisplayContext.getFrequency());
			Assert.assertEquals(
				popularities[i],
				assetTagsSearchFacetTermDisplayContext.getPopularity());
			Assert.assertEquals(
				parameterValues[i],
				assetTagsSearchFacetTermDisplayContext.getValue());

			Assert.assertTrue(
				assetTagsSearchFacetTermDisplayContext.isFrequencyVisible());
			Assert.assertTrue(
				selecteds[i] ==
					assetTagsSearchFacetTermDisplayContext.isSelected());

			i++;
		}
	}

	private void _setUpFacetConfigurationLabel(
		FacetConfiguration facetConfiguration, String facetLabel) {

		Mockito.doReturn(
			facetLabel
		).when(
			facetConfiguration
		).getLabel();
	}

	private void _setUpFacetFacetConfiguration(
		Facet facet, FacetConfiguration facetConfiguration) {

		Mockito.doReturn(
			facetConfiguration
		).when(
			facet
		).getFacetConfiguration();
	}

	private void _setUpFacetTermCollectors(
		Facet facet, List<TermCollector> termCollectors) {

		if (facet == null) {
			return;
		}

		FacetCollector facetCollector = Mockito.mock(FacetCollector.class);

		Mockito.doReturn(
			facetCollector
		).when(
			facet
		).getFacetCollector();

		Mockito.doReturn(
			termCollectors
		).when(
			facetCollector
		).getTermCollectors();
	}

	private void _setUpThemeDisplayScopeGroupId(long displayStyleGroupId) {
		Mockito.doReturn(
			displayStyleGroupId
		).when(
			_themeDisplay
		).getScopeGroupId();
	}

	private AssetTagsSearchFacetDisplayContextBuilder
		_assetTagsSearchFacetDisplayContextBuilder;

	@Mock
	private TagFacetPortletInstanceConfiguration
		_tagFacetPortletInstanceConfiguration;

	@Mock
	private ThemeDisplay _themeDisplay;

}