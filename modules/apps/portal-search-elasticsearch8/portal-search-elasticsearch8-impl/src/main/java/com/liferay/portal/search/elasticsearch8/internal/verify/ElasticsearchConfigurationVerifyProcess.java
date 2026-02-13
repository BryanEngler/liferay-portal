/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.verify;

import com.liferay.portal.configuration.persistence.upgrade.ConfigurationUpgradeStepFactory;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.search.elasticsearch8.configuration.ElasticsearchConfiguration;
import com.liferay.portal.search.elasticsearch8.configuration.ElasticsearchConnectionConfiguration;
import com.liferay.portal.verify.VerifyProcess;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(property = "initial.deployment=true", service = VerifyProcess.class)
public class ElasticsearchConfigurationVerifyProcess extends VerifyProcess {

	@Override
	protected void doVerify() throws Exception {
		_verifyElasticsearchConfiguration();
		_verifyElasticsearchConnectionConfigurations();
	}

	private void _verifyElasticsearchConfiguration() throws Exception {
		UpgradeStep upgradeStep =
			_configurationUpgradeStepFactory.createUpgradeStep(
				_ELASTICSEARCH_7_CONFIGURATION_CLASS_NAME,
				ElasticsearchConfiguration.class.getName());

		upgradeStep.upgrade();
	}

	private void _verifyElasticsearchConnectionConfigurations()
		throws Exception {

		UpgradeStep upgradeStep =
			_configurationUpgradeStepFactory.createUpgradeStep(
				_ELASTICSEARCH_7_CONNECTION_CONFIGURATION_CLASS_NAME,
				ElasticsearchConnectionConfiguration.class.getName());

		upgradeStep.upgrade();
	}

	private static final String _ELASTICSEARCH_7_CONFIGURATION_CLASS_NAME =
		"com.liferay.portal.search.elasticsearch7.configuration." +
			"ElasticsearchConfiguration";

	private static final String
		_ELASTICSEARCH_7_CONNECTION_CONFIGURATION_CLASS_NAME =
			"com.liferay.portal.search.elasticsearch7.configuration." +
				"ElasticsearchConnectionConfiguration";

	@Reference
	private ConfigurationUpgradeStepFactory _configurationUpgradeStepFactory;

}