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

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jorge Ferrer
 */
public class CompanyPortletSettingsTest extends BasePortletSettingsTestCase {

	@Before
	public void setUp() throws Exception {
		portletSettings = new CompanyPortletSettings(getPortletPreferences());
	}

	@Test
	public void testGetValueFromPortalProperties() {
		setPortalProperties(_PORTAL_PROPERTIES_NAME, _PORTAL_PROPERTIES_VALUE);

		String value = portletSettings.getValue(_PORTAL_PROPERTIES_NAME, null);

		Assert.assertEquals(_PORTAL_PROPERTIES_VALUE, value);
	}

	@Test
	public void testSetValue() {
		setPortalProperties(_PORTAL_PROPERTIES_NAME, _PORTAL_PROPERTIES_VALUE);

		portletSettings.setValue(_PORTAL_PROPERTIES_NAME, "newValue");

		String value = portletSettings.getValue(_PORTAL_PROPERTIES_NAME, null);

		Assert.assertEquals("newValue", value);
	}

	protected void setPortalProperties(String name, String value) {
		CompanyPortletSettings companyPortletSettings =
			(CompanyPortletSettings)portletSettings;

		Properties portalProperties = new Properties();

		portalProperties.setProperty(name, value);

		companyPortletSettings.setPortalProperties(portalProperties);
	}

	private static final String _PORTAL_PROPERTIES_NAME =
		"portalPropertiesName";

	private static final String _PORTAL_PROPERTIES_VALUE =
		"portalPropertiesValue";

}