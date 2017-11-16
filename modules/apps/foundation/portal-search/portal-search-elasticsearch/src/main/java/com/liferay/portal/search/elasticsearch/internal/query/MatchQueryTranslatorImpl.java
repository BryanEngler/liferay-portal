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

package com.liferay.portal.search.elasticsearch.internal.query;

import com.liferay.portal.kernel.search.generic.MatchQuery;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch.query.MatchQueryTranslator;

import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQuery.ZeroTermsQuery;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, service = MatchQueryTranslator.class)
public class MatchQueryTranslatorImpl
	extends BaseMatchQueryTranslatorImpl implements MatchQueryTranslator {

	@Override
	public QueryBuilder translate(MatchQuery matchQuery) {
		MatchQuery.Type type = matchQuery.getType();
		String value = matchQuery.getValue();

		if (value.startsWith(StringPool.QUOTE) &&
			value.endsWith(StringPool.QUOTE)) {

			type = MatchQuery.Type.PHRASE;

			value = StringUtil.unquote(value);

			if (value.endsWith(StringPool.STAR)) {
				type = MatchQuery.Type.PHRASE_PREFIX;
			}
		}

		if ((type == null) || (type == MatchQuery.Type.BOOLEAN)) {
			return translateMatchQuery(matchQuery);
		}
		else if (type == MatchQuery.Type.PHRASE) {
			return translateMatchPhraseQuery(matchQuery);
		}
		else if (type == MatchQuery.Type.PHRASE_PREFIX) {
			return translateMatchPhrasePrefixQuery(matchQuery);
		}

		throw new IllegalArgumentException("Invalid match query type: " + type);
	}

	public MatchPhrasePrefixQueryBuilder translateMatchPhrasePrefixQuery(
		MatchQuery matchQuery) {

		MatchPhrasePrefixQueryBuilder matchPhrasePrefixQueryBuilder =
			QueryBuilders.matchPhrasePrefixQuery(
				matchQuery.getField(), matchQuery.getValue());

		if (Validator.isNotNull(matchQuery.getAnalyzer())) {
			matchPhrasePrefixQueryBuilder.analyzer(matchQuery.getAnalyzer());
		}

		if (matchQuery.getMaxExpansions() != null) {
			matchPhrasePrefixQueryBuilder.maxExpansions(
				matchQuery.getMaxExpansions());
		}

		if (matchQuery.getSlop() != null) {
			matchPhrasePrefixQueryBuilder.slop(matchQuery.getSlop());
		}

		if (!matchQuery.isDefaultBoost()) {
			matchPhrasePrefixQueryBuilder.boost(matchQuery.getBoost());
		}

		return matchPhrasePrefixQueryBuilder;
	}

	public MatchPhraseQueryBuilder translateMatchPhraseQuery(
		MatchQuery matchQuery) {

		MatchPhraseQueryBuilder matchPhraseQueryBuilder =
			QueryBuilders.matchPhraseQuery(
				matchQuery.getField(), matchQuery.getValue());

		if (Validator.isNotNull(matchQuery.getAnalyzer())) {
			matchPhraseQueryBuilder.analyzer(matchQuery.getAnalyzer());
		}

		if (matchQuery.getSlop() != null) {
			matchPhraseQueryBuilder.slop(matchQuery.getSlop());
		}

		if (!matchQuery.isDefaultBoost()) {
			matchPhraseQueryBuilder.boost(matchQuery.getBoost());
		}

		return matchPhraseQueryBuilder;
	}

	public MatchQueryBuilder translateMatchQuery(MatchQuery matchQuery) {
		MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(
			matchQuery.getField(), matchQuery.getValue());

		if (Validator.isNotNull(matchQuery.getAnalyzer())) {
			matchQueryBuilder.analyzer(matchQuery.getAnalyzer());
		}

		if (matchQuery.getCutOffFrequency() != null) {
			matchQueryBuilder.cutoffFrequency(matchQuery.getCutOffFrequency());
		}

		if (matchQuery.getFuzziness() != null) {
			matchQueryBuilder.fuzziness(
				Fuzziness.build(matchQuery.getFuzziness()));
		}

		if (matchQuery.getFuzzyRewriteMethod() != null) {
			String matchQueryFuzzyRewrite = translate(
				matchQuery.getFuzzyRewriteMethod());

			matchQueryBuilder.fuzzyRewrite(matchQueryFuzzyRewrite);
		}

		if (matchQuery.getMaxExpansions() != null) {
			matchQueryBuilder.maxExpansions(matchQuery.getMaxExpansions());
		}

		if (Validator.isNotNull(matchQuery.getMinShouldMatch())) {
			matchQueryBuilder.minimumShouldMatch(
				matchQuery.getMinShouldMatch());
		}

		if (matchQuery.getOperator() != null) {
			Operator operator = translate(matchQuery.getOperator());

			matchQueryBuilder.operator(operator);
		}

		if (matchQuery.getPrefixLength() != null) {
			matchQueryBuilder.prefixLength(matchQuery.getPrefixLength());
		}

		if (matchQuery.getZeroTermsQuery() != null) {
			ZeroTermsQuery matchQueryBuilderZeroTermsQuery = translate(
				matchQuery.getZeroTermsQuery());

			matchQueryBuilder.zeroTermsQuery(matchQueryBuilderZeroTermsQuery);
		}

		if (!matchQuery.isDefaultBoost()) {
			matchQueryBuilder.boost(matchQuery.getBoost());
		}

		if (matchQuery.isFuzzyTranspositions() != null) {
			matchQueryBuilder.fuzzyTranspositions(
				matchQuery.isFuzzyTranspositions());
		}

		if (matchQuery.isLenient() != null) {
			matchQueryBuilder.lenient(matchQuery.isLenient());
		}

		return matchQueryBuilder;
	}

}