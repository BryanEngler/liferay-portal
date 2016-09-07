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

package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.web.constants.SearchPortletParameterNames;
import com.liferay.portal.search.web.internal.display.context.SearchScope;
import com.liferay.portal.search.web.internal.display.context.SearchScopePreference;
import com.liferay.portal.search.web.internal.display.context.SearchScopePreferenceSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchScopeSupplier;

import javax.portlet.RenderRequest;

/**
 * @author André de Oliveira
 */
public class SearchPortletSearchScopeSupplier implements SearchScopeSupplier {

	public SearchPortletSearchScopeSupplier(
		RenderRequest renderRequest,
		SearchScopePreferenceSupplier searchScopePreferenceSupplier) {

		_renderRequest = renderRequest;
		_searchScopePreferenceSupplier = searchScopePreferenceSupplier;
	}

	@Override
	public SearchScope getSearchScope() {
		String scopeString = ParamUtil.getString(
			_renderRequest, SearchPortletParameterNames.SCOPE);

		if (Validator.isNotNull(scopeString)) {
			return SearchScope.getSearchScope(scopeString);
		}

		SearchScopePreference searchScopePreference =
			_searchScopePreferenceSupplier.getSearchScopePreference();

		SearchScope searchScope = searchScopePreference.getSearchScope();

		if (searchScope == null) {
			throw new IllegalArgumentException(
				"Scope parameter is empty and no default is set in " +
					"preferences");
		}

		return searchScope;
	}

	private final RenderRequest _renderRequest;
	private final SearchScopePreferenceSupplier _searchScopePreferenceSupplier;

}