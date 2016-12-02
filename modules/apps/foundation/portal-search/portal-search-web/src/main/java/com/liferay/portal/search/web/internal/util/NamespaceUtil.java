package com.liferay.portal.search.web.internal.util;

import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

public class NamespaceUtil {

	public static String randomNamespace(
		String prefix, HttpServletRequest request) {

		String randomKey = PortalUtil.generateRandomKey(
			request, prefix.concat(StringUtil.randomString()));

		return randomKey.concat(StringPool.UNDERLINE);
	}

}
