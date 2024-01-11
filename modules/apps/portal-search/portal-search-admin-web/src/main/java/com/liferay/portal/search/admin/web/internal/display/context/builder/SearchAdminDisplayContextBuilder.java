/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.admin.web.internal.display.context.builder;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.NavigationItemList;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.admin.web.internal.display.context.SearchAdminDisplayContext;
import com.liferay.portal.search.constants.SearchAdminConstants;
import com.liferay.portal.search.index.IndexInformation;

import java.util.List;
import java.util.Objects;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author Adam Brandizzi
 */
public class SearchAdminDisplayContextBuilder {

	public SearchAdminDisplayContextBuilder(
		Language language, Portal portal, RenderRequest renderRequest,
		RenderResponse renderResponse) {

		_language = language;
		_portal = portal;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
	}

	public SearchAdminDisplayContext build() {
		SearchAdminDisplayContext searchAdminDisplayContext =
			new SearchAdminDisplayContext();

		searchAdminDisplayContext.setIndexReindexerClassNames(
			_indexReindexerClassNames);

		NavigationItemList navigationItemList = new NavigationItemList();

		ThemeDisplay themeDisplay = (ThemeDisplay)_renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionChecker permissionChecker =
			themeDisplay.getPermissionChecker();

		if (permissionChecker.isOmniadmin()) {
			_addNavigationItemList(
				navigationItemList, SearchAdminConstants.TAB_CONNECTIONS,
				getSelectedTab());
		}

		_addNavigationItemList(
			navigationItemList, SearchAdminConstants.TAB_INDEX_ACTIONS,
			SearchAdminConstants.TAB_INDEX_ACTIONS);

		if (_isIndexInformationAvailable() && permissionChecker.isOmniadmin()) {
			_addNavigationItemList(
				navigationItemList, SearchAdminConstants.TAB_FIELD_MAPPINGS,
				SearchAdminConstants.TAB_INDEX_ACTIONS);
		}

		searchAdminDisplayContext.setNavigationItemList(navigationItemList);

		searchAdminDisplayContext.setSelectedTab(
			SearchAdminConstants.TAB_INDEX_ACTIONS);

		return searchAdminDisplayContext;
	}

	public void setIndexInformation(IndexInformation indexInformation) {
		_indexInformation = indexInformation;
	}

	public void setIndexReindexerClassNames(
		List<String> indexReindexerClassNames) {

		_indexReindexerClassNames = indexReindexerClassNames;
	}

	protected String getSelectedTab() {
		String selectedTab = ParamUtil.getString(
			_renderRequest, "tabs1", SearchAdminConstants.TAB_CONNECTIONS);

		if (!Objects.equals(
				selectedTab, SearchAdminConstants.TAB_FIELD_MAPPINGS) &&
			!Objects.equals(
				selectedTab, SearchAdminConstants.TAB_INDEX_ACTIONS) &&
			!Objects.equals(
				selectedTab, SearchAdminConstants.TAB_CONNECTIONS)) {

			return SearchAdminConstants.TAB_CONNECTIONS;
		}

		if (Objects.equals(
				selectedTab, SearchAdminConstants.TAB_FIELD_MAPPINGS) &&
			!_isIndexInformationAvailable()) {

			return SearchAdminConstants.TAB_CONNECTIONS;
		}

		return selectedTab;
	}

	private void _addNavigationItemList(
		NavigationItemList navigationItemList, String label,
		String selectedTab) {

		navigationItemList.add(
			navigationItem -> {
				navigationItem.setActive(selectedTab.equals(label));
				navigationItem.setHref(
					_renderResponse.createRenderURL(), "tabs1", label);
				navigationItem.setLabel(
					_language.get(
						_portal.getHttpServletRequest(_renderRequest), label));
			});
	}

	private boolean _isIndexInformationAvailable() {
		if (_indexInformation != null) {
			return true;
		}

		return false;
	}

	private IndexInformation _indexInformation;
	private List<String> _indexReindexerClassNames;
	private final Language _language;
	private final Portal _portal;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;

}