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

package com.liferay.portal.kernel.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * @author Bryan Engler
 */
public class URLStringUtil {

	public static String addParameter(String url, String name, String value) {
		if (url == null) {
			return null;
		}

		String[] urlArray = stripURLAnchor(url, StringPool.POUND);

		url = urlArray[0];

		String anchor = urlArray[1];

		StringBundler sb = new StringBundler(6);

		sb.append(url);

		if (url.indexOf(CharPool.QUESTION) == -1) {
			sb.append(StringPool.QUESTION);
		}
		else if (!url.endsWith(StringPool.QUESTION) &&
				 !url.endsWith(StringPool.AMPERSAND)) {

			sb.append(StringPool.AMPERSAND);
		}

		sb.append(name);
		sb.append(StringPool.EQUAL);
		sb.append(URLCodec.encodeURL(value));
		sb.append(anchor);

		String result = sb.toString();

		if (result.length() > Http.URL_MAXIMUM_LENGTH) {
			result = shortenURL(result, 2);
		}

		return result;
	}

	public static String decodeURL(String url) {
		if (Validator.isNull(url)) {
			return url;
		}

		try {
			return URLCodec.decodeURL(url, StringPool.UTF8);
		}
		catch (IllegalArgumentException iae) {
			_log.error(iae.getMessage());
		}

		return StringPool.BLANK;
	}

	public static String removeParameter(String url, String name) {
		if (Validator.isNull(url) || Validator.isNull(name)) {
			return url;
		}

		int pos = url.indexOf(CharPool.QUESTION);

		if (pos == -1) {
			return url;
		}

		String[] array = stripURLAnchor(url, StringPool.POUND);

		url = array[0];

		String anchor = array[1];

		StringBundler sb = new StringBundler();

		sb.append(url.substring(0, pos + 1));

		String[] parameters = StringUtil.split(
			url.substring(pos + 1, url.length()), CharPool.AMPERSAND);

		for (String parameter : parameters) {
			if (parameter.length() > 0) {
				String[] kvp = StringUtil.split(parameter, CharPool.EQUAL);

				String key = kvp[0];

				String value = StringPool.BLANK;

				if (kvp.length > 1) {
					value = kvp[1];
				}

				if (!key.equals(name)) {
					sb.append(key);
					sb.append(StringPool.EQUAL);
					sb.append(value);
					sb.append(StringPool.AMPERSAND);
				}
			}
		}

		url = StringUtil.replace(
			sb.toString(), StringPool.AMPERSAND + StringPool.AMPERSAND,
			StringPool.AMPERSAND);

		if (url.endsWith(StringPool.AMPERSAND)) {
			url = url.substring(0, url.length() - 1);
		}

		if (url.endsWith(StringPool.QUESTION)) {
			url = url.substring(0, url.length() - 1);
		}

		return url + anchor;
	}

	public static String setParameter(String url, String name, long value) {
		return setParameter(url, name, String.valueOf(value));
	}

	public static String setParameter(String url, String name, String value) {
		if (Validator.isNull(url) || Validator.isNull(name)) {
			return url;
		}

		url = removeParameter(url, name);

		return addParameter(url, name, value);
	}

	public static String shortenURL(String url, int count) {
		if (count == 0) {
			return null;
		}

		StringBundler sb = new StringBundler();

		String[] params = StringUtil.split(url, CharPool.AMPERSAND);

		for (int i = 0; i < params.length; i++) {
			String param = params[i];

			if (param.contains("_backURL=") || param.contains("_redirect=") ||
				param.contains("_returnToFullPageURL=") ||
				param.startsWith("redirect")) {

				int pos = param.indexOf(CharPool.EQUAL);

				String qName = param.substring(0, pos);

				String redirect = param.substring(pos + 1);

				try {
					redirect = decodeURL(redirect);
				}
				catch (IllegalArgumentException iae) {
					if (_log.isDebugEnabled()) {
						_log.debug(
							"Skipping undecodable parameter " + param, iae);
					}

					continue;
				}

				String newURL = shortenURL(redirect, count - 1);

				if (newURL != null) {
					newURL = URLCodec.encodeURL(newURL);

					sb.append(qName);
					sb.append(StringPool.EQUAL);
					sb.append(newURL);

					if (i < (params.length - 1)) {
						sb.append(StringPool.AMPERSAND);
					}
				}
			}
			else {
				sb.append(param);

				if (i < (params.length - 1)) {
					sb.append(StringPool.AMPERSAND);
				}
			}
		}

		return sb.toString();
	}

	public static String[] stripURLAnchor(String url, String separator) {
		String anchor = StringPool.BLANK;

		int pos = url.indexOf(separator);

		if (pos != -1) {
			anchor = url.substring(pos);
			url = url.substring(0, pos);
		}

		return new String[] {url, anchor};
	}

	private static final Log _log = LogFactoryUtil.getLog(URLStringUtil.class);

}