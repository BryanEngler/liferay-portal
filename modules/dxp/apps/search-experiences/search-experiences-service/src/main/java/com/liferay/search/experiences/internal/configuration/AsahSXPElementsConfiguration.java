/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Bryan Engler
 */
@ExtendedObjectClassDefinition(
	category = "search-experiences",
	scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
	id = "com.liferay.search.experiences.internal.configuration.AsahSXPElementsConfiguration",
	localization = "content/Language", name = "asah-elements-configuration-name"
)
public interface AsahSXPElementsConfiguration {

	@Meta.AD(
		deflt = "1440", description = "cache-time-to-live-help",
		name = "cache-time-to-live", required = false
	)
	public int cacheTimeToLive();

}