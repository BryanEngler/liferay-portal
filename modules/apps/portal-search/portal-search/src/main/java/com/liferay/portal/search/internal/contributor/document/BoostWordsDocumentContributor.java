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

package com.liferay.portal.search.internal.contributor.document;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentContributor;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.configuration.CustomRelevanceConfiguration;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import java.util.Map;

/**
 * @author Michael C. Han
 */
@Component(
	configurationPid = "com.liferay.portal.search.configuration.CustomRelevanceConfiguration",
	immediate = true, service = DocumentContributor.class)
public class BoostWordsDocumentContributor implements DocumentContributor {

	@Override
	public void contribute(Document document, BaseModel baseModel) {
		String[] boostedClassTypes =
			customRelevanceConfiguration.boostedClassTypes();

		if (ArrayUtil.contains(
			boostedClassTypes, baseModel.getModelClassName())) {
			document.addText(
				customRelevanceConfiguration.boostFieldName(),
				customRelevanceConfiguration.boostWords());
		}
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		customRelevanceConfiguration =
			ConfigurableUtil.createConfigurable(
				CustomRelevanceConfiguration.class, properties);
	}

	protected CustomRelevanceConfiguration customRelevanceConfiguration;
}