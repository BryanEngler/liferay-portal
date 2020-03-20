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

package com.liferay.portal.search.internal.engine;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.search.SearchEngineHelper;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.configuration.SearchEngineIdConfiguration;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Joshua Cords
 */
@Component(
	configurationPid = "com.liferay.portal.search.configuration.SearchEngineIdConfiguration",
	configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true,
	service = SearchEngineIdProvider.class
)
public class SearchEngineIdProviderImpl implements SearchEngineIdProvider {

	public String getSearchEngineId() {
		return _searchEngineId;
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		SearchEngineIdConfiguration searchEngineIdConfiguration =
			ConfigurableUtil.createConfigurable(
				SearchEngineIdConfiguration.class, properties);

		String searchEngineId =
			searchEngineIdConfiguration.indexSearchEngineId();

		if (Validator.isNotNull(searchEngineId)) {
			_searchEngineId = searchEngineId;
		}
		else {
			_searchEngineId = SearchEngineHelper.SYSTEM_ENGINE_ID;
		}
	}

	private String _searchEngineId = SearchEngineHelper.SYSTEM_ENGINE_ID;

}