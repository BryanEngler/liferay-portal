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

package com.liferay.object.internal.search.spi.model.query.contributor;

import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.ParseException;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.util.RangeParserUtil;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.MatchQuery;
import com.liferay.portal.kernel.search.generic.NestedQuery;
import com.liferay.portal.kernel.search.generic.TermQueryImpl;
import com.liferay.portal.kernel.search.generic.TermRangeQueryImpl;
import com.liferay.portal.kernel.search.generic.WildcardQueryImpl;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.spi.model.query.contributor.KeywordQueryContributor;
import com.liferay.portal.search.spi.model.query.contributor.helper.KeywordQueryContributorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
public class ObjectEntryKeywordQueryContributor
	implements KeywordQueryContributor {

	public ObjectEntryKeywordQueryContributor(
		ObjectFieldLocalService objectFieldLocalService) {

		_objectFieldLocalService = objectFieldLocalService;
	}

	@Override
	public void contribute(
		String keywords, BooleanQuery booleanQuery,
		KeywordQueryContributorHelper keywordQueryContributorHelper) {

		if (Validator.isBlank(keywords)) {
			return;
		}

		SearchContext searchContext =
			keywordQueryContributorHelper.getSearchContext();

		long objectDefinitionId = GetterUtil.getLong(
			searchContext.getAttribute("objectDefinitionId"));

		if (_log.isDebugEnabled()) {
			_log.debug("Object definition ID " + objectDefinitionId);
		}

		if (objectDefinitionId == 0) {
			String className = keywordQueryContributorHelper.getClassName();

			if (className.startsWith(
					"com.liferay.object.model.ObjectDefinition#")) {

				String[] parts = StringUtil.split(className, "#");

				objectDefinitionId = Long.valueOf(parts[1]);
			}
			else {
				return;
			}
		}

		for (String token : _tokenizeKeywords(keywords)) {
			if (!Validator.isBlank(token)) {
				try {
					booleanQuery.add(
						new TermQueryImpl(Field.ENTRY_CLASS_PK, token),
						BooleanClauseOccur.SHOULD);
				}
				catch (ParseException parseException) {
					throw new SystemException(parseException);
				}
			}

			List<ObjectField> objectFields =
				_objectFieldLocalService.getObjectFields(objectDefinitionId);

			for (ObjectField objectField : objectFields) {
				try {
					_contribute(
						token, booleanQuery, keywordQueryContributorHelper,
						objectField);
				}
				catch (ParseException parseException) {
					throw new SystemException(parseException);
				}
			}
		}
	}

	private void _addRangeQuery(
			String token, BooleanQuery booleanQuery, String fieldName,
			String type)
		throws ParseException {

		if (Validator.isBlank(token)) {
			return;
		}

		String[] range = RangeParserUtil.parserRange(token);

		String lowerTerm = range[0];
		String upperTerm = range[1];

		if (!_isValidRange(lowerTerm, type, upperTerm)) {
			return;
		}

		booleanQuery.add(
			new TermRangeQueryImpl(fieldName, lowerTerm, upperTerm, true, true),
			BooleanClauseOccur.MUST);
	}

	private void _contribute(
			String token, BooleanQuery booleanQuery,
			KeywordQueryContributorHelper keywordQueryContributorHelper,
			ObjectField objectField)
		throws ParseException {

		if (!objectField.isIndexed() || Validator.isBlank(token)) {
			return;
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"Add search term ", token, " for object field ",
					objectField.getName()));
		}

		SearchContext searchContext =
			keywordQueryContributorHelper.getSearchContext();

		BooleanQuery nestedBooleanQuery = new BooleanQueryImpl();

		if (objectField.isIndexedAsKeyword()) {
			String lowerCaseKeywords = StringUtil.toLowerCase(token);

			nestedBooleanQuery.add(
				new WildcardQueryImpl(
					"nestedFieldArray.value_keyword",
					lowerCaseKeywords + StringPool.STAR),
				BooleanClauseOccur.MUST);
			nestedBooleanQuery.add(
				new TermQueryImpl(
					"nestedFieldArray.value_keyword", lowerCaseKeywords),
				BooleanClauseOccur.SHOULD);
		}
		else if (Objects.equals(objectField.getType(), "BigDecimal")) {
			_addRangeQuery(
				token, nestedBooleanQuery, "nestedFieldArray.value_double",
				objectField.getType());
		}
		else if (Objects.equals(objectField.getType(), "Blob")) {
			_log.error("Blob type is not indexable");
		}
		else if (Objects.equals(objectField.getType(), "Boolean")) {
			if (StringUtil.equalsIgnoreCase(token, "false") ||
				StringUtil.equalsIgnoreCase(token, "true")) {

				nestedBooleanQuery.add(
					new TermQueryImpl(
						"nestedFieldArray.value_boolean",
						StringUtil.toLowerCase(token)),
					BooleanClauseOccur.MUST);
			}
			else if (StringUtil.equalsIgnoreCase(token, "no") ||
					 StringUtil.equalsIgnoreCase(token, "yes")) {

				nestedBooleanQuery.add(
					new TermQueryImpl(
						"nestedFieldArray.value_keyword",
						StringUtil.toLowerCase(token)),
					BooleanClauseOccur.MUST);
			}
		}
		else if (Objects.equals(objectField.getType(), "Date")) {
			_addRangeQuery(
				token, nestedBooleanQuery, "nestedFieldArray.value_date",
				objectField.getType());
		}
		else if (Objects.equals(objectField.getType(), "Double")) {
			_addRangeQuery(
				token, nestedBooleanQuery, "nestedFieldArray.value_double",
				objectField.getType());
		}
		else if (Objects.equals(objectField.getType(), "Integer")) {
			_addRangeQuery(
				token, nestedBooleanQuery, "nestedFieldArray.value_integer",
				objectField.getType());
		}
		else if (Objects.equals(objectField.getType(), "Long")) {
			_addRangeQuery(
				token, nestedBooleanQuery, "nestedFieldArray.value_long",
				objectField.getType());
		}
		else if (Objects.equals(objectField.getType(), "String")) {
			if (Validator.isBlank(objectField.getIndexedLanguageId())) {
				nestedBooleanQuery.add(
					new MatchQuery("nestedFieldArray.value_text", token),
					BooleanClauseOccur.MUST);
			}
			else if (Objects.equals(
						objectField.getIndexedLanguageId(),
						LocaleUtil.toLanguageId(searchContext.getLocale()))) {

				nestedBooleanQuery.add(
					new MatchQuery(
						"nestedFieldArray.value_" +
							objectField.getIndexedLanguageId(),
						token),
					BooleanClauseOccur.MUST);
			}
		}

		if (nestedBooleanQuery.hasClauses()) {
			BooleanClauseOccur booleanClauseOccur = BooleanClauseOccur.SHOULD;

			if (searchContext.isAndSearch()) {
				booleanClauseOccur = BooleanClauseOccur.MUST;
			}

			booleanQuery.add(
				new NestedQuery("nestedFieldArray", nestedBooleanQuery),
				booleanClauseOccur);

			nestedBooleanQuery.add(
				new TermQueryImpl(
					"nestedFieldArray.fieldName", objectField.getName()),
				BooleanClauseOccur.MUST);
		}
	}

	private boolean _isValidRange(
		String lowerTerm, String type, String upperTerm) {

		if ((lowerTerm == null) || (upperTerm == null)) {
			return false;
		}

		try {
			if (Objects.equals(type, "BigDecimal") ||
				Objects.equals(type, "Double")) {

				Double.valueOf(lowerTerm);
				Double.valueOf(upperTerm);
			}
			else if (Objects.equals(type, "Date")) {
				Matcher lowerTermMatcher = _pattern.matcher(lowerTerm);
				Matcher upperTermMatcher = _pattern.matcher(upperTerm);

				if (!lowerTermMatcher.matches() ||
					!upperTermMatcher.matches()) {

					return false;
				}
			}
			else if (Objects.equals(type, "Integer")) {
				Integer.valueOf(lowerTerm);
				Integer.valueOf(upperTerm);
			}
			else if (Objects.equals(type, "Long")) {
				Long.valueOf(lowerTerm);
				Long.valueOf(upperTerm);
			}
			else {
				return false;
			}
		}
		catch (Exception exception) {
			return false;
		}

		return true;
	}

	private List<String> _tokenizeKeywords(String keywords) {
		KeywordTokenizer keywordTokenizer = new KeywordTokenizer();

		return keywordTokenizer.tokenize(keywords);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryKeywordQueryContributor.class);

	private static final Pattern _pattern = Pattern.compile("\\d{14}");

	private final ObjectFieldLocalService _objectFieldLocalService;

	private class KeywordTokenizer {

		public List<String> tokenize(String keywords) {
			keywords = _normalizeWhitespace(keywords);

			List<String> tokens = new ArrayList<>();

			int[] startAndEnd = getStartAndEnd(keywords);

			tokenize(keywords, tokens, startAndEnd[0], startAndEnd[1]);

			return tokens;
		}

		protected int[] getStartAndEnd(String keywords) {
			int quoteStart = keywords.indexOf(CharPool.QUOTE);
			int rangeStart = keywords.indexOf(CharPool.OPEN_BRACKET);

			if (quoteStart == QueryUtil.ALL_POS) {
				return new int[] {
					rangeStart,
					keywords.indexOf(CharPool.CLOSE_BRACKET, rangeStart + 1)
				};
			}
			else if (rangeStart == QueryUtil.ALL_POS) {
				return new int[] {
					quoteStart, keywords.indexOf(CharPool.QUOTE, quoteStart + 1)
				};
			}
			else if (quoteStart < rangeStart) {
				return new int[] {
					quoteStart, keywords.indexOf(CharPool.QUOTE, quoteStart + 1)
				};
			}
			else {
				return new int[] {
					rangeStart,
					keywords.indexOf(CharPool.CLOSE_BRACKET, rangeStart + 1)
				};
			}
		}

		protected String[] split(String keywords) {
			if (Objects.equals(keywords, StringPool.NULL)) {
				return new String[] {keywords};
			}

			return StringUtil.split(keywords, CharPool.SPACE);
		}

		protected void tokenize(
			String keywords, List<String> tokens, int start, int end) {

			if ((start == QueryUtil.ALL_POS) || (end == QueryUtil.ALL_POS)) {
				keywords = keywords.trim();

				if (!keywords.isEmpty()) {
					tokenizeBySpace(keywords, tokens);
				}

				return;
			}

			String token = keywords.substring(0, start);

			token = token.trim();

			if (!token.isEmpty()) {
				tokenizeBySpace(token, tokens);
			}

			token = keywords.substring(start, end + 1);

			token = token.trim();

			if (!token.isEmpty()) {
				if (StringUtil.startsWith(token, CharPool.QUOTE)) {
					token = StringUtil.unquote(token);
				}

				tokens.add(token);
			}

			if ((end + 1) > keywords.length()) {
				return;
			}

			keywords = keywords.substring(end + 1);

			keywords = keywords.trim();

			if (keywords.isEmpty()) {
				return;
			}

			int[] startAndEnd = getStartAndEnd(keywords);

			tokenize(keywords, tokens, startAndEnd[0], startAndEnd[1]);
		}

		protected void tokenizeBySpace(String keywords, List<String> tokens) {
			String[] keywordTokens = split(keywords);

			for (String keywordToken : keywordTokens) {
				String token = keywordToken.trim();

				if (!token.isEmpty()) {
					tokens.add(token);
				}
			}
		}

		private String _normalizeWhitespace(String keywords) {
			return StringUtil.replace(
				keywords, _IDEOGRAPHIC_SPACE, CharPool.SPACE);
		}

		private static final char _IDEOGRAPHIC_SPACE = '\u3000';

	}

}