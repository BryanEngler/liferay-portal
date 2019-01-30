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

package com.liferay.portal.search.legacy.stats;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.search.stats.Stats;

/**
 * @author Bryan Engler
 */
@ProviderType
public interface StatsFactory {

	/**
	 * Provides a 'com.liferay.portal.search.api' Stats object with an expanded
	 * API, based off a legacy Stats object.
	 *
	 * @param legacyStats the legacy Stats object to be converted
	 * @return the converted 'com.liferay.portal.search.api' Stats object
	 *
	 * @review
	 */
	public Stats getStats(com.liferay.portal.kernel.search.Stats legacyStats);

}