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

package com.liferay.portal.search.elasticsearch6.internal.connection;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.search.elasticsearch6.configuration.ElasticsearchConfiguration;
import com.liferay.portal.search.elasticsearch6.internal.index.IndexFactory;
import com.liferay.portal.search.elasticsearch6.settings.SettingsContributor;
import com.liferay.portal.search.elasticsearch6.settings.XPackSecuritySettings;

import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.KeyStore;

import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Michael C. Han
 */
@Component(
	configurationPid = "com.liferay.portal.search.elasticsearch6.configuration.ElasticsearchConfiguration",
	immediate = true, property = "operation.mode=REMOTE",
	service = ElasticsearchConnection.class
)
public class RemoteElasticsearchConnection extends BaseElasticsearchConnection {

	@Override
	public OperationMode getOperationMode() {
		return OperationMode.REMOTE;
	}

	@Override
	@Reference(unbind = "-")
	public void setIndexFactory(IndexFactory indexFactory) {
		super.setIndexFactory(indexFactory);
	}

	@Activate
	protected void activate(Map<String, Object> properties) {
		replaceElasticsearchConfiguration(properties);
	}

	@Override
	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(operation.mode=REMOTE)"
	)
	protected void addSettingsContributor(
		SettingsContributor settingsContributor) {

		super.addSettingsContributor(settingsContributor);
	}

	protected RestHighLevelClient createRestHighLevelClient() {
		String[] networkHostAddresses =
			elasticsearchConfiguration.networkHostAddresses();

		HttpHost[] httpHosts = new HttpHost[networkHostAddresses.length];

		for (int i = 0; i < networkHostAddresses.length; i++) {
			httpHosts[i] = HttpHost.create(networkHostAddresses[i]);
		}

		RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);

		if ((xPackSecuritySettings != null) &&
			xPackSecuritySettings.requiresXPackSecurity()) {

			Settings.Builder builder = settingsBuilder.getBuilder();

			String usernamePassword = builder.get("xpack.security.user");

			CredentialsProvider credentialsProvider =
				new BasicCredentialsProvider();

			credentialsProvider.setCredentials(
				AuthScope.ANY,
				new UsernamePasswordCredentials(usernamePassword));

			String keyStoreFilePath = builder.get("xpack.ssl.keystore.path");

			Path keyStorePath = Paths.get(keyStoreFilePath);

			SSLContext sslContext;

			try (InputStream is = Files.newInputStream(keyStorePath)) {
				KeyStore truststore = KeyStore.getInstance("jks");

				String keyStorePass = builder.get(
					"xpack.ssl.keystore.password");

				truststore.load(is, keyStorePass.toCharArray());

				SSLContextBuilder sslBuilder = SSLContexts.custom();

				sslBuilder.loadTrustMaterial(truststore, null);

				sslContext = sslBuilder.build();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}

			restClientBuilder.setHttpClientConfigCallback(
				new RestClientBuilder.HttpClientConfigCallback() {

					@Override
					public HttpAsyncClientBuilder customizeHttpClient(
						HttpAsyncClientBuilder httpClientBuilder) {

						httpClientBuilder.setSSLContext(sslContext);

						return httpClientBuilder.setDefaultCredentialsProvider(
							credentialsProvider);
					}

				});
		}

		RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
			restClientBuilder);

		return restHighLevelClient;
	}

	@Deactivate
	protected void deactivate(Map<String, Object> properties) {
		close();
	}

	@Override
	protected void loadConfigurations() {
		return;
	}

	@Modified
	protected synchronized void modified(Map<String, Object> properties) {
		replaceElasticsearchConfiguration(properties);

		if (isConnected()) {
			close();
		}

		if (!isConnected() &&
			(elasticsearchConfiguration.operationMode() ==
				com.liferay.portal.search.elasticsearch6.configuration.
					OperationMode.REMOTE)) {

			connect();
		}
	}

	@Override
	protected void removeSettingsContributor(
		SettingsContributor settingsContributor) {

		super.removeSettingsContributor(settingsContributor);
	}

	protected void replaceElasticsearchConfiguration(
		Map<String, Object> properties) {

		elasticsearchConfiguration = ConfigurableUtil.createConfigurable(
			ElasticsearchConfiguration.class, properties);
	}

	@Reference
	protected Props props;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	protected volatile XPackSecuritySettings xPackSecuritySettings;

}