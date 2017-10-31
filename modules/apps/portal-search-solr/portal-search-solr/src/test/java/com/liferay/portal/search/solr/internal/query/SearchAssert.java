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

package com.liferay.portal.search.solr.internal.query;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.search.Query;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import org.junit.Assert;

/**
 * @author Bryan Engler
 */
public class SearchAssert {

	public static void assertHighlights(
			final SolrClient client, final String field, final Query query,
			final String... expectedValues)
		throws Exception {

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					Assert.assertEquals(
						sort(Arrays.asList(expectedValues)),
						sort(getHighLights(search(client, query), field)));

					return null;
				}

			});
	}

	public static void assertNoHits(
			SolrClient client, String field, Query query)
		throws Exception {

		assertSearch(client, field, query, new String[0]);
	}

	public static void assertSearch(
			final SolrClient client, final String field, final Query query,
			final String... expectedValues)
		throws Exception {

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					Assert.assertEquals(
						sort(Arrays.asList(expectedValues)),
						sort(getValues(search(client, query), field)));

					return null;
				}

			});
	}

	protected static List<String> getHighLights(
		QueryResponse queryResponse, String fieldName) {

		List<String> highlights = new ArrayList<>();

		SolrDocumentList solrDocumentList = queryResponse.getResults();

		Map<String, Map<String, List<String>>> highlightFieldMap =
			queryResponse.getHighlighting();

		for (SolrDocument solrDocument : solrDocumentList) {
			String key = (String)solrDocument.getFieldValue(Field.UID);

			Map<String, List<String>> uidHighlights = highlightFieldMap.get(
				key);

			List<String> snippets = uidHighlights.get(fieldName);

			if (!snippets.isEmpty()) {
				highlights.add(snippets.get(0));
			}
		}

		return highlights;
	}

	protected static List<String> getValues(
		QueryResponse queryResponse, String field) {

		List<String> values = new ArrayList<>();

		SolrDocumentList solrDocumentList = queryResponse.getResults();

		for (SolrDocument solrDocument : solrDocumentList) {
			values.addAll(
				(Collection<String>)solrDocument.getFieldValue(field));
		}

		return values;
	}

	protected static QueryResponse search(SolrClient client, Query query)
		throws Exception {

		SolrQuery solrQuery = new SolrQuery();

		solrQuery.setQuery(query.toString());

		solrQuery.setFields(StringPool.STAR);

		solrQuery.setHighlight(true);

		solrQuery.addHighlightField(StringPool.STAR);

		solrQuery.setHighlightSimplePre("<em>");

		solrQuery.setHighlightSimplePost("</em>");

		QueryResponse queryResponse = client.query(
			solrQuery, SolrRequest.METHOD.POST);

		return queryResponse;
	}

	protected static String sort(Collection<String> collection) {
		List<String> list = new ArrayList<>(collection);

		Collections.sort(list);

		return list.toString();
	}

}