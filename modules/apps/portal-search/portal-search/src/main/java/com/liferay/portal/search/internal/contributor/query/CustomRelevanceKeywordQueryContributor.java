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

package com.liferay.portal.search.internal.contributor.query;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.ParseException;
import com.liferay.portal.kernel.search.generic.MatchQuery;
import com.liferay.portal.search.configuration.CustomRelevanceConfiguration;
import com.liferay.portal.search.spi.model.query.contributor.KeywordQueryContributor;
import com.liferay.portal.search.spi.model.query.contributor.helper.KeywordQueryContributorHelper;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import java.util.Map;

/**
 * @author Michael C. Han
 */
@Component(
	configurationPid = "com.liferay.portal.search.configuration.CustomRelevanceConfiguration",
	immediate = true, service = KeywordQueryContributor.class)
public class CustomRelevanceKeywordQueryContributor implements KeywordQueryContributor {

	@Override
	public void contribute(
		String keywords, BooleanQuery booleanQuery,
		KeywordQueryContributorHelper keywordQueryContributorHelper) {

		float boostFactor = customRelevanceConfiguration.boostFactor();
		String boostFieldName = customRelevanceConfiguration.boostFieldName();

		MatchQuery matchQuery = new MatchQuery(boostFieldName, keywords);

		matchQuery.setBoost(boostFactor);

		try {
			booleanQuery.add(matchQuery, BooleanClauseOccur.SHOULD);
		}
		catch (ParseException pe) {
			throw new RuntimeException(pe);
		}
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		customRelevanceConfiguration =
			ConfigurableUtil.createConfigurable(
				CustomRelevanceConfiguration.class, properties);
	}

	protected CustomRelevanceConfiguration customRelevanceConfiguration;

}