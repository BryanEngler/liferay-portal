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

package com.liferay.portal.search.web.components.bar.classic.portlet;

import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.portlet.PortletRequestThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.request.params.SearchParametersConfiguration;

import java.util.Optional;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

	protected String addQParameter(
		String url, ActionRequest actionRequest,
		SearchParametersConfiguration parametersConfiguration) {

		String qParameterName = parametersConfiguration.getQParameterName();

		String q = ParamUtil.getString(actionRequest, qParameterName);

		return HttpUtil.addParameter(url, qParameterName, q);
	}

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		if (SessionErrors.isEmpty(actionRequest)) {
			SessionMessages.add(
				actionRequest, portal.getPortletId(actionRequest),
				SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_SUCCESS_MESSAGE);
		}

		SearchBarClassicConfigurationImpl searchBarClassicConfigurationImpl =
			new SearchBarClassicConfigurationImpl(
				actionRequest.getPreferences());

		String redirectURL = getRedirectURL(
			actionRequest, searchBarClassicConfigurationImpl);

		redirectURL = addQParameter(
			redirectURL, actionRequest, searchBarClassicConfigurationImpl);

		actionResponse.sendRedirect(portal.escapeRedirect(redirectURL));
	}

	protected String getFriendlyURL(ThemeDisplay themeDisplay) {
		Layout layout = themeDisplay.getLayout();

		return layout.getFriendlyURL(themeDisplay.getLocale());
	}

	protected String getRedirectURL(
		ActionRequest actionRequest,
		SearchBarClassicConfiguration searchBarClassicConfiguration) {

		ThemeDisplay themeDisplay = getThemeDisplay(actionRequest);

		String url = themeDisplay.getURLCurrent();

		String page = getFriendlyURL(themeDisplay);

		String path = url.substring(0, url.indexOf(page));

		Optional<String> destinationOptional =
			searchBarClassicConfiguration.getDestination();

		String destination = destinationOptional.orElse(page);

		return path + '/' + unslash(destination);
	}

	protected ThemeDisplay getThemeDisplay(ActionRequest actionRequest) {
		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(actionRequest);

		return themeDisplaySupplier.getThemeDisplay();
	}

	protected String unslash(String string) {
		if (string.charAt(0) == CharPool.SLASH) {
			return string.substring(1);
		}

		return string;
	}

	@Reference
	protected Portal portal;

}