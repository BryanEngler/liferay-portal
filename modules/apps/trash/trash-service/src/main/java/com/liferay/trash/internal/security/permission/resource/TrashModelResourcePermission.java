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
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.trash.model.TrashEntry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luan Maoski
 */
@Component(
	immediate = true,
	property = "model.class.name=com.liferay.trash.model.TrashEntry",
	service = ModelResourcePermission.class
)
public class TrashModelResourcePermission
	implements ModelResourcePermission<TrashEntry> {

	@Override
	public void check(
			PermissionChecker permissionChecker, long entryId, String actionId)
		throws PortalException {

		trashPermission.check(permissionChecker, entryId, actionId);
	}

	@Override
	public void check(
			PermissionChecker permissionChecker, TrashEntry model,
			String actionId)
		throws PortalException {

		trashPermission.check(permissionChecker, model.getEntryId(), actionId);
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, long entryId, String actionId)
		throws PortalException {

		return trashPermission.contains(permissionChecker, entryId, actionId);
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, TrashEntry model,
			String actionId)
		throws PortalException {

		return trashPermission.contains(
			permissionChecker, model.getEntryId(), actionId);
	}

	@Override
	public String getModelName() {
		return TrashEntry.class.getName();
	}

	@Override
	public PortletResourcePermission getPortletResourcePermission() {
		return null;
	}

	@Reference
	protected TrashPermission trashPermission;

}