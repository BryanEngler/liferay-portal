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

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Wade Cao
 */
public class ResultsRankingDisplayContext {

	public ResultsRankingDisplayContext(Document document, Locale locale) {
		_keywords = document.get("keywords");

		_setDisplayDate(document);
		_setHidden(document);
		_setModifiedDate(document);
		_setPinned(document);
		_status = document.get("status");
		_uid = document.get("uid");
	}

	public Date getDisplayDate() {
		return _displayDate;
	}

	public String getHidden() {
		return _hidden;
	}

	public String getKeywords() {
		return _keywords;
	}

	public Date getModifiedDate() {
		return _modifiedDate;
	}

	public String getPinned() {
		return _pinned;
	}
	
	public String getStatus() {
		return _status;
	}

	public String getUid() {
		return _uid;
	}

	protected static Optional<String> maybe(String s) {
		s = StringUtil.trim(s);

		if (Validator.isBlank(s)) {
			return Optional.empty();
		}

		return Optional.of(s);
	}

	protected static Optional<String[]> maybe(String[] s) {
		if ((s == null) || (s.length == 0)) {
			return Optional.empty();
		}

		return Optional.of(s);
	}

	protected Date parseDateStringFieldValue(String dateStringFieldValue) {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

		try {
			return dateFormat.parse(dateStringFieldValue);
		}
		catch (Exception e) {
			throw new IllegalArgumentException(
				"Unable to parse date string: " + dateStringFieldValue, e);
		}
	}

	protected int parseIntStringFieldValue(String[] intStringFieldValue) {
		try {
			int totalValue = 0;

			for (String s : intStringFieldValue) {
				totalValue += Integer.parseInt(s);
			}

			return totalValue;
		}
		catch (Exception e) {
			throw new IllegalArgumentException(
				"Unable to parse int string: " + intStringFieldValue, e);
		}
	}

	private void _setDisplayDate(Document document) {
		Optional<String> dateStringOptional = maybe(
			document.get(Field.DISPLAY_DATE));

		Optional<Date> dateOptional = dateStringOptional.map(
			this::parseDateStringFieldValue);

		dateOptional.ifPresent(
			date -> {
				_displayDate = date;
			});
	}

	private void _setHidden(Document document) {
		int hiddenInt = 0;
		Field field = document.getField("hidden_documents");

		if (field == null) {
			_hidden = String.valueOf(hiddenInt);

			return;
		}

		Optional<String[]> intStringOptional = maybe(field.getValues());

		Optional<Integer> intOptional = intStringOptional.map(
			this::parseIntStringFieldValue);

		intOptional.ifPresent(
			integer -> {
				_hidden = String.valueOf(integer);
			});
	}

	private void _setModifiedDate(Document document) {
		Optional<String> dateStringOptional = maybe(
			document.get(Field.MODIFIED_DATE));

		Optional<Date> dateOptional = dateStringOptional.map(
			this::parseDateStringFieldValue);

		dateOptional.ifPresent(
			date -> {
				_modifiedDate = date;
			});
	}

	private void _setPinned(Document document) {
		int pinnedInt = 0;
		Field field = document.getField("pinned_documents");

		if (field == null) {
			_pinned = String.valueOf(pinnedInt);

			return;
		}

		Optional<String[]> intStringOptional = maybe(field.getValues());

		Optional<Integer> intOptional = intStringOptional.map(
			this::parseIntStringFieldValue);

		intOptional.ifPresent(
			integer -> {
				_pinned = String.valueOf(integer);
			});
	}

	private Date _displayDate;
	private String _hidden;
	private final String _keywords;
	private Date _modifiedDate;
	private String _pinned;
	private String _status;
	private final String _uid;

}