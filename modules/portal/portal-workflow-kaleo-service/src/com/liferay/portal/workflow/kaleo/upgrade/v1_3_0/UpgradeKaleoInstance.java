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

package com.liferay.portal.workflow.kaleo.upgrade.v1_3_0;

import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.util.PortalUtil;

/**
 * @author Christopher Kian
 */
public class UpgradeKaleoInstance extends UpgradeProcess {

	protected void deleteOrphanedWorkflowInstanceLinks(
			String tableName, String columnName, String columnValue)
		throws Exception {

		StringBundler sb = new StringBundler(12);

		sb.append("delete from ");
		sb.append(tableName);
		sb.append(" where classPK not in ");
		sb.append(StringPool.OPEN_PARENTHESIS);
		sb.append("select recordId from DDLRecord");
		sb.append(StringPool.CLOSE_PARENTHESIS);
		sb.append(" and ");
		sb.append(columnName);
		sb.append(" like ");
		sb.append(StringPool.APOSTROPHE);
		sb.append(columnValue);
		sb.append(StringPool.APOSTROPHE);

		runSQL(sb.toString());
	}

	@Override
	protected void doUpgrade() throws Exception {
		for (String[] tableData : getOrphanedWorkflowInstanceTableData()) {
			String tableName = tableData[0];
			String columnName = tableData[1];
			String columnValue = null;

			if (StringUtil.matches(columnName, "classNameId")) {
				columnValue = getKaleoClassNameId();
			}
			else {
				columnValue = getKaleoClassName();
			}

			deleteOrphanedWorkflowInstanceLinks(
				tableName, columnName, columnValue);
		}
	}

	protected String getKaleoClassName() {
		return _KALEO_CLASS_NAME;
	}

	protected String getKaleoClassNameId() {
		return GetterUtil.getString(
			PortalUtil.getClassNameId(_KALEO_CLASS_NAME));
	}

	protected String[][] getOrphanedWorkflowInstanceTableData() {
		return _WORKFLOW_INSTANCE_TABLE_DATA;
	}

	private static final String _KALEO_CLASS_NAME =
		"com.liferay.portal.workflow.kaleo.forms.model.KaleoProcess";

	private static final String[][] _WORKFLOW_INSTANCE_TABLE_DATA = {
		new String[] {"KaleoInstance", "className"},
		new String[] {"KaleoInstanceToken", "className"},
		new String[] {"workflowinstancelink", "classNameId"}
	};

}