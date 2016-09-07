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

package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.web.internal.display.context.IndexSearchPropsValues;
import com.liferay.portal.search.web.internal.display.context.QueryConfigPreferences;

import javax.portlet.PortletPreferences;

/**
 * @author André de Oliveira
 */
public class SearchPorletQueryConfigPreferences
	implements QueryConfigPreferences {

	public SearchPorletQueryConfigPreferences(
		PortletPreferences portletPreferences,
		IndexSearchPropsValues indexSearchPropsValues) {

		_portletPreferences = portletPreferences;
		_indexSearchPropsValues = indexSearchPropsValues;
	}

	@Override
	public int getCollatedSpellCheckResultDisplayThreshold() {
		if (_collatedSpellCheckResultDisplayThreshold != null) {
			return _collatedSpellCheckResultDisplayThreshold;
		}

		int collatedSpellCheckResultScoresThreshold =
			_indexSearchPropsValues.
				getCollatedSpellCheckResultScoresThreshold();

		_collatedSpellCheckResultDisplayThreshold = GetterUtil.getInteger(
			_portletPreferences.getValue(
				"collatedSpellCheckResultDisplayThreshold", null),
			collatedSpellCheckResultScoresThreshold);

		if (_collatedSpellCheckResultDisplayThreshold < 0) {
			_collatedSpellCheckResultDisplayThreshold =
				collatedSpellCheckResultScoresThreshold;
		}

		return _collatedSpellCheckResultDisplayThreshold;
	}

	@Override
	public int getQueryIndexingThreshold() {
		if (_queryIndexingThreshold != null) {
			return _queryIndexingThreshold;
		}

		_queryIndexingThreshold = GetterUtil.getInteger(
			_portletPreferences.getValue("queryIndexingThreshold", null),
			_indexSearchPropsValues.getQueryIndexingThreshold());

		if (_queryIndexingThreshold < 0) {
			_queryIndexingThreshold =
				_indexSearchPropsValues.getQueryIndexingThreshold();
		}

		return _queryIndexingThreshold;
	}

	@Override
	public int getQuerySuggestionsDisplayThreshold() {
		if (_querySuggestionsDisplayThreshold != null) {
			return _querySuggestionsDisplayThreshold;
		}

		_querySuggestionsDisplayThreshold = GetterUtil.getInteger(
			_portletPreferences.getValue(
				"querySuggestionsDisplayThreshold", null),
			_indexSearchPropsValues.getQuerySuggestionScoresThreshold());

		if (_querySuggestionsDisplayThreshold < 0) {
			_querySuggestionsDisplayThreshold =
				_indexSearchPropsValues.getQuerySuggestionScoresThreshold();
		}

		return _querySuggestionsDisplayThreshold;
	}

	@Override
	public int getQuerySuggestionsMax() {
		if (_querySuggestionsMax != null) {
			return _querySuggestionsMax;
		}

		_querySuggestionsMax = GetterUtil.getInteger(
			_portletPreferences.getValue("querySuggestionsMax", null),
			_indexSearchPropsValues.getQuerySuggestionMax());

		if (_querySuggestionsMax <= 0) {
			_querySuggestionsMax =
				_indexSearchPropsValues.getQuerySuggestionMax();
		}

		return _querySuggestionsMax;
	}

	@Override
	public boolean isCollatedSpellCheckResultEnabled() {
		if (_collatedSpellCheckResultEnabled != null) {
			return _collatedSpellCheckResultEnabled;
		}

		_collatedSpellCheckResultEnabled = GetterUtil.getBoolean(
			_portletPreferences.getValue(
				"collatedSpellCheckResultEnabled", null),
			_indexSearchPropsValues.isCollatedSpellCheckResultEnabled());

		return _collatedSpellCheckResultEnabled;
	}

	@Override
	public boolean isQueryIndexingEnabled() {
		if (_queryIndexingEnabled != null) {
			return _queryIndexingEnabled;
		}

		_queryIndexingEnabled = GetterUtil.getBoolean(
			_portletPreferences.getValue("queryIndexingEnabled", null),
			_indexSearchPropsValues.isQueryIndexingEnabled());

		return _queryIndexingEnabled;
	}

	@Override
	public boolean isQuerySuggestionsEnabled() {
		if (_querySuggestionsEnabled != null) {
			return _querySuggestionsEnabled;
		}

		_querySuggestionsEnabled = GetterUtil.getBoolean(
			_portletPreferences.getValue("querySuggestionsEnabled", null),
			_indexSearchPropsValues.isQuerySuggestionEnabled());

		return _querySuggestionsEnabled;
	}

	private Integer _collatedSpellCheckResultDisplayThreshold;
	private Boolean _collatedSpellCheckResultEnabled;
	private final IndexSearchPropsValues _indexSearchPropsValues;
	private final PortletPreferences _portletPreferences;
	private Boolean _queryIndexingEnabled;
	private Integer _queryIndexingThreshold;
	private Integer _querySuggestionsDisplayThreshold;
	private Boolean _querySuggestionsEnabled;
	private Integer _querySuggestionsMax;

}