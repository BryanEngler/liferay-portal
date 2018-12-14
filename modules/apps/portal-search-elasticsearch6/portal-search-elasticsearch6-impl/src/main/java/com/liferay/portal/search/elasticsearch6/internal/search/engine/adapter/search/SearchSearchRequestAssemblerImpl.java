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

package com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter.search;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.GroupBy;
import com.liferay.portal.kernel.search.Stats;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch6.internal.groupby.GroupByTranslator;
import com.liferay.portal.search.elasticsearch6.internal.highlight.HighlighterTranslator;
import com.liferay.portal.search.elasticsearch6.internal.sort.SortTranslator;
import com.liferay.portal.search.elasticsearch6.internal.stats.StatsTranslator;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;

import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, service = SearchSearchRequestAssembler.class)
public class SearchSearchRequestAssemblerImpl
	implements SearchSearchRequestAssembler {

	@Override
	public void assemble(
		SearchSourceBuilder searchSourceBuilder,
		SearchSearchRequest searchSearchRequest, SearchRequest searchRequest) {

		commonSearchSourceBuilderAssembler.assemble(
			searchSourceBuilder, searchSearchRequest, searchRequest);

		Map<String, Stats> stats = searchSearchRequest.getStats();

		if (!MapUtil.isEmpty(stats)) {
			stats.forEach(
				(statsKey, stat) -> statsTranslator.translate(
					searchSourceBuilder, stat));
		}

		addGroupBy(searchSourceBuilder, searchSearchRequest);

		if (searchSearchRequest.isHighlightEnabled()) {
			highlighterTranslator.translate(
				searchSourceBuilder, searchSearchRequest.getLocale(),
				searchSearchRequest.getHighlightFieldNames(),
				searchSearchRequest.isHighlightRequireFieldMatch(),
				searchSearchRequest.getHighlightFragmentSize(),
				searchSearchRequest.getHighlightSnippetSize(),
				searchSearchRequest.isLuceneSyntax());
		}

		addPagination(
			searchSourceBuilder, searchSearchRequest.getStart(),
			searchSearchRequest.getSize());
		addPreference(searchRequest, searchSearchRequest);
		addSelectedFields(
			searchSourceBuilder, searchSearchRequest.getSelectedFieldNames());

		sortTranslator.translate(
			searchSourceBuilder, searchSearchRequest.getSorts());

		searchSourceBuilder.trackScores(searchSearchRequest.isScoreEnabled());
	}

	protected void addGroupBy(
		SearchSourceBuilder searchSourceBuilder,
		SearchSearchRequest searchSearchRequest) {

		GroupBy groupBy = searchSearchRequest.getGroupBy();

		if (groupBy == null) {
			return;
		}

		groupByTranslator.translate(
			searchSourceBuilder, groupBy, searchSearchRequest.getSorts(),
			searchSearchRequest.getLocale(),
			searchSearchRequest.getSelectedFieldNames(),
			searchSearchRequest.getHighlightFieldNames(),
			searchSearchRequest.isHighlightEnabled(),
			searchSearchRequest.isHighlightRequireFieldMatch(),
			searchSearchRequest.getHighlightFragmentSize(),
			searchSearchRequest.getHighlightSnippetSize(),
			searchSearchRequest.getStart(), searchSearchRequest.getSize());
	}

	protected void addPagination(
		SearchSourceBuilder searchSourceBuilder, int start, int size) {

		searchSourceBuilder.from(start);
		searchSourceBuilder.size(size);
	}

	protected void addPreference(
		SearchRequest searchRequest, SearchSearchRequest searchSearchRequest) {

		String preference = searchSearchRequest.getPreference();

		if (!Validator.isBlank(preference)) {
			searchRequest.preference(preference);
		}
	}

	protected void addSelectedFields(
		SearchSourceBuilder searchSourceBuilder, String[] selectedFieldNames) {

		if (ArrayUtil.isEmpty(selectedFieldNames)) {
			searchSourceBuilder.storedField(StringPool.STAR);
		}
		else {
			searchSourceBuilder.storedFields(
				ListUtil.fromArray(selectedFieldNames));
		}
	}

	@Reference
	protected CommonSearchSourceBuilderAssembler
		commonSearchSourceBuilderAssembler;

	@Reference
	protected GroupByTranslator groupByTranslator;

	@Reference
	protected HighlighterTranslator highlighterTranslator;

	@Reference
	protected SortTranslator sortTranslator;

	@Reference
	protected StatsTranslator statsTranslator;

}