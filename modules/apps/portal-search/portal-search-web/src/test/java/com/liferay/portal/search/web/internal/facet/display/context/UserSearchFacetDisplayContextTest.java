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

package com.liferay.portal.search.web.internal.facet.display.context;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.web.internal.facet.display.builder.UserSearchFacetDisplayBuilder;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Lino Alves
 */
public class UserSearchFacetDisplayContextTest {

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		Mockito.doReturn(
			_facetCollector
		).when(
			_facet
		).getFacetCollector();
	}

	@Test
	public void testEmptySearchResults() throws Exception {
		String paramValue = "";

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(paramValue);

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			userSearchFacetTermDisplayContexts.toString(), 0,
			userSearchFacetTermDisplayContexts.size());

		Assert.assertEquals(
			paramValue, userSearchFacetDisplayContext.getParamValue());
		Assert.assertTrue(userSearchFacetDisplayContext.isNothingSelected());
		Assert.assertTrue(userSearchFacetDisplayContext.isRenderNothing());
	}

	@Test
	public void testEmptySearchResultsWithPreviousSelection() throws Exception {
		String userId = RandomTestUtil.randomString();

		String paramValue = userId;

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(paramValue);

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			userSearchFacetTermDisplayContexts.toString(), 1,
			userSearchFacetTermDisplayContexts.size());

		UserSearchFacetTermDisplayContext userSearchFacetTermDisplayContext =
			userSearchFacetTermDisplayContexts.get(0);

		Assert.assertEquals(
			_USER_DISPLAY_NAME,
			userSearchFacetTermDisplayContext.getDisplayName());
		Assert.assertEquals(
			0, userSearchFacetTermDisplayContext.getFrequency());
		Assert.assertEquals(
			userId, userSearchFacetTermDisplayContext.getUserId());
		Assert.assertTrue(userSearchFacetTermDisplayContext.isSelected());
		Assert.assertTrue(
			userSearchFacetTermDisplayContext.isFrequencyVisible());

		Assert.assertEquals(
			paramValue, userSearchFacetDisplayContext.getParamValue());
		Assert.assertFalse(userSearchFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(userSearchFacetDisplayContext.isRenderNothing());
	}

	@Test
	public void testOneTerm() throws Exception {
		String userId = RandomTestUtil.randomString();

		int count = RandomTestUtil.randomInt();

		setUpOneTermCollector(userId, count);

		String paramValue = StringPool.BLANK;

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(paramValue);

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			userSearchFacetTermDisplayContexts.toString(), 1,
			userSearchFacetTermDisplayContexts.size());

		UserSearchFacetTermDisplayContext userSearchFacetTermDisplayContext =
			userSearchFacetTermDisplayContexts.get(0);

		Assert.assertEquals(
			_USER_DISPLAY_NAME,
			userSearchFacetTermDisplayContext.getDisplayName());
		Assert.assertEquals(
			count, userSearchFacetTermDisplayContext.getFrequency());
		Assert.assertEquals(
			userId, userSearchFacetTermDisplayContext.getUserId());
		Assert.assertFalse(userSearchFacetTermDisplayContext.isSelected());
		Assert.assertTrue(
			userSearchFacetTermDisplayContext.isFrequencyVisible());

		Assert.assertEquals(
			paramValue, userSearchFacetDisplayContext.getParamValue());
		Assert.assertTrue(userSearchFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(userSearchFacetDisplayContext.isRenderNothing());
	}

	@Test
	public void testOneTermWithPreviousSelection() throws Exception {
		String userId = RandomTestUtil.randomString();

		int count = RandomTestUtil.randomInt();

		setUpOneTermCollector(userId, count);

		String paramValue = userId;

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(paramValue);

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			userSearchFacetTermDisplayContexts.toString(), 1,
			userSearchFacetTermDisplayContexts.size());

		UserSearchFacetTermDisplayContext userSearchFacetTermDisplayContext =
			userSearchFacetTermDisplayContexts.get(0);

		Assert.assertEquals(
			_USER_DISPLAY_NAME,
			userSearchFacetTermDisplayContext.getDisplayName());
		Assert.assertEquals(
			count, userSearchFacetTermDisplayContext.getFrequency());
		Assert.assertEquals(
			userId, userSearchFacetTermDisplayContext.getUserId());
		Assert.assertTrue(userSearchFacetTermDisplayContext.isSelected());
		Assert.assertTrue(
			userSearchFacetTermDisplayContext.isFrequencyVisible());

		Assert.assertEquals(
			paramValue, userSearchFacetDisplayContext.getParamValue());
		Assert.assertFalse(userSearchFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(userSearchFacetDisplayContext.isRenderNothing());
	}

	protected UserSearchFacetDisplayContext createDisplayContext(
			String paramValue)
		throws Exception {

		UserSearchFacetDisplayBuilder userSearchFacetDisplayBuilder =
			new UserSearchFacetDisplayBuilder();

		userSearchFacetDisplayBuilder.setFacet(_facet);
		userSearchFacetDisplayBuilder.setParamValue(paramValue);
		userSearchFacetDisplayBuilder.setUserLocalService(
			createUserLocalService());
		userSearchFacetDisplayBuilder.setFrequenciesVisible(true);
		userSearchFacetDisplayBuilder.setFrequencyThreshold(0);
		userSearchFacetDisplayBuilder.setMaxTerms(0);

		return userSearchFacetDisplayBuilder.build();
	}

	protected TermCollector createTermCollector(String userId, int count) {
		TermCollector termCollector = Mockito.mock(TermCollector.class);

		Mockito.doReturn(
			count
		).when(
			termCollector
		).getFrequency();

		Mockito.doReturn(
			userId
		).when(
			termCollector
		).getTerm();

		return termCollector;
	}

	protected UserLocalService createUserLocalService() throws Exception {
		User user = Mockito.mock(User.class);

		Mockito.doReturn(
			_USER_DISPLAY_NAME
		).when(
			user
		).getFullName();

		UserLocalService userLocalService = Mockito.mock(
			UserLocalService.class);

		Mockito.doReturn(
			user
		).when(
			userLocalService
		).fetchUser(
			Mockito.anyLong()
		);

		return userLocalService;
	}

	protected void setUpOneTermCollector(String userId, int count) {
		Mockito.doReturn(
			Collections.singletonList(createTermCollector(userId, count))
		).when(
			_facetCollector
		).getTermCollectors();
	}

	private static final String _USER_DISPLAY_NAME = "user display name";

	@Mock
	private Facet _facet;

	@Mock
	private FacetCollector _facetCollector;

}