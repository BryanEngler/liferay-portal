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

package com.liferay.object.model.impl;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.TextFormatter;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
public class ObjectDefinitionImpl extends ObjectDefinitionBaseImpl {

	public ObjectDefinitionImpl() {
	}

	@Override
	public String getDBPrimaryKeyColumnName() {
		return getPrimaryKeyColumnName() + StringPool.UNDERLINE;
	}

	@Override
	public String getDBTableName() {
		return StringBundler.concat(
			"O_", getCompanyId(), StringPool.UNDERLINE, getName());
	}

	@Override
	public String getPrimaryKeyColumnName() {
		return TextFormatter.format(getName() + "Id", TextFormatter.I);
	}

}