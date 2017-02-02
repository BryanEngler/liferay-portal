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

package com.liferay.portal.search.elasticsearch.connection;

import com.liferay.portal.kernel.configuration.Filter;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.search.elasticsearch.internal.configuration.ElasticserachConfigurationContainerImpl;
import com.liferay.portal.search.elasticsearch.internal.connection.RemoteElasticsearchConnection;

import java.net.InetSocketAddress;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author André de Oliveira
 */
public class ElasticsearchConnectionManagerTest {

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		resetMockConnections();
		setUpPropsUtil();

		_elasticsearchConnectionManager =
			createElasticsearchConnectionManager();

		_elasticsearchConnectionManager.setElasticsearchConfiguration(
			new HashMap<String, Object>());

		_elasticsearchConnectionManager.setEmbeddedElasticsearchConnection(
			_mockedEmbeddedElasticsearchConnection);
		_elasticsearchConnectionManager.setRemoteElasticsearchConnection(
			_mockedRemoteElasticsearchConnection);
	}

	@Test
	public void testActivateMustNotOpenAnyConnection() {
		HashMap<String, Object> properties = new HashMap<>();

		properties.put("operationMode", OperationMode.EMBEDDED.name());

		_elasticsearchConnectionManager.activate(properties);

		verifyNeverCloseNeverConnect(_mockedEmbeddedElasticsearchConnection);
		verifyNeverCloseNeverConnect(_mockedRemoteElasticsearchConnection);
	}

	@Test
	public void testActivateThenConnect() {
		HashMap<String, Object> properties = new HashMap<>();

		properties.put("operationMode", OperationMode.EMBEDDED.name());

		_elasticsearchConnectionManager.activate(properties);

		_elasticsearchConnectionManager.connect();

		verifyConnectNeverClose(_mockedEmbeddedElasticsearchConnection);
		verifyNeverCloseNeverConnect(_mockedRemoteElasticsearchConnection);
	}

	@Test
	public void testGetClient() {
		modify(OperationMode.EMBEDDED);

		_elasticsearchConnectionManager.getClient();

		Mockito.verify(_mockedEmbeddedElasticsearchConnection).getClient();

		modify(OperationMode.REMOTE);

		_elasticsearchConnectionManager.getClient();

		Mockito.verify(_mockedRemoteElasticsearchConnection).getClient();
	}

	@Test
	public void testGetClientWhenOperationModeNotSet() {
		try {
			_elasticsearchConnectionManager.getClient();

			Assert.fail();
		}
		catch (ElasticsearchConnectionNotInitializedException ecnie) {
		}
	}

	@Test
	public void testModifyTransportAddress() {
		_elasticsearchConnectionManager.setRemoteElasticsearchConnection(
			_remoteElasticsearchConnection);

		_remoteElasticsearchConnection.setElasticsearchConfigurationContainer(
			new ElasticserachConfigurationContainerImpl());

		HashMap<String, Object> properties = new HashMap<>();

		properties.put("operationMode", OperationMode.REMOTE.name());

		_elasticsearchConnectionManager.modified(properties);

		Assert.assertTrue(_remoteElasticsearchConnection.isConnected());

		assertTransportAddress("localhost", 9300);

		properties.put("transportAddresses", "127.0.0.1:9999");

		_elasticsearchConnectionManager.modified(properties);

		Assert.assertTrue(_remoteElasticsearchConnection.isConnected());

		assertTransportAddress("127.0.0.1", 9999);
	}

	@Test
	public void testSetModifiedOperationModeResetsConnection() {
		HashMap<String, Object> properties = new HashMap<>();

		properties.put("operationMode", OperationMode.EMBEDDED.name());

		_elasticsearchConnectionManager.activate(properties);

		resetMockConnections();

		properties.put("operationMode", OperationMode.REMOTE.name());

		_elasticsearchConnectionManager.modified(properties);

		verifyCloseNeverConnect(_mockedEmbeddedElasticsearchConnection);
		verifyConnectNeverClose(_mockedRemoteElasticsearchConnection);
	}

	@Test
	public void testSetOperationModeToUnavailable() {
		_elasticsearchConnectionManager.unsetElasticsearchConnection(
			_mockedRemoteElasticsearchConnection);

		verifyCloseNeverConnect(_mockedRemoteElasticsearchConnection);
		verifyNeverCloseNeverConnect(_mockedEmbeddedElasticsearchConnection);

		resetMockConnections();

		try {
			modify(OperationMode.REMOTE);

			Assert.fail();
		}
		catch (MissingOperationModeException mome) {
			String message = mome.getMessage();

			Assert.assertTrue(
				message.contains(String.valueOf(OperationMode.REMOTE)));
		}

		verifyNeverCloseNeverConnect(_mockedEmbeddedElasticsearchConnection);
		verifyNeverCloseNeverConnect(_mockedRemoteElasticsearchConnection);
	}

	@Test
	public void testSetSameOperationModeResetsConnection() {
		HashMap<String, Object> properties = new HashMap<>();

		properties.put("operationMode", OperationMode.REMOTE.name());

		_elasticsearchConnectionManager.modified(properties);

		resetMockConnections();

		properties.put("operationMode", OperationMode.REMOTE.name());

		_elasticsearchConnectionManager.modified(properties);

		verifyNeverCloseNeverConnect(_mockedEmbeddedElasticsearchConnection);
		verifyConnectAndClose(_mockedRemoteElasticsearchConnection);
	}

	@Test
	public void testToggleOperationMode() {
		modify(OperationMode.EMBEDDED);

		verifyConnectNeverClose(_mockedEmbeddedElasticsearchConnection);
		verifyNeverCloseNeverConnect(_mockedRemoteElasticsearchConnection);

		resetMockConnections();

		modify(OperationMode.REMOTE);

		verifyCloseNeverConnect(_mockedEmbeddedElasticsearchConnection);
		verifyConnectNeverClose(_mockedRemoteElasticsearchConnection);

		resetMockConnections();

		modify(OperationMode.EMBEDDED);

		verifyCloseNeverConnect(_mockedRemoteElasticsearchConnection);
		verifyConnectNeverClose(_mockedEmbeddedElasticsearchConnection);
	}

	@Test
	public void testUnableToCloseOldConnectionUseNewConnectionAnyway() {
		modify(OperationMode.EMBEDDED);

		resetMockConnections();

		Mockito.doThrow(
			IllegalStateException.class
		).when(
			_mockedEmbeddedElasticsearchConnection
		).close();

		modify(OperationMode.REMOTE);

		Assert.assertSame(
			_mockedRemoteElasticsearchConnection,
			_elasticsearchConnectionManager.getElasticsearchConnection());

		verifyCloseNeverConnect(_mockedEmbeddedElasticsearchConnection);
		verifyConnectNeverClose(_mockedRemoteElasticsearchConnection);
	}

	@Test
	public void testUnableToOpenNewConnectionStayWithOldConnection() {
		modify(OperationMode.EMBEDDED);

		resetMockConnections();

		Mockito.doThrow(
			IllegalStateException.class
		).when(
			_mockedRemoteElasticsearchConnection
		).connect();

		try {
			modify(OperationMode.REMOTE);

			Assert.fail();
		}
		catch (IllegalStateException ise) {
		}

		Assert.assertSame(
			_mockedEmbeddedElasticsearchConnection,
			_elasticsearchConnectionManager.getElasticsearchConnection());

		verifyConnectAndClose(_mockedRemoteElasticsearchConnection);
		verifyNeverCloseNeverConnect(_mockedEmbeddedElasticsearchConnection);
	}

	protected static ElasticsearchConnectionManager
		createElasticsearchConnectionManager() {

		return new ElasticsearchConnectionManager() {
			{
				elasticsearchConfigurationContainer =
					new ElasticserachConfigurationContainerImpl();
			}
		};
	}

	protected static RemoteElasticsearchConnection
		createRemoteElasticsearchConnection() {

		return new RemoteElasticsearchConnection() {
			{
				props = new Props() {

					@Override
					public boolean contains(String key) {
						return false;
					}

					@Override
					public String get(String key) {
						return null;
					}

					@Override
					public String get(String key, Filter filter) {
						return null;
					}

					@Override
					public String[] getArray(String key) {
						return new String[0];
					}

					@Override
					public String[] getArray(String key, Filter filter) {
						return new String[0];
					}

					@Override
					public Properties getProperties() {
						return null;
					}

					@Override
					public Properties getProperties(
						String prefix, boolean removePrefix) {

						return null;
					}

				};
			}
		};
	}

	protected void assertTransportAddress(String hostString, int port) {
		TransportClient transportClient =
			(TransportClient)_remoteElasticsearchConnection.getClient();

		List<TransportAddress> transportAddresses =
			transportClient.transportAddresses();

		Assert.assertEquals(1, transportAddresses.size());

		InetSocketTransportAddress inetSocketTransportAddress =
			(InetSocketTransportAddress)transportAddresses.get(0);

		InetSocketAddress inetSocketAddress =
			inetSocketTransportAddress.address();

		Assert.assertEquals(hostString, inetSocketAddress.getHostString());
		Assert.assertEquals(port, inetSocketAddress.getPort());
	}

	protected void modify(OperationMode operationMode) {
		_elasticsearchConnectionManager.modify(operationMode);
	}

	protected void resetMockConnections() {
		Mockito.reset(
			_mockedEmbeddedElasticsearchConnection,
			_mockedRemoteElasticsearchConnection);

		Mockito.when(
			_mockedEmbeddedElasticsearchConnection.getOperationMode()
		).thenReturn(
			OperationMode.EMBEDDED
		);
		Mockito.when(
			_mockedRemoteElasticsearchConnection.getOperationMode()
		).thenReturn(
			OperationMode.REMOTE
		);
	}

	protected void setUpPropsUtil() {
		Props props = Mockito.mock(Props.class);

		PropsUtil.setProps(props);
	}

	protected void verifyCloseNeverConnect(
		ElasticsearchConnection elasticsearchConnection) {

		Mockito.verify(
			elasticsearchConnection
		).close();

		Mockito.verify(
			elasticsearchConnection, Mockito.never()
		).connect();
	}

	protected void verifyConnectAndClose(
		ElasticsearchConnection elasticsearchConnection) {

		Mockito.verify(
			elasticsearchConnection
		).connect();

		Mockito.verify(
			elasticsearchConnection
		).close();
	}

	protected void verifyConnectNeverClose(
		ElasticsearchConnection elasticsearchConnection) {

		Mockito.verify(
			elasticsearchConnection, Mockito.never()
		).close();

		Mockito.verify(
			elasticsearchConnection
		).connect();
	}

	protected void verifyNeverCloseNeverConnect(
		ElasticsearchConnection elasticsearchConnection) {

		Mockito.verify(
			elasticsearchConnection, Mockito.never()
		).close();

		Mockito.verify(
			elasticsearchConnection, Mockito.never()
		).connect();
	}

	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

	@Mock
	private ElasticsearchConnection _mockedEmbeddedElasticsearchConnection;

	@Mock
	private ElasticsearchConnection _mockedRemoteElasticsearchConnection;

	private final RemoteElasticsearchConnection _remoteElasticsearchConnection =
		createRemoteElasticsearchConnection();

}