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

package com.liferay.portal.search.web.internal.search.bar.classic.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.web.internal.request.params.SearchParametersConfiguration;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;

import org.osgi.service.component.annotations.Component;

/**
 * @author André de Oliveira
 */
@Component(
	property = {
		"javax.portlet.name=" + SearchBarClassicPortletKeys.PORTLET_NAME,
		"mvc.command.name=redirectSearchBar"
	},
	service = MVCActionCommand.class
)
public class SearchBarClassicRedirectMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		PortletPreferences portletPreferences = actionRequest.getPreferences();

		SearchBarClassicConfigurationImpl searchBarClassicConfigurationImpl =
			new SearchBarClassicConfigurationImpl(portletPreferences);

		SearchParametersConfiguration parametersConfiguration =
			searchBarClassicConfigurationImpl;

		String qParameterName = parametersConfiguration.getQParameterName();

		String q = ParamUtil.getString(actionRequest, qParameterName);

		SearchBarClassicConfiguration configuration =
			searchBarClassicConfigurationImpl;

		actionResponse.sendRedirect(
			"/web/guest/" + configuration.getDestination() + "?" +
				qParameterName + "=" + q);
	}

}