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

package com.liferay.portal.search.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bryan Engler
 */
public class ConnectionInformation {

	public String getClusterName() {
		return _clusterName;
	}

	public String getConnectionId() {
		return _connectionId;
	}

	public String getError() {
		return _error;
	}

	public String getHealth() {
		return _health;
	}

	public String getLabel() {
		return _label;
	}

	public List<NodeInformation> getNodeInformationList() {
		return _nodeInformationList;
	}

	public void setClusterName(String clusterName) {
		_clusterName = clusterName;
	}

	public void setConnectionId(String connectionId) {
		_connectionId = connectionId;
	}

	public void setError(String error) {
		_error = error;
	}

	public void setHealth(String health) {
		_health = health;
	}

	public void setLabel(String label) {
		_label = label;
	}

	public void setNodeInformationList(
		List<NodeInformation> nodeInformationList) {

		_nodeInformationList = nodeInformationList;
	}

	private String _clusterName;
	private String _connectionId;
	private String _error;
	private String _health;
	private String _label;
	private List<NodeInformation> _nodeInformationList = new ArrayList<>();

}