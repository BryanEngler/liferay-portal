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

package com.liferay.announcements.web.lar;

import com.liferay.announcements.kernel.service.persistence.AnnouncementsEntryUtil;
import com.liferay.announcements.web.constants.AnnouncementsPortletKeys;
import com.liferay.exportimport.kernel.lar.DataLevel;
import com.liferay.exportimport.kernel.lar.DefaultConfigurationPortletDataHandler;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.portal.kernel.bean.BeanReference;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;

import javax.portlet.PortletPreferences;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * @author Christopher Kian
 */
@Component(
	property = {"javax.portlet.name=" + AnnouncementsPortletKeys.ANNOUNCEMENTS},
	service = PortletDataHandler.class
)
public class AnnouncementsPortletDataHandler
	extends DefaultConfigurationPortletDataHandler {

	@Override
	public PortletPreferences deleteData(
		PortletDataContext portletDataContext, String portletId,
		PortletPreferences portletPreferences) {

		long groupId = portletDataContext.getGroupId();

		try {
			Group group = _groupLocalService.getGroup(groupId);

			if (group.isSite()) {
				AnnouncementsEntryUtil.removeByC_C(
					group.getClassNameId(), group.getGroupId());
			}
			else {
				AnnouncementsEntryUtil.removeByC_C(
					group.getClassNameId(), group.getClassPK());
			}
		}
		catch (Exception e) {
		}

		return null;
	}

	@Activate
	protected void activate() {
		setDataLevel(DataLevel.PORTLET_INSTANCE);
		setPublishToLiveByDefault(true);
	}

	@BeanReference(type = GroupLocalService.class)
	private GroupLocalService _groupLocalService;

}