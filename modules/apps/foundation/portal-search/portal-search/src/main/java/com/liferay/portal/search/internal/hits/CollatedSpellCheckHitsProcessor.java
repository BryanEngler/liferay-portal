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

package com.liferay.portal.search.internal.hits;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.IndexSearcherHelperUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.hits.HitsProcessor;
import com.liferay.portal.kernel.util.StringUtil;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 * @author Josef Sustacek
 */
@Component(
	immediate = true, property = {"sort.order=0"}, service = HitsProcessor.class
)
public class CollatedSpellCheckHitsProcessor implements HitsProcessor {

	@Override
	public boolean process(SearchContext searchContext, Hits hits)
		throws SearchException {

		QueryConfig queryConfig = searchContext.getQueryConfig();

		if (!queryConfig.isCollatedSpellCheckResultEnabled()) {
			return true;
		}

		int collatedSpellCheckResultScoresThreshold =
			queryConfig.getCollatedSpellCheckResultScoresThreshold();

		if (hits.getLength() >= collatedSpellCheckResultScoresThreshold) {
			return true;
		}

		String collatedKeywords = IndexSearcherHelperUtil.spellCheckKeywords(
			searchContext);

		String keywords = searchContext.getKeywords();

		if ((keywords.charAt(0) == CharPool.APOSTROPHE) &&
			(keywords.charAt(keywords.length() - 1) == CharPool.APOSTROPHE)) {

			collatedKeywords = StringUtil.quote(collatedKeywords);
		}
		else if ((keywords.charAt(0) == CharPool.QUOTE) &&
				 (keywords.charAt(keywords.length() - 1) == CharPool.QUOTE)) {

			collatedKeywords = StringUtil.quote(
				collatedKeywords, CharPool.QUOTE);
		}

		if (collatedKeywords.equals(keywords)) {
			collatedKeywords = StringPool.BLANK;
		}

		hits.setCollatedSpellCheckResult(collatedKeywords);

		return true;
	}

}