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

package com.liferay.portal.search.internal.batch;

import com.liferay.portal.search.batch.IndexableActionableDynamicQuery;
import com.liferay.portal.search.batch.IndexableActionableDynamicQueryFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(
	immediate = true, service = IndexableActionableDynamicQueryFactory.class
)
public class IndexableActionableDynamicQueryFactoryImpl
	implements IndexableActionableDynamicQueryFactory {

	@Override
	public IndexableActionableDynamicQuery getIndexableActionableDynamicQuery(
		com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
			indexableActionableDynamicQueryPrototype) {

		indexableActionableDynamicQuery.setBaseLocalService(
			indexableActionableDynamicQueryPrototype.getBaseLocalService());
		indexableActionableDynamicQuery.setClassLoader(
			indexableActionableDynamicQueryPrototype.getClassLoader());
		indexableActionableDynamicQuery.setModelClass(
			indexableActionableDynamicQueryPrototype.getModelClass());

		indexableActionableDynamicQuery.setPrimaryKeyPropertyName(
			indexableActionableDynamicQueryPrototype.
				getPrimaryKeyPropertyName());

		return indexableActionableDynamicQuery;
	}

	@Reference
	protected IndexableActionableDynamicQuery indexableActionableDynamicQuery;

}