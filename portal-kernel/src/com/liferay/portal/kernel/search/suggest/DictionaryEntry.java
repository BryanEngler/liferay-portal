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

package com.liferay.portal.kernel.search.suggest;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;

/**
 * @author Michael C. Han
 * @deprecated As of 7.0.0, moved to {@link
 *             com.liferay.portal.search.suggest.DictionaryEntry}
 */
@Deprecated
public class DictionaryEntry {

	public DictionaryEntry(String line) {
		int index = line.indexOf(StringPool.SPACE);

		if (index > 0) {
			_word = line.substring(0, index);
			_weight = GetterUtil.getFloat(
				line.substring(index + 1, line.length()));
		}
		else {
			_word = line;
			_weight = 0;
		}
	}

	public float getWeight() {
		return _weight;
	}

	public String getWord() {
		return _word;
	}

	private final float _weight;
	private final String _word;

}