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

package com.liferay.commerce.elasticsearch7.internal.index.settings.contributor;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.search.spi.settings.IndexSettingsContributor;
import com.liferay.portal.search.spi.settings.IndexSettingsHelper;
import com.liferay.portal.search.spi.settings.TypeMappingsHelper;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * @author Marco Leo
 */
@Component(
	enabled = false, immediate = true, service = IndexSettingsContributor.class
)
public class CommerceIndexSettingsContributor
	implements IndexSettingsContributor {

	@Override
	public void contribute(
		String indexName, TypeMappingsHelper typeMappingsHelper) {

		if (_log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"Please verify that includeCommerceMappings is set to ",
					"\"true\" in com.liferay.portal.search.elasticsearch7.",
					"configuration.ElasticsearchConfiguration"));
		}
	}

	@Override
	public void populate(IndexSettingsHelper indexSettingsHelper) {
	}

	@Activate
	protected void activate() {
		if (_log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"If includeCommerceMappings was not set to \"true\" in ",
					"com.liferay.portal.search.elasticsearch7.configuration.",
					"ElasticsearchConfiguration when the company indexes were ",
					"created, then set it to \"true\" and run a full reindex"));
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceIndexSettingsContributor.class);

}