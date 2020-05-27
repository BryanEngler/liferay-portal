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

package com.liferay.portal.search.elasticsearch7.internal;

import java.net.BindException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnection;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnectionFixture;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch7.internal.sidecar.SidecarKnownIssues;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.SnapshotClient;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author André de Oliveira
 */
public class ElasticsearchSearchEngineReconnectTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		Assume.assumeTrue(SidecarKnownIssues.LPS112597_IS_FIXED);

		ElasticsearchConnectionFixture elasticsearchConnectionFixture =
			ElasticsearchConnectionFixture.builder(
			).clusterName(
				ElasticsearchSearchEngineReconnectTest.class.getSimpleName()
			).elasticsearchConfigurationProperties(
				HashMapBuilder.<String, Object>put(
					"embeddedHttpPort", String.valueOf(9200)
				).put(
					"transportTcpPort", "9300-9301"
				).build()
			).build();

		ElasticsearchSearchEngineFixture elasticsearchSearchEngineFixture =
			new ElasticsearchSearchEngineFixture(
				elasticsearchConnectionFixture);

		elasticsearchSearchEngineFixture.setUp();

		_elasticsearchConnectionFixture = elasticsearchConnectionFixture;

		_elasticsearchSearchEngineFixture = elasticsearchSearchEngineFixture;
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		if (_elasticsearchSearchEngineFixture != null) {
			_elasticsearchSearchEngineFixture.tearDown();
		}
	}

	public SnapshotClient getSnapshotClient() {
		RestHighLevelClient restHighLevelClient =
			_elasticsearchConnectionFixture.getRestHighLevelClient();

		return restHighLevelClient.snapshot();
	}

	@Test
	public void testInitializeAfterReconnect() {
		ElasticsearchSearchEngine elasticsearchSearchEngine =
			_elasticsearchSearchEngineFixture.getElasticsearchSearchEngine();

		long companyId = RandomTestUtil.randomLong();

//		elasticsearchSearchEngine.initialize(companyId);
		
		reconnect(
			_elasticsearchSearchEngineFixture.
				getElasticsearchConnectionManager());
//		try {
//			TimeUnit.SECONDS.sleep(20);
//		} catch (InterruptedException e) {
//			
//		}
		
//		elasticsearchSearchEngine.initialize(companyId);
	}

	protected void reconnect(
		ElasticsearchConnectionManager elasticsearchConnectionManager) {

		ElasticsearchConnection elasticsearchConnection =
			elasticsearchConnectionManager.getElasticsearchConnection();
		
		boolean ret1 = _isPortAvaiable(9200);
		
		System.out.println("ret1 = " + ret1);
		
		elasticsearchConnection.close();
		
		boolean ret2 = _isPortAvaiable(9200);
		
		System.out.println("ret2 = " + ret2);
			
//		while (!_isPortAvaiable(9200)) {
//			try {
//				TimeUnit.SECONDS.sleep(5);
//			} catch (InterruptedException e) {
//				
//			}
//		}
		
		elasticsearchConnection.connect();
	}
	
	private static boolean _isPortAvaiable(int port)
	{
	    Socket socket = null;
	    try {
	    	socket = new Socket("127.0.0.1", port);
	        return false;
	    } catch (Exception exception) {
	        return true;
	    } finally {
	        if(socket != null)
	            try {socket.close();}
	            catch(Exception exception){}
	    }
	}
	
	boolean isPortOccupied(int port) {
	    DatagramSocket sock = null;
	    try {
	        sock = new DatagramSocket(port);
	        sock.close();
	        return false;
	    } catch (BindException ignored) {
	        return true;
	    } catch (SocketException ex) {
	        System.out.println(ex);
	        return true;
	    }
	}

	private static ElasticsearchConnectionFixture
		_elasticsearchConnectionFixture;
	private static ElasticsearchSearchEngineFixture
		_elasticsearchSearchEngineFixture;

}