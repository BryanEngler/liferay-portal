/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.configuration;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Dictionary;

/**
 * @author Bryan Engler
 */
public class ElasticsearchConfigurationSanitizer {

	public static void sanitizeProperties(
		Dictionary<String, Object> properties) {

		if (properties == null) {
			return;
		}

		properties.remove("discoveryZenPingUnicastHostsPort");

		Object embeddedHttpPort = properties.remove("embeddedHttpPort");

		if (embeddedHttpPort != null) {
			properties.put(
				"sidecarHttpPort", GetterUtil.getInteger(embeddedHttpPort));
		}

		String operationMode = GetterUtil.getString(
			properties.remove("operationMode"));

		if (StringUtil.equals(operationMode, "REMOTE")) {
			properties.put("productionModeEnabled", Boolean.TRUE);
		}

		Object trackTotalHits = properties.remove("trackTotalHits");

		if ((trackTotalHits != null) &&
			!GetterUtil.getBoolean(trackTotalHits)) {

			int indexMaxResultWindow = GetterUtil.getInteger(
				properties.get("indexMaxResultWindow"));

			if (indexMaxResultWindow > 0) {
				properties.put("trackTotalHitsLimit", indexMaxResultWindow);
			}
		}

		properties.remove("restClientLoggerLevel");
	}

}