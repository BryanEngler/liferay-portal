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

package com.liferay.portal.search.web.portlet.shared.search;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.search.web.search.request.SearchSettings;

import java.util.Optional;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;

/**
 * @author André de Oliveira
 */
@ProviderType
public interface PortletSharedSearchSettings extends SearchSettings {

	public Optional<String> getParameter(String name);

	public default Optional<String> getParameter71(String name) {
		return getParameter(name);
	}

	public Optional<String[]> getParameterValues(String name);

	public default Optional<String[]> getParameterValues71(String name) {
		return getParameterValues(name);
	}

	public String getPortletId();

	public Optional<PortletPreferences> getPortletPreferences();

	public default Optional<PortletPreferences> getPortletPreferences71() {
		return getPortletPreferences();
	}

	public RenderRequest getRenderRequest();

	public ThemeDisplay getThemeDisplay();

}