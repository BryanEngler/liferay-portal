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

package com.liferay.portal.search.ranking.web.internal.display.context;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.HitsImpl;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Html;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.internal.legacy.searcher.SearchRequestBuilderFactoryImpl;
import com.liferay.portal.search.legacy.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.legacy.searcher.SearchResponseBuilderFactory;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.SearchResponseBuilder;
import com.liferay.portal.search.searcher.Searcher;

import java.util.function.Consumer;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Wade Cao
 */
@PrepareForTest(
	{ParamUtil.class, HtmlUtil.class, GetterUtil.class, PropsUtil.class}
)
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("com.liferay.portal.kernel.util.ParamUtil")
public class ResultsRankingsDisplayContextTest extends PowerMockito {

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		_themeDisplay = createThemeDisplay();

		setUpRenderRequest();
		setUpHttpServletRequest();
		setUpMockups();
		setUpSearcher();
		setUpSearchResponseBuilderFactory();
	}

	@Test
	public void testMatchAllSearch() throws Exception {
		ResultsRankingsDisplayContext resultsRankingsDisplayContext =
			new ResultsRankingsDisplayContext(
				_httpServletRequest, _language, _queries, _renderRequest,
				_renderResponse, _searcher, _searchRequestBuilderFactory);

		Assert.assertEquals(2, resultsRankingsDisplayContext.getTotalItems());
	}

	protected ThemeDisplay createThemeDisplay() throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(Mockito.mock(Company.class));
		themeDisplay.setUser(Mockito.mock(User.class));

		return themeDisplay;
	}

	protected Hits getMockHits() {
		Hits hits = new HitsImpl();
		Document[] docs = {new DocumentImpl(), new DocumentImpl()};

		hits.setDocs(docs);
		hits.setLength(docs.length);

		return hits;
	}

	protected void setUpHttpServletRequest() throws Exception {
		Mockito.doReturn(
			_themeDisplay
		).when(
			_httpServletRequest
		).getAttribute(
			WebKeys.THEME_DISPLAY
		);
	}

	protected void setUpMockups() {
		Mockito.doReturn(
			mock(PortletURL.class)
		).when(
			_renderResponse
		).createRenderURL();

		Mockito.doReturn(
			"keywords"
		).when(
			_httpServletRequest
		).getParameter(
			"keywords"
		);

		Mockito.doReturn(
			"html-escaped"
		).when(
			_html
		).escape(
			"keywords"
		);

		Mockito.doReturn(
			"message formatted"
		).when(
			_language
		).format(
			Matchers.any(HttpServletRequest.class), Matchers.anyString(),
			Matchers.anyString(), Matchers.anyBoolean()
		);

		mockStatic(ParamUtil.class);

		when(
			ParamUtil.getString(
				Matchers.any(HttpServletRequest.class), Matchers.anyString())
		).thenReturn(
			"keywords"
		);

		when(
			ParamUtil.getBoolean(
				Matchers.any(PortletRequest.class), Matchers.anyString())
		).thenReturn(
			true
		);

		mockStatic(HtmlUtil.class);

		when(
			HtmlUtil.getHtml()
		).thenReturn(
			_html
		);

		mockStatic(GetterUtil.class);

		when(
			GetterUtil.getInteger(Matchers.anyString())
		).thenReturn(
			20
		);

		mockStatic(PropsUtil.class);

		when(
			PropsUtil.get(Matchers.anyString())
		).thenReturn(
			"search.container.page.default.delta"
		);
	}

	protected void setUpRenderRequest() throws Exception {
		Mockito.doReturn(
			_themeDisplay
		).when(
			_renderRequest
		).getAttribute(
			WebKeys.THEME_DISPLAY
		);
	}

	@SuppressWarnings("unchecked")
	protected void setUpSearcher() throws Exception {
		Mockito.doAnswer(
			(Answer<?>)invocation -> {
				Hits hits = getMockHits();
				Consumer<Hits> consumer =
					(Consumer<Hits>)invocation.getArguments()[0];

				consumer.accept(hits);

				return null;
			}
		).when(
			_searchResponse
		).withHits(
			Matchers.any(Consumer.class)
		);

		Mockito.doReturn(
			_searchResponse
		).when(
			_searcher
		).search(
			Mockito.any()
		);
	}

	protected void setUpSearchResponseBuilderFactory() {
		Mockito.doReturn(
			_searchResponseBuilder
		).when(
			_searchResponseBuilderFactory
		).getSearchResponseBuilder(
			Mockito.any()
		);

		Mockito.doReturn(
			_searchResponse
		).when(
			_searchResponseBuilder
		).build();
	}

	@Mock
	private Html _html;

	@Mock
	private HttpServletRequest _httpServletRequest;

	@Mock
	private Language _language;

	@Mock
	private Queries _queries;

	@Mock
	private RenderRequest _renderRequest;

	@Mock
	private RenderResponse _renderResponse;

	@Mock
	private Searcher _searcher;

	private final SearchRequestBuilderFactory _searchRequestBuilderFactory =
		new SearchRequestBuilderFactoryImpl();

	@Mock
	private SearchResponse _searchResponse;

	@Mock
	private SearchResponseBuilder _searchResponseBuilder;

	@Mock
	private SearchResponseBuilderFactory _searchResponseBuilderFactory;

	private ThemeDisplay _themeDisplay;

}