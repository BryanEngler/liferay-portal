/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.aggregation.bucket;

import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;

import com.liferay.portal.search.aggregation.bucket.BucketCountThresholds;

/**
 * @author Michael C. Han
 */
public class BucketCountThresholdsTranslator {

	public TermsAggregation.Builder translate(
		BucketCountThresholds bucketCountThresholds) {

		TermsAggregation.Builder termsAggBuilder =
			new TermsAggregation.Builder();

		return termsAggBuilder.minDocCount(
			(int)bucketCountThresholds.getMinDocCount()
		).shardMinDocCount(
			bucketCountThresholds.getShardMinDocCount()
		).size(
			bucketCountThresholds.getRequiredSize()
		).shardSize(
			bucketCountThresholds.getShardSize()
		);
	}

}