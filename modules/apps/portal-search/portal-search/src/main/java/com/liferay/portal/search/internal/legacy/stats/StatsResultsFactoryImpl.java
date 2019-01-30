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

package com.liferay.portal.search.internal.legacy.stats;

import com.liferay.portal.search.legacy.stats.StatsResultsFactory;
import com.liferay.portal.search.stats.StatsResults;

import org.osgi.service.component.annotations.Component;

/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = StatsResultsFactory.class)
public class StatsResultsFactoryImpl implements StatsResultsFactory {

	@Override
	public com.liferay.portal.kernel.search.StatsResults
		getLegacyStatsResults(StatsResults statsResults) {

		com.liferay.portal.kernel.search.StatsResults legacyStatsResults =
			new com.liferay.portal.kernel.search.StatsResults(
				statsResults.getField());

		legacyStatsResults.setCount(statsResults.getCount());
		legacyStatsResults.setMax(statsResults.getMax());
		legacyStatsResults.setMean(statsResults.getMean());
		legacyStatsResults.setMin(statsResults.getMin());
		legacyStatsResults.setMissing(statsResults.getMissing());
		legacyStatsResults.setStandardDeviation(
			statsResults.getStandardDeviation());
		legacyStatsResults.setSum(statsResults.getSum());
		legacyStatsResults.setSumOfSquares(statsResults.getSumOfSquares());

		return legacyStatsResults;
	}

}