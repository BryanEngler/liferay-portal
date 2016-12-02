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
import com.liferay.portal.kernel.util.Validator;

import java.util.Optional;

/**
 * @author André de Oliveira
 */
public class StringUtil {

	public static String concat(Object obj, Object concat) {
		String s1 = StringPool.NULL;

		if (obj != null) {
			s1 = obj.toString();
		}

		String s2 = StringPool.NULL;

		if (concat != null) {
			s2 = concat.toString();
		}

		if (s1.length() == 0) {
			return s2;
		}

		if (s2.length() == 0) {
			return s1;
		}

		return s1.concat(s2);
	}

	public static String concat(Object obj1, Object obj2, Object... objArray) {
		StringBundler sb = new StringBundler(2 + objArray.length);

		sb.append(obj1);
		sb.append(obj2);

		for (Object obj : objArray) {
			sb.append(obj);
		}

		return sb.toString();
	}

	public static String concat(String s, String concat) {
		if (s == null) {
			s = StringPool.NULL;
		}

		if (concat == null) {
			concat = StringPool.NULL;
		}

		if (s.length() == 0) {
			return concat;
		}

		if (concat.length() == 0) {
			return s;
		}

		return s.concat(concat);
	}

	public static String concat(String s1, String s2, String... stringArray) {
		StringBundler sb = new StringBundler(2 + stringArray.length);

		sb.append(s1);
		sb.append(s2);
		sb.append(stringArray);

		return sb.toString();
	}

	public static Optional<String> trimOpt(String s) {
		s = com.liferay.portal.kernel.util.StringUtil.trim(s);

		if (Validator.isBlank(s)) {
			return Optional.empty();
		}

		return Optional.of(s);
	}

}