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

package com.liferay.portal.search.configuration;

import aQute.bnd.annotation.ProviderType;
import aQute.bnd.annotation.metatype.Meta;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Michael C. Han
 */
@ExtendedObjectClassDefinition(category = "search")
@Meta.OCD(
	id = "com.liferay.portal.search.configuration.CustomRelevanceConfiguration",
	localization = "content/Language",
	name = "custom-relevance-configuration-name"
)
@ProviderType
public interface CustomRelevanceConfiguration {

	@Meta.AD(
		name = "asset-list-id", required = false
	)
	public long assetListId(); //used to to add/index field in documents

	@Meta.AD(
		name = "template-asset-id", required = false
	)
	public long templateAssetId(); //used to add/index field in document

	@Meta.AD(
		name = "boost-field-name", required = false, deflt = "boostWords"
	)
	public String boostFieldName(); //used to create query and index field. must match field name in ddl data definition / web content structure.
	// could get field name from asset...

	@Meta.AD(
		name = "boost-factor", required = false, deflt = "100.0f"
	)
	public float boostFactor(); //used to create query

	@Meta.AD(
		name = "boosted-class-types", required = false
	)
	public String[] boostedClassTypes();

	@Meta.AD(
		name = "boost-words", required = false
	)
	public String boostWords();

}