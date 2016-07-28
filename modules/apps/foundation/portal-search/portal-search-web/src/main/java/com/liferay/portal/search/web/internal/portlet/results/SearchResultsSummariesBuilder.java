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

package com.liferay.portal.search.web.internal.portlet.results;

import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.blogs.service.BlogsEntryLocalService;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.security.permission.ResourceActions;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.search.web.internal.display.context.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.display.context.PortletURLFactory;
import com.liferay.portal.search.web.internal.display.context.PortletURLFactoryImpl;
import com.liferay.portal.search.web.internal.display.context.SearchResultPreferences;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.request.helper.HttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.LiferayPortletHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.result.display.builder.SearchResultSummaryDisplayBuilder;
import com.liferay.portal.search.web.internal.result.display.context.SearchResultSummaryDisplayContext;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

import java.util.List;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author André de Oliveira
 */
public class SearchResultsSummariesBuilder {

	public SearchResultsSummariesBuilder(
		SearchResultsData searchResultsData, RenderRequest renderRequest,
		RenderResponse renderResponse,
		AssetEntryLocalService assetEntryLocalService,
		BlogsEntryLocalService blogsEntryLocalService, Language language,
		ResourceActions resourceActions) {

		_searchResultsData = searchResultsData;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
		_assetEntryLocalService = assetEntryLocalService;
		_blogsEntryLocalService = blogsEntryLocalService;
		_language = language;
		_resourceActions = resourceActions;
	}

	public SearchResultsSummariesHolder build() throws Exception {
		ThemeDisplay themeDisplay = getThemeDisplay(_renderRequest);

		List<Document> documents = _searchResultsData.getDocuments();

		SearchResultsSummariesHolder searchResultsSummariesHolder =
			new SearchResultsSummariesHolder(documents.size());

		for (Document document : documents) {
			SearchResultSummaryDisplayContext summary = buildSummary(
				document, themeDisplay, _renderRequest, _renderResponse,
				_searchResultsData);

			searchResultsSummariesHolder.put(document, summary);
		}

		return searchResultsSummariesHolder;
	}

	protected SearchResultSummaryDisplayContext buildSummary(
			Document document, ThemeDisplay themeDisplay,
			RenderRequest renderRequest, RenderResponse renderResponse,
			SearchResultsData searchResultsData)
		throws Exception {

		SearchResultSummaryDisplayBuilder searchResultSummaryDisplayBuilder =
			new SearchResultSummaryDisplayBuilder();

		// TODO

		boolean highlightEnabled = true;

		PortletURLFactory portletURLFactory = new PortletURLFactoryImpl(
			renderRequest, renderResponse);

		PortletURL portletURL = portletURLFactory.getPortletURL();

		HttpServletRequestSupplier httpServletRequestSupplier =
			new LiferayPortletHttpServletRequestSupplier(renderRequest);

		HttpServletRequest httpServletRequest =
			httpServletRequestSupplier.get();

		searchResultSummaryDisplayBuilder.setAssetEntryLocalService(
			_assetEntryLocalService);
		searchResultSummaryDisplayBuilder.setBlogsEntryLocalService(
			_blogsEntryLocalService);
		searchResultSummaryDisplayBuilder.setCoverImageRequested(true);
		searchResultSummaryDisplayBuilder.setCurrentURL(portletURL.toString());
		searchResultSummaryDisplayBuilder.setDocument(document);
		searchResultSummaryDisplayBuilder.setHighlightEnabled(highlightEnabled);
		searchResultSummaryDisplayBuilder.setLanguage(_language);
		searchResultSummaryDisplayBuilder.setLocale(themeDisplay.getLocale());
		searchResultSummaryDisplayBuilder.setPortletURLFactory(
			portletURLFactory);
		searchResultSummaryDisplayBuilder.setQueryTerms(
			searchResultsData.getHighlights());
		searchResultSummaryDisplayBuilder.setRenderRequest(renderRequest);
		searchResultSummaryDisplayBuilder.setRenderResponse(renderResponse);
		searchResultSummaryDisplayBuilder.setRequest(httpServletRequest);
		searchResultSummaryDisplayBuilder.setResourceActions(_resourceActions);
		searchResultSummaryDisplayBuilder.setSearchResultPreferences(
			getSearchResultPreferences());
		searchResultSummaryDisplayBuilder.setThemeDisplay(themeDisplay);

		return searchResultSummaryDisplayBuilder.build();
	}

	protected SearchResultPreferences getSearchResultPreferences() {

		// TODO Portlet Preferences

		return new SearchResultPreferences() {

			@Override
			public boolean isDisplayResultsInDocumentForm() {
				return true;
			}

			@Override
			public boolean isViewInContext() {
				return true;
			}

		};
	}

	protected ThemeDisplay getThemeDisplay(RenderRequest renderRequest) {
		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		return themeDisplaySupplier.getThemeDisplay();
	}

	private final AssetEntryLocalService _assetEntryLocalService;
	private final BlogsEntryLocalService _blogsEntryLocalService;
	private final Language _language;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ResourceActions _resourceActions;
	private final SearchResultsData _searchResultsData;

}