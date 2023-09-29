/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.index.creation;

import com.liferay.petra.io.Deserializer;
import com.liferay.petra.io.Serializer;
import com.liferay.portal.events.StartupHelperUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationWrapper;
import com.liferay.portal.search.index.creation.IndexNamePrefixIndexCreationConditionContributor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.nio.ByteBuffer;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(service = IndexNamePrefixIndexCreationConditionContributor.class)
public class IndexNamePrefixIndexCreationConditionContributorImpl
	implements IndexNamePrefixIndexCreationConditionContributor {

	@Override
	public boolean shouldCreateIndexes() {
		File dataFile = _bundleContext.getDataFile(
			"elasticsearch_configuration_index_name_prefix.data");

		if (dataFile.exists() && !StartupHelperUtil.isDBNew()) {
			try {
				Deserializer deserializer = new Deserializer(
					ByteBuffer.wrap(FileUtil.getBytes(dataFile)));

				String indexNamePrefix = deserializer.readString();

				if (indexNamePrefix.equals(
						_elasticsearchConfigurationWrapper.indexNamePrefix())) {

					return false;
				}
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to read Elasticsearch configuration",
						exception);
				}
			}
		}

		Serializer serializer = new Serializer();

		serializer.writeString(
			_elasticsearchConfigurationWrapper.indexNamePrefix());

		try (OutputStream outputStream = new FileOutputStream(dataFile)) {
			serializer.writeTo(outputStream);
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to update Elasticsearch configuration", exception);
			}
		}

		return true;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		IndexNamePrefixIndexCreationConditionContributorImpl.class);

	private BundleContext _bundleContext;

	@Reference
	private volatile ElasticsearchConfigurationWrapper
		_elasticsearchConfigurationWrapper;

}