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

package com.liferay.portal.search.internal.query;

import com.liferay.portal.search.query.LearnToRankQuery;
import com.liferay.portal.search.query.QueryVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bryan Engler
 */
public class LearnToRankQueryImpl
	extends BaseQueryImpl implements LearnToRankQuery {

	@Override
	public <T> T accept(QueryVisitor<T> queryVisitor) {
		return queryVisitor.visit(this);
	}

	@Override
	public void addParam(String key, Object value) {
		_params.put(key, value);
	}

	@Override
	public String getModelName() {
		return _modelName;
	}

	@Override
	public Map<String, Object> getParams() {
		return _params;
	}

	@Override
	public void setModelName(String modelName) {
		_modelName = modelName;
	}

	@Override
	public void setParams(Map<String, Object> params) {
		_params = params;
	}

	private String _modelName;
	private Map<String, Object> _params = new HashMap<>();

}