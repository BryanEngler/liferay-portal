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

package com.liferay.portal.search.internal.rescore;

import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.rescore.Rescore;
import com.liferay.portal.search.rescore.RescoreBuilder;

/**
 * @author Bryan Engler
 */
public class RescoreBuilderImpl implements RescoreBuilder {

	@Override
	public Rescore build() {
		return new RescoreImpl(_query, _windowSize);
	}

	@Override
	public RescoreBuilder query(Query query) {
		_query = query;

		return this;
	}

	@Override
	public RescoreBuilder windowSize(Integer windowSize) {
		_windowSize = windowSize;

		return this;
	}

	private Query _query;
	private Integer _windowSize;

}