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

package com.liferay.dynamic.data.lists.internal.search;

import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.service.DDLRecordSetLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ResourceBundleLoader;
import com.liferay.portal.kernel.util.ResourceBundleLoaderUtil;
import com.liferay.portal.search.spi.model.result.contributor.ModelSummaryContributor;

import java.util.Locale;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Raymond Augé
 * @author Zsigmond Rab
 * @author Hugo Huijser
 * @author Marco Leo
 */
@Component(
	immediate = true,
	property = "indexer.class.name=com.liferay.dynamic.data.lists.model.DDLRecord",
	service = ModelSummaryContributor.class
)
public class DDLRecordModelSummaryContributor
	implements ModelSummaryContributor {

	@Override
	public Summary getSummary(
		Document document, Locale locale, String snippet) {

		long recordSetId = GetterUtil.getLong(document.get("recordSetId"));

		String prefix = Field.SNIPPET + StringPool.UNDERLINE;

		String title = getTitle(recordSetId, locale);

		String content = document.get(prefix + null, null);

		Summary summary = new Summary(title, content);

		summary.setMaxContentLength(200);

		summary.setTitle(title);

		return summary;
	}

	protected ResourceBundle getResourceBundle(Locale defaultLocale) {
		ResourceBundleLoader portalResourceBundleLoader =
			ResourceBundleLoaderUtil.getPortalResourceBundleLoader();

		return portalResourceBundleLoader.loadResourceBundle(defaultLocale);
	}

	protected String getTitle(long recordSetId, Locale locale) {
		try {
			DDLRecordSet recordSet = ddlRecordSetLocalService.getRecordSet(
				recordSetId);

			DDMStructure ddmStructure = recordSet.getDDMStructure();

			String ddmStructureName = ddmStructure.getName(locale);

			String recordSetName = recordSet.getName(locale);

			return LanguageUtil.format(
				locale, "new-x-for-list-x",
				new Object[] {ddmStructureName, recordSetName}, false);
		}
		catch (Exception e) {
			_log.error(e, e);
		}

		return StringPool.BLANK;
	}

	@Reference
	protected DDLRecordSetLocalService ddlRecordSetLocalService;

	private static final Log _log = LogFactoryUtil.getLog(
		DDLRecordModelSummaryContributor.class);

}