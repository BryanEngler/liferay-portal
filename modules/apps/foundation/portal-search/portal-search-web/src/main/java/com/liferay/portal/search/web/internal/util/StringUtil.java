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

import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;

import java.util.Objects;

/**
 * @author André de Oliveira
 */
public class StringUtil {

	public static String concat(Object ... objects) {
		if (objects == null || objects.length == 0) {
			return StringPool.BLANK;
		}

		if (objects.length == 1) {
			return toString(objects[0]);
		}

		if (objects.length == 2) {
			String string0 = toString(objects[0]);
			String string1 = toString(objects[1]);

			return string0.concat(string1);
		}

		StringBundler stringBundler = new StringBundler(objects.length);

		for (Object object : objects) {
			stringBundler.append(toString(object));
		}

		return stringBundler.toString();
	}

	public static String toString(Object object) {
		return Objects.toString(object, StringPool.BLANK);
	}

}