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
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;

import javax.annotation.Generated;

import org.osgi.service.component.ComponentServiceObjects;

/**
 * @author Bryan Engler
 * @generated
 */
@Generated("")
public class Query {

	public static void setDocumentResourceComponentServiceObjects(
		ComponentServiceObjects<DocumentResource>
			documentResourceComponentServiceObjects) {

		_documentResourceComponentServiceObjects =
			documentResourceComponentServiceObjects;
	}

	public static void setSearchResultResourceComponentServiceObjects(
		ComponentServiceObjects<SearchResultResource>
			searchResultResourceComponentServiceObjects) {

		_searchResultResourceComponentServiceObjects =
			searchResultResourceComponentServiceObjects;
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public Document getDocument(
			@GraphQLName("index") String index, @GraphQLName("uid") String uid)
		throws Exception {

		return _applyComponentServiceObjects(
			_documentResourceComponentServiceObjects,
			this::_populateResourceContext,
			documentResource -> documentResource.getDocument(index, uid));
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public SearchResult getSearchHiddenCompanyIndexKeywordsFromSize(
			@GraphQLName("companyId") Long companyId,
			@GraphQLName("index") String index,
			@GraphQLName("keywords") String keywords,
			@GraphQLName("from") Long from, @GraphQLName("size") Long size)
		throws Exception {

		return _applyComponentServiceObjects(
			_searchResultResourceComponentServiceObjects,
			this::_populateResourceContext,
			searchResultResource ->
				searchResultResource.
					getSearchHiddenCompanyIndexKeywordsFromSize(
						companyId, index, keywords, from, size));
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public SearchResult getSearchCompanyKeywordsFromSize(
			@GraphQLName("companyId") Long companyId,
			@GraphQLName("keywords") String keywords,
			@GraphQLName("from") Long from, @GraphQLName("size") Long size)
		throws Exception {

		return _applyComponentServiceObjects(
			_searchResultResourceComponentServiceObjects,
			this::_populateResourceContext,
			searchResultResource ->
				searchResultResource.getSearchCompanyKeywordsFromSize(
					companyId, keywords, from, size));
	}

	private <T, R, E1 extends Throwable, E2 extends Throwable> R
			_applyComponentServiceObjects(
				ComponentServiceObjects<T> componentServiceObjects,
				UnsafeConsumer<T, E1> unsafeConsumer,
				UnsafeFunction<T, R, E2> unsafeFunction)
		throws E1, E2 {

		T resource = componentServiceObjects.getService();

		try {
			unsafeConsumer.accept(resource);

			return unsafeFunction.apply(resource);
		}
		finally {
			componentServiceObjects.ungetService(resource);
		}
	}

	private void _populateResourceContext(DocumentResource documentResource)
		throws Exception {

		documentResource.setContextCompany(
			CompanyLocalServiceUtil.getCompany(
				CompanyThreadLocal.getCompanyId()));
	}

	private void _populateResourceContext(
			SearchResultResource searchResultResource)
		throws Exception {

		searchResultResource.setContextCompany(
			CompanyLocalServiceUtil.getCompany(
				CompanyThreadLocal.getCompanyId()));
	}

	private static ComponentServiceObjects<DocumentResource>
		_documentResourceComponentServiceObjects;
	private static ComponentServiceObjects<SearchResultResource>
		_searchResultResourceComponentServiceObjects;

}