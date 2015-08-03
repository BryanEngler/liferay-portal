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

package com.liferay.portal.verify;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.ClassName;
import com.liferay.portal.service.ClassNameLocalServiceUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Shinn Lok
 */
public class VerifyWorkflow extends VerifyProcess {

	protected void deleteOrphanedWorkflowDefinitionLinks() throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"select distinct classNameId from WorkflowDefinitionLink");

			rs = ps.executeQuery();

			while (rs.next()) {
				long classNameId = rs.getLong("classNameId");

				ClassName className = ClassNameLocalServiceUtil.fetchClassName(
					classNameId);

				if (className == null) {
					continue;
				}

				String classNameValue = className.getValue();

				for (String[] orphanedAttachedModel :
						getOrphanedAttachedModels()) {

					String orphanedClassName = orphanedAttachedModel[0];

					if (classNameValue.equals(orphanedClassName)) {
						String orphanedTableName = orphanedAttachedModel[1];
						String orphanedColumnName = orphanedAttachedModel[2];

						deleteOrphanedWorkflowDefinitionLinks(
							orphanedTableName, orphanedColumnName);
					}
				}
			}
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected void deleteOrphanedWorkflowDefinitionLinks(
			String tableName, String columnName)
		throws Exception {

		StringBundler sb = new StringBundler(6);

		sb.append("delete from WorkflowDefinitionLink where classPK not ");
		sb.append("in (select ");
		sb.append(columnName);
		sb.append(" from ");
		sb.append(tableName);
		sb.append(StringPool.CLOSE_PARENTHESIS);

		runSQL(sb.toString());
	}

	protected void deleteOrphanedWorkflowInstanceLinks() throws Exception {
		for (String tableName : getOrphanedWorkflowInstanceTableNames()) {
			deleteOrphanedWorkflowInstanceLinks(tableName);
		}
	}

	protected void deleteOrphanedWorkflowInstanceLinks(String tableName)
		throws Exception {

		StringBundler sb = new StringBundler(6);

		sb.append("delete from ");
		sb.append(tableName);
		sb.append(" where classPK not in ");
		sb.append(StringPool.OPEN_PARENTHESIS);
		sb.append("select recordId from DDLRecord");
		sb.append(StringPool.CLOSE_PARENTHESIS);

		try {
			runSQL(sb.toString());
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to delete from table: " + tableName);
			}
		}
	}

	@Override
	protected void doVerify() throws Exception {
		deleteOrphanedWorkflowDefinitionLinks();
		deleteOrphanedWorkflowInstanceLinks();
	}

	protected String[][] getOrphanedAttachedModels() {
		return _ORPHANED_ATTACHED_MODELS;
	}

	protected String[] getOrphanedWorkflowInstanceTableNames() {
		return _WORKFLOW_INSTANCE_TABLE_NAMES;
	}

	private static final String[][] _ORPHANED_ATTACHED_MODELS = new String[][] {
		new String[] {
			"com.liferay.portal.workflow.kaleo.forms.model.KaleoProcess",
			"KaleoProcess", "kaleoProcessId"
		}
	};

	private static final String[] _WORKFLOW_INSTANCE_TABLE_NAMES =
		new String[] {
			"kaleoinstance", "kaleoinstancetoken", "workflowinstancelink"
	};

	private static final Log _log = LogFactoryUtil.getLog(VerifyWorkflow.class);

}