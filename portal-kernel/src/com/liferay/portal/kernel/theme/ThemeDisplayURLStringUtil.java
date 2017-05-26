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

package com.liferay.portal.kernel.theme;

import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.util.URLStringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Bryan Engler
 */
public class ThemeDisplayURLStringUtil {

	public static String addPreservedParameters(
		ThemeDisplay themeDisplay, String url, boolean typeControlPanel,
		boolean doAsUser) {

		if (doAsUser) {
			if (Validator.isNotNull(themeDisplay.getDoAsUserId())) {
				url = URLStringUtil.setParameter(
					url, "doAsUserId", themeDisplay.getDoAsUserId());
			}

			if (Validator.isNotNull(themeDisplay.getDoAsUserLanguageId())) {
				url = URLStringUtil.setParameter(
					url, "doAsUserLanguageId",
					themeDisplay.getDoAsUserLanguageId());
			}
		}

		if (typeControlPanel) {
			if (Validator.isNotNull(themeDisplay.getPpid())) {
				url = URLStringUtil.setParameter(
					url, "p_p_id", themeDisplay.getPpid());
			}

			if (themeDisplay.getDoAsGroupId() > 0) {
				url = URLStringUtil.setParameter(
					url, "doAsGroupId", themeDisplay.getDoAsGroupId());
			}

			if (themeDisplay.getRefererGroupId() !=
					GroupConstants.DEFAULT_PARENT_GROUP_ID) {

				url = URLStringUtil.setParameter(
					url, "refererGroupId", themeDisplay.getRefererGroupId());
			}

			if (themeDisplay.getRefererPlid() != LayoutConstants.DEFAULT_PLID) {
				url = URLStringUtil.setParameter(
					url, "refererPlid", themeDisplay.getRefererPlid());
			}
		}

		return url;
	}

}