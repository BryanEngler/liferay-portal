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

package com.liferay.portal.search.elasticsearch7.internal.query;

import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.search.query.LearnToRankQuery;

import com.o19s.es.ltr.feature.store.FeatureStore;
import com.o19s.es.ltr.query.StoredLtrQueryBuilder;
import com.o19s.es.ltr.utils.FeatureStoreLoader;

import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;

import org.osgi.service.component.annotations.Component;

/**
 * @author Bryan Engler
 */
@Component(service = LearnToRankQueryTranslator.class)
public class LearnToRankQueryTranslatorImpl
	implements LearnToRankQueryTranslator {

	@Override
	public QueryBuilder translate(LearnToRankQuery learnToRankQuery) {
		StoredLtrQueryBuilder storedLtrQueryBuilder = new StoredLtrQueryBuilder(
			new FeatureStoreLoader() {

				@Override
				public FeatureStore load(String storeName, Client client) {
					return null;
				}

			});

		if (learnToRankQuery.getBoost() != null) {
			storedLtrQueryBuilder.boost(learnToRankQuery.getBoost());
		}

		if (learnToRankQuery.getModelName() != null) {
			storedLtrQueryBuilder.modelName(learnToRankQuery.getModelName());
		}

		if (MapUtil.isNotEmpty(learnToRankQuery.getParams())) {
			storedLtrQueryBuilder.params(learnToRankQuery.getParams());
		}

		if (learnToRankQuery.getQueryName() != null) {
			storedLtrQueryBuilder.queryName(learnToRankQuery.getQueryName());
		}

		return storedLtrQueryBuilder;
	}

}