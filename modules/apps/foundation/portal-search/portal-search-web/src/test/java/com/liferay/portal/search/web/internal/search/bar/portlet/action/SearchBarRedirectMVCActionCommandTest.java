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

package com.liferay.portal.search.web.internal.search.bar.portlet.action;

import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author André de Oliveira
 */
public class SearchBarRedirectMVCActionCommandTest {

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		setUpPortalUtil();

		searchBarRedirectMVCActionCommand =
			createSearchBarRedirectMVCActionCommand();
	}

	@Test
	public void testDestinationURLParameter() throws Exception {
		String parameterValue = RandomTestUtil.randomString();
		String url = StringPool.SLASH + RandomTestUtil.randomString();

		ActionRequestBuilder actionRequestBuilder = new ActionRequestBuilder() {
			{
				friendlyURL = url;
				destination = parameterValue;
			}
		};

		ActionRequest actionRequest = actionRequestBuilder.build();

		ActionResponse actionResponse = Mockito.mock(ActionResponse.class);

		doProcessAction(actionRequest, actionResponse);

		verifyUrl(url + "?destination=" + parameterValue);
	}

	//@Test
	public void testDoAsUserIdAndScopeParameters() throws Exception {
		String doAsUserIdParameterValue = RandomTestUtil.randomString();
		String scopeParameterValue = RandomTestUtil.randomString();
		String url = StringPool.SLASH + RandomTestUtil.randomString();

		ActionRequestBuilder actionRequestBuilder = new ActionRequestBuilder() {
			{
				doAsUserId = doAsUserIdParameterValue;
				friendlyURL = url;
				scope = scopeParameterValue;
			}
		};

		ActionRequest actionRequest = actionRequestBuilder.build();

		ActionResponse actionResponse = Mockito.mock(ActionResponse.class);

		doProcessAction(actionRequest, actionResponse);

		StringBundler sb = new StringBundler(5);

		sb.append(url);
		sb.append("?doAsUserId=");
		sb.append(doAsUserIdParameterValue);
		sb.append("&scope=");
		sb.append(scopeParameterValue);

		verifyUrl(sb.toString());
	}

	//@Test
	public void testDoAsUserIdParameterPreserved() throws Exception {
		String parameterValue = RandomTestUtil.randomString();
		String url = StringPool.SLASH + RandomTestUtil.randomString();

		ActionRequestBuilder actionRequestBuilder = new ActionRequestBuilder() {
			{
				doAsUserId = parameterValue;
				friendlyURL = url;
			}
		};

		ActionRequest actionRequest = actionRequestBuilder.build();

		ActionResponse actionResponse = Mockito.mock(ActionResponse.class);

		doProcessAction(actionRequest, actionResponse);

		verifyUrl(url + "?doAsUserId=" + parameterValue);
	}

	//@Test
	public void testPlainURL() throws Exception {
		String url = StringPool.SLASH + RandomTestUtil.randomString();

		ActionRequestBuilder actionRequestBuilder = new ActionRequestBuilder() {
			{
				friendlyURL = url;
			}
		};

		ActionRequest actionRequest = actionRequestBuilder.build();

		ActionResponse actionResponse = Mockito.mock(ActionResponse.class);

		doProcessAction(actionRequest, actionResponse);

		verifyUrl(url);
	}

	//@Test
	public void testScopeURLParameter() throws Exception {
		String parameterValue = RandomTestUtil.randomString();
		String url = StringPool.SLASH + RandomTestUtil.randomString();

		ActionRequestBuilder actionRequestBuilder = new ActionRequestBuilder() {
			{
				friendlyURL = url;
				scope = parameterValue;
			}
		};

		ActionRequest actionRequest = actionRequestBuilder.build();

		ActionResponse actionResponse = Mockito.mock(ActionResponse.class);

		doProcessAction(actionRequest, actionResponse);

		verifyUrl(url + "?scope=" + parameterValue);
	}

	//@Test
	public void testScopeURLParameterBlank() throws Exception {
		String url = StringPool.SLASH + RandomTestUtil.randomString();

		ActionRequestBuilder actionRequestBuilder = new ActionRequestBuilder() {
			{
				friendlyURL = url;
				scope = StringPool.BLANK;
			}
		};

		ActionRequest actionRequest = actionRequestBuilder.build();

		ActionResponse actionResponse = Mockito.mock(ActionResponse.class);

		doProcessAction(actionRequest, actionResponse);

		verifyUrl(url);
	}

	protected SearchBarRedirectMVCActionCommand
		createSearchBarRedirectMVCActionCommand() {

		Portal portal2 = portal;

		return new SearchBarRedirectMVCActionCommand() {
			{
				portal = portal2;
			}
		};
	}

	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		searchBarRedirectMVCActionCommand.doProcessAction(
			actionRequest, actionResponse);
	}

	protected void setUpPortalUtil() {
		Mockito.doAnswer(
			AdditionalAnswers.returnsFirstArg()
		).when(
			portal
		).escapeRedirect(
			Mockito.anyString()
		);

		Mockito.doReturn(
			Mockito.mock(HttpServletRequest.class)
		).when(
			portal
		).getHttpServletRequest(
			Mockito.any()
		);

		Mockito.doReturn(
			Mockito.mock(LiferayPortletRequest.class)
		).when(
			portal
		).getLiferayPortletRequest(
			Mockito.any()
		);

		Mockito.doAnswer(
			AdditionalAnswers.returnsFirstArg()
		).when(
			portal
		).getOriginalServletRequest(
			Mockito.any()
		);

		PortalUtil portalUtil = new PortalUtil();

		portalUtil.setPortal(portal);
	}

	protected void verifyUrl(String url) {

		Mockito.verify(
			portal
		).escapeRedirect(
			url
		);
	}

	@Mock
	protected Portal portal;

	protected SearchBarRedirectMVCActionCommand
		searchBarRedirectMVCActionCommand;

	protected static class ActionRequestBuilder {

		public ActionRequest build() {
			ActionRequest actionRequest = Mockito.mock(ActionRequest.class);

			Mockito.doReturn(
				createThemeDisplay()
			).when(
				actionRequest
			).getAttribute(
				WebKeys.THEME_DISPLAY
			);

			if (scope != null) {
				Mockito.doReturn(
					scope
				).when(
					actionRequest
				).getParameter(
					"scope"
				);
			}

			return actionRequest;
		}

		protected Layout createLayout() {
			Layout layout = Mockito.mock(Layout.class);

			Mockito.doReturn(
				friendlyURL
			).when(
				layout
			).getFriendlyURL(
				Mockito.any()
			);

			return layout;
		}

		protected ThemeDisplay createThemeDisplay() {
			ThemeDisplay themeDisplay = new ThemeDisplay();

			themeDisplay.setDoAsUserId(doAsUserId);
			themeDisplay.setLayout(createLayout());
			themeDisplay.setURLCurrent(friendlyURL);

			return themeDisplay;
		}

		protected String destination;
		protected String doAsUserId;
		protected String friendlyURL;
		protected String scope;

	}

}