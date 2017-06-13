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

package com.liferay.portal.kernel.search;

import com.liferay.portal.kernel.search.highlight.HighlightUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;

/**
 * @author Brian Wing Shun Chan
 * @author Ryan Park
 * @author Tibor Lipusz
 */
public class Summary {

	public Summary(Locale locale, String title, String content) {
		_locale = locale;
		_title = title;
		_content = content;
	}

	public Summary(String title, String content) {
		this(LocaleThreadLocal.getThemeDisplayLocale(), title, content);
	}

	public String getContent() {
		return getContent(false, _escape);
	}

	public String getContent(boolean highlight, boolean escape) {
		return _getText(_content, highlight, escape);
	}

	public String getHighlightedContent() {
		return getContent(true, _escape);
	}

	public String getHighlightedTitle() {
		return getTitle(true, _escape);
	}

	public Locale getLocale() {
		return _locale;
	}

	public int getMaxContentLength() {
		return _maxContentLength;
	}

	/**
	 * @deprecated As of 7.0.0
	 */
	@Deprecated
	public String[] getQueryTerms() {
		return _queryTerms;
	}

	public String getTitle() {
		return getTitle(false, _escape);
	}

	public String getTitle(boolean highlight, boolean escape) {
		return _getText(_title, highlight, escape);
	}

	public boolean isEscape() {
		return _escape;
	}

	/**
	 * @deprecated As of 7.0.0
	 */
	@Deprecated
	public boolean isHighlight() {
		return _highlight;
	}

	public void setContent(String content) {
		_content = content;
	}

	public void setEscape(boolean escape) {
		_escape = escape;
	}

	/**
	 * @deprecated As of 7.0.0
	 */
	@Deprecated
	public void setHighlight(boolean highlight) {
		_highlight = highlight;
	}

	public void setLocale(Locale locale) {
		_locale = locale;
	}

	public void setMaxContentLength(int maxContentLength) {
		_maxContentLength = maxContentLength;
	}

	/**
	 * @deprecated As of 7.0.0
	 */
	@Deprecated
	public void setQueryTerms(String[] queryTerms) {
		if (ArrayUtil.isEmpty(queryTerms)) {
			return;
		}

		_queryTerms = queryTerms;
	}

	public void setTitle(String title) {
		_title = title;
	}

	private String _getText(String text, boolean highlight, boolean escape) {
		if (Validator.isNull(text)) {
			return StringPool.BLANK;
		}

		if (!highlight || !_isHighlightSnippet(text)) {
			if (!highlight) {
				text = _removeHighlightTags(text);
			}

			if (escape) {
				text = HtmlUtil.escape(text);
			}

			if ((_maxContentLength <= 0) ||
				(text.length() <= _maxContentLength)) {

				return text;
			}

			return StringUtil.shorten(text, _maxContentLength);
		}

		text = _replaceHighlightTags(text);

		if (escape) {
			text = HtmlUtil.escape(text);
		}

		return StringUtil.replace(
			text, _ESCAPE_SAFE_HIGHLIGHTS, HighlightUtil.HIGHLIGHTS);
	}

	private boolean _isHighlightSnippet(String text) {
		if (StringUtil.count(text, HighlightUtil.HIGHLIGHT_TAG_OPEN) > 0) {
			return true;
		}

		return false;
	}

	private String _removeHighlightTags(String text) {
		text = StringUtil.replace(
			text, HighlightUtil.HIGHLIGHT_TAG_OPEN, StringPool.BLANK);

		text = StringUtil.replace(
			text, HighlightUtil.HIGHLIGHT_TAG_CLOSE, StringPool.BLANK);

		return text;
	}

	private String _replaceHighlightTags(String text) {
		text = StringUtil.replace(
			text, HighlightUtil.HIGHLIGHT_TAG_OPEN, _ESCAPE_SAFE_HIGHLIGHTS[0]);

		text = StringUtil.replace(
			text, HighlightUtil.HIGHLIGHT_TAG_CLOSE,
			_ESCAPE_SAFE_HIGHLIGHTS[1]);

		return text;
	}

	private static final String[] _ESCAPE_SAFE_HIGHLIGHTS =
		{"[@HIGHLIGHT1@]", "[@HIGHLIGHT2@]"};

	private String _content;
	private boolean _escape = true;

	/**
	 * @deprecated As of 7.0.0
	 */
	@Deprecated
	private boolean _highlight;

	private Locale _locale;
	private int _maxContentLength;

	/**
	 * @deprecated As of 7.0.0
	 */
	@Deprecated
	private String[] _queryTerms;

	private String _title;

}