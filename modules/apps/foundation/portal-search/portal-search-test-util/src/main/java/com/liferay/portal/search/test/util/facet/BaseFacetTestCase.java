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

package com.liferay.portal.search.test.util.facet;

import com.liferay.portal.json.JSONObjectImpl;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import org.mockito.Mockito;

/**
 * @author Bryan Engler
 */
public abstract class BaseFacetTestCase extends BaseIndexingTestCase {

	public static void assertEquals(
		String message, Map<?, ?> expectedMap, Map<?, ?> actualMap) {

		Assert.assertEquals(
			message, _toString(expectedMap), _toString(actualMap));
	}

	protected static Map<Long, Integer> toMap(
		List<TermCollector> termCollectors) {

		Map<Long, Integer> actual = new HashMap<>(termCollectors.size());

		for (TermCollector termCollector : termCollectors) {
			actual.put(
				Long.valueOf(termCollector.getTerm()),
				termCollector.getFrequency());
		}

		return actual;
	}

	protected void addDocuments(
			final String field, final String name, int count)
		throws Exception {

		for (int i = 1; i <= count; i++) {
			addDocument(
				new DocumentCreationHelper() {

					@Override
					public void populate(Document document) {
						document.addKeyword(field, name);
					}

				});
		}
	}

	protected void assertAggregation(
		SearchContext searchContext, List<TermCollector> termCollectors,
		int expectedTermCount, Map<String, Integer> expectedTerms) {

		Assert.assertNotNull(termCollectors);
		Assert.assertEquals(expectedTermCount, termCollectors.size());

		assertEquals(
			searchContext.getKeywords(), expectedTerms, toMap(termCollectors));
	}

	protected void setUpJSONFactoryUtil() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		JSONFactory jsonFactory = Mockito.mock(JSONFactory.class);

		Mockito.when(
			jsonFactory.createJSONObject()
		).thenReturn(
			new JSONObjectImpl()
		);

		jsonFactoryUtil.setJSONFactory(jsonFactory);
	}

	protected void testAggregation() throws Exception {
		Map<String, Integer> entries = new HashMap<>();

		String field = Field.FOLDER_ID;

		entries.put("10394", 3);
		entries.put("10506", 16);
		entries.put("13492", 2);
		entries.put("15363", 6);
		entries.put("16432", 7);
		entries.put("16532", 4);

		Map<String, Integer> expectedTerms = new HashMap<>();

		expectedTerms.put("10394", 3);
		expectedTerms.put("10506", 16);
		expectedTerms.put("15363", 6);
		expectedTerms.put("16432", 7);
		expectedTerms.put("16532", 4);

		testAggregation(field, entries, expectedTerms, expectedTerms.size());
	}

	protected void testAggregation(
			String field, Map<String, Integer> entries,
			Map<String, Integer> expectedTerms, int maxTerms)
		throws Exception {

		setUpJSONFactoryUtil();

		for (Map.Entry entry : entries.entrySet()) {
			addDocuments(
				field, (String)entry.getKey(), (Integer)entry.getValue());
		}

		SearchContext searchContext = createSearchContext();

		MultiValueFacet facet = new MultiValueFacet(searchContext);

		facet.setFieldName(field);

		FacetConfiguration facetConfiguration = facet.getFacetConfiguration();

		JSONObject data = facetConfiguration.getData();

		data.put("maxTerms", maxTerms);

		searchContext.addFacet(facet);

		search(searchContext);

		Facet updatedFacet = searchContext.getFacet(field);

		FacetCollector facetCollector = updatedFacet.getFacetCollector();

		List<TermCollector> termCollectors = facetCollector.getTermCollectors();

		assertAggregation(
			searchContext, termCollectors, maxTerms, expectedTerms);
	}

	private static String _toString(Map<?, ?> map) {
		List<String> list = new ArrayList<>(map.size());

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			list.add(entry.toString());
		}

		Collections.sort(list);

		return list.toString();
	}

}