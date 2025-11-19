/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.blueprint.parameter.index;

import java.util.Date;

/**
 * @author Bryan Engler
 */
public class AsahCacheEntry {

	public String getDocumentId() {
		return _documentId;
	}

	public Date getLastSyncDate() {
		return _lastSyncDate;
	}

	public String getMostViewedContents() {
		return _mostViewedContents;
	}

	public int getSize() {
		return _size;
	}

	public void setDocumentId(String documentId) {
		_documentId = documentId;
	}

	public void setLastSyncDate(Date lastSyncDate) {
		_lastSyncDate = lastSyncDate;
	}

	public void setMostViewedContents(String mostViewedContents) {
		_mostViewedContents = mostViewedContents;
	}

	public void setSize(int size) {
		_size = size;
	}

	private String _documentId;
	private Date _lastSyncDate;
	private String _mostViewedContents;
	private int _size;

}