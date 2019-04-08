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

package com.liferay.portal.search.ranking.web.internal.display.context;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.document.Document;

import java.text.DateFormat;

import java.util.Date;
import java.util.List;

/**
 * @author Bryan Engler
 */
public class ResultsRankingEntryDisplayContextBuilder {

	public ResultsRankingEntryDisplayContextBuilder(Document document) {
		_document = document;
	}

	public ResultsRankingEntryDisplayContext build() {
		ResultsRankingEntryDisplayContext resultsRankingEntryDisplayContext =
			new ResultsRankingEntryDisplayContext();

		_setAliases(resultsRankingEntryDisplayContext);
		_setDisplayDate(resultsRankingEntryDisplayContext);
		_setHiddenResultsCount(resultsRankingEntryDisplayContext);
		_setIndex(resultsRankingEntryDisplayContext);
		_setKeywords(resultsRankingEntryDisplayContext);
		_setModifiedDate(resultsRankingEntryDisplayContext);
		_setPinnedResultsCount(resultsRankingEntryDisplayContext);
		_setStatus(resultsRankingEntryDisplayContext);
		_setUid(resultsRankingEntryDisplayContext);

		return resultsRankingEntryDisplayContext;
	}

	private Date _getDate(String name) {
		try {
			DateFormat dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
				_INDEX_DATE_FORMAT_PATTERN);

			return dateFormat.parse(_document.getDate(name));
		}
		catch (Exception e) {
			return null;
		}
	}

	private void _setAliases(
		ResultsRankingEntryDisplayContext resultsRankingEntryDisplayContext) {

		resultsRankingEntryDisplayContext.setAliases(
			StringUtil.merge(
				_document.getStrings("aliases"), StringPool.COMMA_AND_SPACE));
	}

	private void _setDisplayDate(
		ResultsRankingEntryDisplayContext resultsRankingEntryDisplayContext) {

		resultsRankingEntryDisplayContext.setDisplayDate(
			_getDate(Field.DISPLAY_DATE));
	}

	private void _setHiddenResultsCount(
		ResultsRankingEntryDisplayContext resultsRankingEntryDisplayContext) {

		List<Object> values = _document.getValues("hidden_documents");

		resultsRankingEntryDisplayContext.setHiddenResultsCount(
			String.valueOf(values.size()));
	}

	private void _setIndex(
		ResultsRankingEntryDisplayContext resultsRankingEntryDisplayContext) {

		resultsRankingEntryDisplayContext.setIndex(
			_document.getString("index"));
	}

	private void _setKeywords(
		ResultsRankingEntryDisplayContext resultsRankingEntryDisplayContext) {

		resultsRankingEntryDisplayContext.setKeywords(
			_document.getString("keywords"));
	}

	private void _setModifiedDate(
		ResultsRankingEntryDisplayContext resultsRankingEntryDisplayContext) {

		resultsRankingEntryDisplayContext.setModifiedDate(
			_getDate(Field.MODIFIED_DATE));
	}

	private void _setPinnedResultsCount(
		ResultsRankingEntryDisplayContext resultsRankingEntryDisplayContext) {

		List<Object> values = _document.getValues("pinned_documents");

		resultsRankingEntryDisplayContext.setPinnedResultsCount(
			String.valueOf(values.size()));
	}

	private void _setStatus(
		ResultsRankingEntryDisplayContext resultsRankingEntryDisplayContext) {

		resultsRankingEntryDisplayContext.setStatus(
			_document.getString("status"));
	}

	private void _setUid(
		ResultsRankingEntryDisplayContext resultsRankingEntryDisplayContext) {

		resultsRankingEntryDisplayContext.setUid(_document.getString("uid"));
	}

	private static final String _INDEX_DATE_FORMAT_PATTERN = PropsUtil.get(
		PropsKeys.INDEX_DATE_FORMAT_PATTERN);

	private final Document _document;

}