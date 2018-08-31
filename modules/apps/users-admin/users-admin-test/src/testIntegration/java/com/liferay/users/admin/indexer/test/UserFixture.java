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

package com.liferay.users.admin.indexer.test;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.service.test.ServiceTestUtil;

import java.util.List;

/**
 * @author Luan Maoski
 */
public class UserFixture {

	public UserFixture(
		UserLocalService userLocalService, List<Group> groups,
		List<User> users) {

		_userLocalService = userLocalService;
		_groups = groups;
		_users = users;
	}

	public Group addGroup() throws Exception {
		Group group = GroupTestUtil.addGroup();

		_groups.add(group);

		return group;
	}

	public User createAnUser(
			String firstName, String lastName, String screenName)
		throws Exception, PortalException {

		try {
			User user = UserTestUtil.addUser();

			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setScreenName(screenName);

			user = _userLocalService.updateUser(user);

			_users.add(user);

			return user;
		}
		catch (PortalException e)
		{
			throw new RuntimeException(e);
		}
	}

	public ServiceContext getServiceContext() throws Exception {
		return ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), getUserId());
	}

	public void setGroup(Group group) {
		_group = group;
	}

	public void setUp() throws Exception {
		ServiceTestUtil.setUser(TestPropsValues.getUser());

		CompanyThreadLocal.setCompanyId(TestPropsValues.getCompanyId());
	}

	protected long getUserId() throws Exception {
		return TestPropsValues.getUserId();
	}

	private Group _group;
	private final List<Group> _groups;
	private final UserLocalService _userLocalService;
	private final List<User> _users;

}