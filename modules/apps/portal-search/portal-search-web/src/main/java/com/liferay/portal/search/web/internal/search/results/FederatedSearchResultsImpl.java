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

package com.liferay.portal.search.web.internal.search.results;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.HitsImpl;
import com.liferay.portal.search.web.search.result.FederatedSearchResults;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bryan Engler
 */
public class FederatedSearchResultsImpl implements FederatedSearchResults {

	@Override
	public void add(String source, Hits hits) {
		_sourcedHits.put(source, hits);
	}

	@Override
	public Hits getHitsFromSource(String source) {
		Hits hits =  _sourcedHits.get(source);

		if (hits == null) {
			return new HitsImpl();
		}

		return hits;
	}

	@Override
	public Document[] getDocumentsFromSource(String source) {
		Hits hits = _sourcedHits.get(source);

		if (hits == null) {
			return new Document[0];
		}

		return hits.getDocs();
	}

	@Override
	public int getTotalCountFromSource(String source) {
		Hits hits = _sourcedHits.get(source);

		if (hits == null) {
			return 0;
		}

		return hits.getLength();
	}

	private Map<String, Hits> _sourcedHits = new HashMap<>();
}