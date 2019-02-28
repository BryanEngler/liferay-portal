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

package com.liferay.trash.internal.security.permission.resource;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.trash.TrashHandler;
import com.liferay.portal.kernel.trash.TrashHandlerRegistryUtil;
import com.liferay.trash.model.TrashEntry;

/**
 * @author Luan Maoski
 */
public class TrashPermissionImpl implements TrashPermission {

	@Override
	public void check(
			PermissionChecker permissionChecker, long entryId, String actionId)
		throws PrincipalException {

		if (!contains(permissionChecker, entryId, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, TrashEntry.class.getName(), entryId,
				actionId);
		}
	}

	@Override
	public boolean contains(
		PermissionChecker permissionChecker, long entryId, String actionId) {

		TrashHandler trashHandler = TrashHandlerRegistryUtil.getTrashHandler(
			TrashEntry.class.getName());

		try {
			return trashHandler.hasTrashPermission(
				permissionChecker, 0, entryId, actionId);
		}
		catch (PortalException pe) {
			throw new SystemException(pe);
		}
	}

}