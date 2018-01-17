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

package com.liferay.portal.search.elasticsearch6.xpack.security.internal.settings;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.search.elasticsearch.settings.BaseSettingsContributor;
import com.liferay.portal.search.elasticsearch.settings.ClientSettingsHelper;
import com.liferay.portal.search.elasticsearch.settings.SettingsContributor;
import com.liferay.portal.search.elasticsearch6.xpack.security.configuration.XpackSecurityConfiguration;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Bryan Engler
 */
@Component(
	configurationPid = "com.liferay.portal.search.elasticsearch6.xpack.security.configuration.XpackSecurityConfiguration",
	immediate = true, property = {"operation.mode=REMOTE"},
	service = SettingsContributor.class
)
public class XpackSecurityRemoteSettingsContributor
	extends BaseSettingsContributor {

	public XpackSecurityRemoteSettingsContributor() {
		super(1);
	}

	@Override
	public void populate(ClientSettingsHelper clientSettingsHelper) {
		if (!xpackSecurityConfiguration.requiresAuthentication()) {
			return;
		}

		configureAuthentication(clientSettingsHelper);
		configureSSL(clientSettingsHelper);
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		xpackSecurityConfiguration = ConfigurableUtil.createConfigurable(
			XpackSecurityConfiguration.class, properties);
	}

	protected void configureAuthentication(
		ClientSettingsHelper clientSettingsHelper) {

		String user =
			xpackSecurityConfiguration.password() + ":" +
				xpackSecurityConfiguration.username();

		clientSettingsHelper.put("xpack.security.user", user);
	}

	protected void configurePEMPaths(
		ClientSettingsHelper clientSettingsHelper) {

		clientSettingsHelper.putArray(
			"xpack.ssl.certificate_authorities",
			xpackSecurityConfiguration.sslCertificateAuthoritiesPaths());
		clientSettingsHelper.put(
			"xpack.ssl.certificate",
			xpackSecurityConfiguration.sslCertificatePath());
		clientSettingsHelper.put(
			"xpack.ssl.key", xpackSecurityConfiguration.sslKeyPath());
	}

	protected void configurePKCSPaths(
		ClientSettingsHelper clientSettingsHelper) {

		clientSettingsHelper.put(
			"xpack.ssl.keystore.password",
			xpackSecurityConfiguration.sslKeystorePassword());
		clientSettingsHelper.put(
			"xpack.ssl.keystore.path",
			xpackSecurityConfiguration.sslKeystorePath());
		clientSettingsHelper.put(
			"xpack.ssl.truststore.password",
			xpackSecurityConfiguration.sslTruststorePassword());
		clientSettingsHelper.put(
			"xpack.ssl.truststore.path",
			xpackSecurityConfiguration.sslTruststorePath());
	}

	protected void configureSSL(ClientSettingsHelper clientSettingsHelper) {
		if (!xpackSecurityConfiguration.transportSSLEnabled()) {
			return;
		}

		clientSettingsHelper.put(
			"xpack.security.transport.ssl.enabled", "true");
		clientSettingsHelper.put(
			"xpack.security.transport.ssl.verification_mode",
			xpackSecurityConfiguration.transportSSLVerificationMode());

		String certificateFormat =
			xpackSecurityConfiguration.certificateFormat();

		if (certificateFormat.equals("PKCS#12")) {
			configurePKCSPaths(clientSettingsHelper);
		}
		else {
			configurePEMPaths(clientSettingsHelper);
		}
	}

	protected volatile XpackSecurityConfiguration xpackSecurityConfiguration;

}