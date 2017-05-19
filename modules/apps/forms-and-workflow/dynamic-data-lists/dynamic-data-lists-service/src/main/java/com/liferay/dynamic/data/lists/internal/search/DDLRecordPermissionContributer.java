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

import com.liferay.dynamic.data.lists.model.DDLRecord;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.portal.kernel.search.SearchQueryPermissionFilterClassNameContributer;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Bryan Engler
 */
@Component(
	immediate = true,
	service = SearchQueryPermissionFilterClassNameContributer.class
)
public class DDLRecordPermissionContributer
	implements SearchQueryPermissionFilterClassNameContributer {

	@Override
	public Optional<String> contribute(String className) {
		if (className.equals(DDLRecord.class.getName())) {
			return Optional.of(DDLRecordSet.class.getName());
		}
		else {
			return Optional.empty();
		}
	}

}