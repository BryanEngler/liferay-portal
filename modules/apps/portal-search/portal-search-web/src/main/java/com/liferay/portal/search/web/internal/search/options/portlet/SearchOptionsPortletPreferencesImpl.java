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

package com.liferay.portal.search.web.internal.search.options.portlet;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.web.internal.util.PortletPreferencesHelper;
import com.liferay.portal.search.spi.federated.searcher.FederatedSearcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.portlet.PortletPreferences;

/**
 * @author Wade Cao
 */
public class SearchOptionsPortletPreferencesImpl
	implements SearchOptionsPortletPreferences {

	public SearchOptionsPortletPreferencesImpl(
		Optional<PortletPreferences> portletPreferencesOptional, Collection<FederatedSearcher> federatedSearchers) {

		_portletPreferencesHelper = new PortletPreferencesHelper(
			portletPreferencesOptional);

		_federatedSearchers = federatedSearchers;
	}

	@Override
	public boolean isAllowEmptySearches() {
		return _portletPreferencesHelper.getBoolean(
			SearchOptionsPortletPreferences.PREFERENCE_KEY_ALLOW_EMPTY_SEARCHES,
			false);
	}

	@Override
	public boolean isBasicFacetSelection() {
		return _portletPreferencesHelper.getBoolean(
			SearchOptionsPortletPreferences.
				PREFERENCE_KEY_BASIC_FACET_SELECTION,
			false);
	}

	@Override
	public boolean federatedSearchEnabled() {
		return _portletPreferencesHelper.getBoolean(
			SearchOptionsPortletPreferences.
				PREFERENCE_KEY_FEDERATED_SEARCH_ENABLED,
			false);
	}

	@Override
	public Optional<String[]> getFederatedSearchSourcesArray() {
		Optional<String> federatedSearchSources = _portletPreferencesHelper.getString(
			SearchOptionsPortletPreferences.PREFERENCE_KEY_FEDERATED_SEARCH_SOURCES);

		return federatedSearchSources.map(StringUtil::split);
	}

	@Override
	public String getFederatedSearchSourcesString() {
		return _portletPreferencesHelper.getString(
			SearchOptionsPortletPreferences.PREFERENCE_KEY_FEDERATED_SEARCH_SOURCES,
			StringPool.BLANK);
	}

	@Override
	public List<KeyValuePair> getAvailableFederatedSearchSources() {
		Optional<String[]> federatedSearchSourcesOptional =
			getFederatedSearchSourcesArray();

		String[] allFederatedSearchSources = getAllFederatedSearchSources();

		String[] federatedSearchSources =
			federatedSearchSourcesOptional.orElse(allFederatedSearchSources);

		List<KeyValuePair> availableFederatedSearchSources = new ArrayList<>();

		for (String source : allFederatedSearchSources) {
			if (!ArrayUtil.contains(federatedSearchSources, source)) {
				availableFederatedSearchSources.add(
					getKeyValuePair(source, source)); //displayname?
			}
		}

		return availableFederatedSearchSources;
	}

	@Override
	public List<KeyValuePair> getCurrentFederatedSearchSources() {

		String[] federatedSearchSources = getCurrentFederatedSearchSourcesArray();

		List<KeyValuePair> currentFederatedSearchSources = new ArrayList<>();

		for (String source : federatedSearchSources) {
			currentFederatedSearchSources.add(getKeyValuePair(source, source));
		}

		return currentFederatedSearchSources;
	}

	@Override
	public String[] getCurrentFederatedSearchSourcesArray() {
		Optional<String[]> federatedSearchSourcesOptional = getFederatedSearchSourcesArray();

		String[] allfederatedSearchSources = getAllFederatedSearchSources();

		return federatedSearchSourcesOptional.orElse(allfederatedSearchSources);
	}

	protected String[] getAllFederatedSearchSources() {
		List<String> sources = new ArrayList<>();

		for (FederatedSearcher federatedSearcher : _federatedSearchers) {
			sources.add(federatedSearcher.getSourceDisplayName());
		}

		return ArrayUtil.toStringArray(sources);
	}

	protected KeyValuePair getKeyValuePair(String name, String displayName) {
		return new KeyValuePair(name, displayName);
	}

	private Collection<FederatedSearcher> _federatedSearchers;

	private final PortletPreferencesHelper _portletPreferencesHelper;

}