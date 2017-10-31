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

package com.liferay.portal.search.solr.internal.connection;

import com.liferay.portal.search.solr.internal.http.BasicAuthPoolingHttpClientFactory;

import java.util.Collections;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;

/**
 * @author Bryan Engler
 */
public class SolrFixture {

	public SolrClient getClient() throws Exception {
		BasicAuthPoolingHttpClientFactory httpClientFactory =
			new BasicAuthPoolingHttpClientFactory() {
				{
					activate(Collections.<String, Object>emptyMap());
				}
			};

		HttpClient readerHttpClient = httpClientFactory.createInstance();

		LBHttpSolrClient readerLBHttpSolrClient = new LBHttpSolrClient(
			readerHttpClient, "http://localhost:8983/solr/liferay/");

		HttpClient writerHttpClient = httpClientFactory.createInstance();

		LBHttpSolrClient writerLBHttpSolrClient = new LBHttpSolrClient(
			writerHttpClient, "http://localhost:8983/solr/liferay/");

		ReadWriteSolrClient readWriteSolrClient = new ReadWriteSolrClient(
			readerLBHttpSolrClient, writerLBHttpSolrClient);

		return readWriteSolrClient;
	}

}