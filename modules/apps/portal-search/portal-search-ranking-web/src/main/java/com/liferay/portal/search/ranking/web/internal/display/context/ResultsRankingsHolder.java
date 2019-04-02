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

package com.liferay.portal.search.ranking.web.internal.display.context;

import com.liferay.portal.kernel.search.Document;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Wade Cao
 */
public class ResultsRankingsHolder {

	public ResultsRankingsHolder(int capacity) {
		_map = new LinkedHashMap<>(capacity);
	}

	public ResultsRankingDisplayContext get(Document document) {
		return _map.get(document);
	}

	public Collection<Document> getDocuments() {
		return Collections.unmodifiableSet(_map.keySet());
	}

	public void put(
		Document document,
		ResultsRankingDisplayContext resultsRankingDisplayContext) {

		_map.put(
			document, Objects.requireNonNull(resultsRankingDisplayContext));
	}

	private final Map<Document, ResultsRankingDisplayContext> _map;

}