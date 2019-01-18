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

package com.liferay.portal.search.internal.legacy.groupby;

import com.liferay.portal.search.groupby.GroupBy;
import com.liferay.portal.search.legacy.groupby.GroupByFactory;

import org.osgi.service.component.annotations.Component;

/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = GroupByFactory.class)
public class GroupByFactoryImpl implements GroupByFactory {

	@Override
	public GroupBy getGroupBy(
		com.liferay.portal.kernel.search.GroupBy legacyGroupBy) {

		GroupBy groupBy = new GroupBy(legacyGroupBy.getField());

		groupBy.setDocsSize(legacyGroupBy.getSize());
		groupBy.setDocsSorts(legacyGroupBy.getSorts());
		groupBy.setDocsStart(legacyGroupBy.getStart());

		return groupBy;
	}

}