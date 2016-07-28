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

package com.liferay.portal.search.web.components.bar.classic.portlet;

import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.search.web.internal.request.helper.PortalOriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.params.SearchParameters;
import com.liferay.portal.search.web.internal.request.params.SearchParametersConfiguration;
import com.liferay.portal.search.web.internal.request.params.SearchParametersImpl;

import javax.portlet.PortletPreferences;

import javax.servlet.http.HttpServletRequest;

/**
 * @author André de Oliveira
 */
public class SearchBarClassicDisplayContext {

	public SearchBarClassicDisplayContext(
		HttpServletRequest request, PortletPreferences portletPreferences) {

		SearchBarClassicConfigurationImpl searchBarClassicConfigurationImpl =
			new SearchBarClassicConfigurationImpl(portletPreferences);

		_parameters = createSearchParameters(
			request, searchBarClassicConfigurationImpl);

		_parametersConfiguration = searchBarClassicConfigurationImpl;
	}

	public String getQ() {
		return _parameters.getQParameter();
	}

	public String getQParameterName() {
		return _parametersConfiguration.getQParameterName();
	}

	protected SearchParameters createSearchParameters(
		HttpServletRequest request,
		SearchParametersConfiguration searchParametersConfiguration) {

		return new SearchParametersImpl(
			new PortalOriginalHttpServletRequestSupplier(
				()->request, PortalUtil.getPortal()),
			searchParametersConfiguration);
	}

	private final SearchParameters _parameters;
	private final SearchParametersConfiguration _parametersConfiguration;

}