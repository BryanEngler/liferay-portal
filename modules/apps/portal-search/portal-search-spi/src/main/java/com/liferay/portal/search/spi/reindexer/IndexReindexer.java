/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.spi.reindexer;

/**
 * @author Bryan Engler
 */
public interface IndexReindexer {

	/*
	something like this that can maybe help standardize index names and also be
	used to check if indexes exist or not
	 */
	public String getIndexNameSuffix();

	@Deprecated
	public void reindex(long companyId) throws Exception;

	@Deprecated
	public void reindex(long companyId, String executionMode) throws Exception;

	public void reindex(long companyId, ExecutionMode executionMode)
		throws Exception;

	/*
	create enum inststead of String so that developers know what the different
	modes are
	 */
	public enum ExecutionMode {
		CONCURRENT, FULL, SYNC
	}

}