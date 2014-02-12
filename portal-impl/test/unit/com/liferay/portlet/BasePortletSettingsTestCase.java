/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.portlet;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jorge Ferrer
 */
public abstract class BasePortletSettingsTestCase {

	@Test
	public void testGetNonnexistentValue() {
		String value = portletSettings.getValue("nonexistentName", null);

		Assert.assertNull(value);
	}

	@Test
	public void testGetValidValue() {
		String value = portletSettings.getValue(_PORTLET_SETTINGS_NAME, null);

		Assert.assertEquals(_PORTLET_SETTINGS_VALUE, value);
	}

	@Test
	public void testSetExistentValue() {
		portletSettings.setValue(_PORTLET_SETTINGS_NAME, "newValue");

		String value = portletSettings.getValue(_PORTLET_SETTINGS_NAME, null);

		Assert.assertEquals("newValue", value);
	}

	@Test
	public void testSetNonexistentValue() {
		portletSettings.setValue("nonexistentName", "newValue");

		String value = portletSettings.getValue("nonexistentName", null);

		Assert.assertEquals("newValue", value);
	}

	protected PortletPreferences getPortletPreferences()
		throws ReadOnlyException {

		PortletPreferences portletPreferences = new PortletPreferencesImpl();

		portletPreferences.setValue(
			_PORTLET_SETTINGS_NAME, _PORTLET_SETTINGS_VALUE);

		return portletPreferences;
	}

	protected PortletSettings portletSettings;

	private static final String _PORTLET_SETTINGS_NAME = "portletSettingsName";

	private static final String _PORTLET_SETTINGS_VALUE =
		"portletSettingsValue";

}