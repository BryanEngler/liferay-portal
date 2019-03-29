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

package com.liferay.headless.search.internal.graphql.query.v1_0;

import com.liferay.headless.search.dto.v1_0.Document;
import com.liferay.headless.search.dto.v1_0.SearchResult;
import com.liferay.headless.search.resource.v1_0.DocumentResource;
import com.liferay.headless.search.resource.v1_0.SearchResultResource;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;

import javax.annotation.Generated;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Bryan Engler
 * @generated
 */
@Generated("")
public class Query {

	@GraphQLField
	@GraphQLInvokeDetached
	public Document getDocumentIndexUid(
			@GraphQLName("index") String index, @GraphQLName("uid") String uid)
		throws Exception {

		DocumentResource documentResource = _createDocumentResource();

		return documentResource.getDocumentIndexUid(index, uid);
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public SearchResult getSearchIndexKeywordsHiddenStartDelta(
			@GraphQLName("index") String index,
			@GraphQLName("keywords") String keywords,
			@GraphQLName("hidden") String hidden,
			@GraphQLName("start") Long start, @GraphQLName("delta") Long delta)
		throws Exception {

		SearchResultResource searchResultResource =
			_createSearchResultResource();

		return searchResultResource.getSearchIndexKeywordsHiddenStartDelta(
			index, keywords, hidden, start, delta);
	}

	private static DocumentResource _createDocumentResource() throws Exception {
		DocumentResource documentResource =
			_documentResourceServiceTracker.getService();

		documentResource.setContextCompany(
			CompanyLocalServiceUtil.getCompany(
				CompanyThreadLocal.getCompanyId()));

		return documentResource;
	}

	private static final ServiceTracker<DocumentResource, DocumentResource>
		_documentResourceServiceTracker;

	private static SearchResultResource _createSearchResultResource()
		throws Exception {

		SearchResultResource searchResultResource =
			_searchResultResourceServiceTracker.getService();

		searchResultResource.setContextCompany(
			CompanyLocalServiceUtil.getCompany(
				CompanyThreadLocal.getCompanyId()));

		return searchResultResource;
	}

	private static final ServiceTracker
		<SearchResultResource, SearchResultResource>
			_searchResultResourceServiceTracker;

	static {
		Bundle bundle = FrameworkUtil.getBundle(Query.class);

		ServiceTracker<DocumentResource, DocumentResource>
			documentResourceServiceTracker = new ServiceTracker<>(
				bundle.getBundleContext(), DocumentResource.class, null);

		documentResourceServiceTracker.open();

		_documentResourceServiceTracker = documentResourceServiceTracker;
		ServiceTracker<SearchResultResource, SearchResultResource>
			searchResultResourceServiceTracker = new ServiceTracker<>(
				bundle.getBundleContext(), SearchResultResource.class, null);

		searchResultResourceServiceTracker.open();

		_searchResultResourceServiceTracker =
			searchResultResourceServiceTracker;
	}

}