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

import com.liferay.portal.search.legacy.stats.StatsFactory;
import com.liferay.portal.search.stats.Stats;

import org.osgi.service.component.annotations.Component;

/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = StatsFactory.class)
public class StatsFactoryImpl implements StatsFactory {

	@Override
	public Stats getStats(com.liferay.portal.kernel.search.Stats legacyStats) {
		Stats stats = new Stats();

		stats.setCount(legacyStats.isCount());
		stats.setField(legacyStats.getField());
		stats.setMax(legacyStats.isMax());
		stats.setMean(legacyStats.isMean());
		stats.setMin(legacyStats.isMin());
		stats.setMissing(legacyStats.isMissing());
		stats.setStandardDeviation(legacyStats.isStandardDeviation());
		stats.setSum(legacyStats.isSum());
		stats.setSumOfSquares(legacyStats.isSumOfSquares());

		return stats;
	}

}