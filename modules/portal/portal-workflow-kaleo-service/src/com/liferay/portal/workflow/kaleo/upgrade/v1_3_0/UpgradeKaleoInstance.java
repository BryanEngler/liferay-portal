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
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;

/**
 * @author Christopher Kian
 */
public class UpgradeKaleoInstance extends UpgradeProcess {

	protected void deleteOrphanedWorkflowInstanceLinks(String tableName)
		throws Exception {

		StringBundler sb = new StringBundler(11);

		sb.append("delete from ");
		sb.append(tableName);
		sb.append(" where classPK not in ");
		sb.append(StringPool.OPEN_PARENTHESIS);
		sb.append("select recordId from DDLRecord");
		sb.append(StringPool.CLOSE_PARENTHESIS);
		sb.append(" and classname like ");
		sb.append(StringPool.APOSTROPHE);
		sb.append(getKaleoClassName());
		sb.append(StringPool.APOSTROPHE);

		runSQL(sb.toString());
	}

	@Override
	protected void doUpgrade() throws Exception {
		for (String tableName : getOrphanedWorkflowInstanceTableNames()) {
			deleteOrphanedWorkflowInstanceLinks(tableName);
		}
	}

	protected String[] getKaleoClassName() {
		return _KALEO_CLASS_NAME;
	}

	protected String[] getOrphanedWorkflowInstanceTableNames() {
		return _WORKFLOW_INSTANCE_TABLE_NAMES;
	}

	private static final String _KALEO_CLASS_NAME =
		"com.liferay.portal.workflow.kaleo.forms.model.KaleoProcess";

	private static final String[] _WORKFLOW_INSTANCE_TABLE_NAMES =
			new String[] {
				"KaleoInstance", "KaleoInstanceToken", "WorkflowInstanceLink"
	};

}