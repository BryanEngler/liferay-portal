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

package com.liferay.portal.search.web.internal.util;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Optional;

import javax.portlet.PortletPreferences;

/**
 * @author André de Oliveira
 */
public class PortletPreferencesHelper {

	public PortletPreferencesHelper(PortletPreferences portletPreferences) {
		_portletPreferences = portletPreferences;
	}

	public Optional<Boolean> getBoolean(String key) {
		Optional<String> value = getValue(key);

		return value.map(GetterUtil::getBoolean);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		Optional<Boolean> value = getBoolean(key);

		return value.orElse(defaultValue);
	}

	public Optional<Integer> getInteger(String key) {
		Optional<String> value = getValue(key);

		return value.map(GetterUtil::getInteger);
	}

	public int getInteger(String key, int defaultValue) {
		Optional<Integer> value = getInteger(key);

		return value.orElse(defaultValue);
	}

	public Optional<String> getString(String key) {
		return getValue(key);
	}

	protected Optional<String> getValue(String key) {
		String value = StringUtil.trim(
			_portletPreferences.getValue(key, StringPool.BLANK));

		if (Validator.isBlank(value)) {
			return Optional.empty();
		}

		return Optional.of(value);
	}

	private final PortletPreferences _portletPreferences;

}