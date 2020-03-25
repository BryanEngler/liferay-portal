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

import com.liferay.portal.search.engine.SearchEngineIdProvider;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.engine.SearchEngineInformationHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Joshua Cords
 */
@Component(immediate = true, service = SearchEngineInformationHelper.class)
public class SearchEngineInformationHelperImpl
	implements SearchEngineInformationHelper {

	@Override
	public SearchEngineInformation getSearchEngineInformation() {
		return _getSearchEngineInformation();
	}

	@Override
	public SearchEngineInformation getSearchEngineInformation(
		String searchEngineId) {

		return searchEngineInformations.get(searchEngineId);
	}

	@Override
	public Map<String, SearchEngineInformation> getSearchEngineInformations() {
		return searchEngineInformations;
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void addSearchEngineInformation(
		SearchEngineInformation searchEngineInformation) {

		searchEngineInformations.put(
			searchEngineInformation.getSearchEngineId(),
			searchEngineInformation);
	}

	protected void removeSearchEngineInformation(
		SearchEngineInformation searchEngineInformation) {

		Collection<SearchEngineInformation> searchEngineInformationValues =
			searchEngineInformations.values();

		searchEngineInformationValues.remove(searchEngineInformation);
	}

	@Reference
	protected SearchEngineIdProvider searchEngineIdProvider;

	protected Map<String, SearchEngineInformation> searchEngineInformations =
		new HashMap<>();

	private SearchEngineInformation _getSearchEngineInformation() {
		return searchEngineInformations.get(
			searchEngineIdProvider.getSearchEngineId());
	}

}