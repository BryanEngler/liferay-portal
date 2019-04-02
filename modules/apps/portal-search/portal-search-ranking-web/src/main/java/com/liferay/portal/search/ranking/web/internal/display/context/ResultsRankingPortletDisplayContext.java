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

import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemList;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Html;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.legacy.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.ranking.web.internal.request.SearchRankingRequest;
import com.liferay.portal.search.ranking.web.internal.request.SearchRankingResponse;
import com.liferay.portal.search.searcher.Searcher;

import java.util.List;
import java.util.Objects;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Kevin Tan
 */
public class ResultsRankingPortletDisplayContext {

	public ResultsRankingPortletDisplayContext(
		HttpServletRequest httpServletRequest, Language language,
		Queries queries, RenderRequest renderRequest,
		RenderResponse renderResponse, Searcher searcher,
		SearchRequestBuilderFactory searchRequestBuilderFactory) {

		_httpServletRequest = httpServletRequest;
		_language = language;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;

		_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		_resultsRankingsHolder = _rankingSearch(
			queries, searcher, searchRequestBuilderFactory);
	}

	public List<DropdownItem> getActionDropdownItems() {
		return new DropdownItemList() {

			private static final long serialVersionUID = 1L;

			{
				add(
					dropdownItem -> {
						dropdownItem.putData(
							"action", "deleteResultRankingsEntries");
						dropdownItem.setIcon("times");
						dropdownItem.setLabel(
							LanguageUtil.get(_httpServletRequest, "delete"));
						dropdownItem.setQuickAction(true);
					});
			}
		};
	}

	@SuppressWarnings("deprecation")
	public String getClearResultsURL() {
		PortletURL clearResultsURL = _getPortletURL();

		clearResultsURL.setParameter("keywords", StringPool.BLANK);

		return clearResultsURL.toString();
	}

	public CreationMenu getCreationMenu() {
		return new CreationMenu() {

			private static final long serialVersionUID = 1L;

			{
				addPrimaryDropdownItem(
					dropdownItem -> {
						dropdownItem.setHref(
							_renderResponse.createRenderURL(),
							"mvcRenderCommandName", "addResultsRankingsEntry",
							"redirect",
							PortalUtil.getCurrentURL(_httpServletRequest));
						dropdownItem.setLabel(
							LanguageUtil.get(
								_httpServletRequest, "new-ranking"));
					});
			}
		};
	}

	public String getDisplayStyle() {
		if (_displayStyle != null) {
			return _displayStyle;
		}

		_displayStyle = ParamUtil.getString(
			_renderRequest, "displayStyle", "list");

		return _displayStyle;
	}

	public List<DropdownItem> getFilterItemsDropdownItems() {
		return new DropdownItemList() {

			private static final long serialVersionUID = 1L;

			{
				addGroup(
					dropdownGroupItem -> {
						dropdownGroupItem.setDropdownItems(
							_getFilterNavigationDropdownItems());
						dropdownGroupItem.setLabel(
							LanguageUtil.get(
								_httpServletRequest, "filter-by-navigation"));
					});
				addGroup(
					dropdownGroupItem -> {
						dropdownGroupItem.setDropdownItems(
							_getOrderByDropdownItems());
						dropdownGroupItem.setLabel(
							LanguageUtil.get(_httpServletRequest, "order-by"));
					});
			}
		};
	}

	public String getOrderByType() {
		if (Validator.isNotNull(_orderByType)) {
			return _orderByType;
		}

		_orderByType = ParamUtil.getString(
			_httpServletRequest, "orderByType", "asc");

		return _orderByType;
	}

	public ResultsRankingDisplayContext getResultsRankingDisplayContext(
		Document document) {

		return _resultsRankingsHolder.get(document);
	}

	public String getSearchActionURL() {
		PortletURL portletURL = _getPortletURL();

		return portletURL.toString();
	}

	public SearchContainer<Document> getSearchContainer() {
		if (_searchContainer != null) {
			return _searchContainer;
		}

		Html html = HtmlUtil.getHtml();

		String emptyResultMessage = _language.format(
			_httpServletRequest, "no-custom-results-yet",
			"<strong>" + html.escape(_getKeywords()) + "</strong>", false);

		SearchContainer<Document> searchContainer = new SearchContainer<>(
			_renderRequest, _getPortletURL(), null, emptyResultMessage);

		searchContainer.setId("resultRankingsEntries");
		searchContainer.setOrderByCol(_getOrderByCol());

		// searchContainer.setOrderByComparator(_getOrderByComparator());

		searchContainer.setOrderByType(getOrderByType());
		searchContainer.setRowChecker(
			new EmptyOnClickRowChecker(_renderResponse));

		_searchContainer = searchContainer;

		return _searchContainer;
	}

	@SuppressWarnings("deprecation")
	public String getSortingURL() {
		PortletURL sortingURL = _getPortletURL();

		sortingURL.setParameter(
			"orderByType",
			Objects.equals(getOrderByType(), "asc") ? "desc" : "asc");

		return sortingURL.toString();
	}

	public int getTotalItems() throws PortalException {
		SearchContainer<?> searchContainer = getSearchContainer();

		return searchContainer.getTotal();
	}

	public boolean isDisabledManagementBar() throws PortalException {
		if (_hasResults()) {
			return false;
		}

		if (_isSearch()) {
			return false;
		}

		return true;
	}

	public Boolean isShowCreationMenu() {
		return true;
	}

	private List<DropdownItem> _getFilterNavigationDropdownItems() {
		return new DropdownItemList() {

			private static final long serialVersionUID = 1L;

			{
				add(
					dropdownItem -> {
						dropdownItem.setActive(true);
						dropdownItem.setHref(_renderResponse.createRenderURL());
						dropdownItem.setLabel(
							LanguageUtil.get(_httpServletRequest, "all"));
					});
			}
		};
	}

	private String _getKeywords() {
		if (_keywords != null) {
			return _keywords;
		}

		_keywords = ParamUtil.getString(_httpServletRequest, "keywords");

		return _keywords;
	}

	private String _getOrderByCol() {
		if (_orderByCol != null) {
			return _orderByCol;
		}

		_orderByCol = ParamUtil.getString(
			_renderRequest, "orderByCol", "modified-date");

		return _orderByCol;
	}

	private List<DropdownItem> _getOrderByDropdownItems() {
		return new DropdownItemList() {

			private static final long serialVersionUID = 1L;

			{
				add(
					dropdownItem -> {
						dropdownItem.setActive(
							Objects.equals(_getOrderByCol(), "modified-date"));
						dropdownItem.setHref(
							_getPortletURL(), "orderByCol", "modified-date");
						dropdownItem.setLabel(
							LanguageUtil.get(
								_httpServletRequest, "modified-date"));
					});
				add(
					dropdownItem -> {
						dropdownItem.setActive(
							Objects.equals(_getOrderByCol(), "name"));
						dropdownItem.setHref(
							_getPortletURL(), "orderByCol", "name");
						dropdownItem.setLabel(
							LanguageUtil.get(_httpServletRequest, "name"));
					});
			}
		};
	}

	@SuppressWarnings("deprecation")
	private PortletURL _getPortletURL() {
		PortletURL portletURL = _renderResponse.createRenderURL();

		portletURL.setParameter("mvcPath", "/view.jsp");

		String keywords = _getKeywords();

		if (Validator.isNotNull(keywords)) {
			portletURL.setParameter("keywords", keywords);
		}

		portletURL.setParameter("displayStyle", getDisplayStyle());
		portletURL.setParameter("orderByCol", _getOrderByCol());
		portletURL.setParameter("orderByType", getOrderByType());

		return portletURL;
	}

	private boolean _hasResults() throws PortalException {
		if (getTotalItems() > 0) {
			return true;
		}

		return false;
	}

	private boolean _isSearch() {
		if (Validator.isNotNull(_getKeywords())) {
			return true;
		}

		return false;
	}

	private ResultsRankingsHolder _rankingSearch(
		Queries queries, Searcher searcher,
		SearchRequestBuilderFactory searchRequestBuilderFactory) {

		SearchRankingRequest searchRankingRequest = new SearchRankingRequest(
			_httpServletRequest, queries, getSearchContainer(), searcher,
			searchRequestBuilderFactory, _themeDisplay);

		SearchRankingResponse searchRankingResponse =
			searchRankingRequest.search();

		List<Document> documents = searchRankingResponse.getDocuments();

		if (documents.isEmpty()) {
			_searchContainer = null;

			return null;
		}

		_searchContainer.setSearch(true);
		_searchContainer.setResults(documents);
		_searchContainer.setTotal(searchRankingResponse.getTotalHits());

		final ResultsRankingsHolder resultsRankingsHolder =
			new ResultsRankingsHolder(documents.size());

		documents.forEach(
			document -> {
				ResultsRankingDisplayContext resultsRankingDisplayContext =
					new ResultsRankingDisplayContext(
						document, _themeDisplay.getLocale());

				resultsRankingsHolder.put(
					document, resultsRankingDisplayContext);
			});

		return resultsRankingsHolder;
	}

	private String _displayStyle;
	private final HttpServletRequest _httpServletRequest;
	private String _keywords;
	private final Language _language;
	private String _orderByCol;
	private String _orderByType;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ResultsRankingsHolder _resultsRankingsHolder;
	private SearchContainer<Document> _searchContainer;
	private final ThemeDisplay _themeDisplay;

}