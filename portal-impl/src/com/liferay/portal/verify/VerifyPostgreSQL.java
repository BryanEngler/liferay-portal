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

import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.util.PropsValues;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * @author Michael Bowerman
 */
public class VerifyPostgreSQL extends VerifyProcess {

	@Override
	protected void doVerify() throws Exception {
		DB db = DBFactoryUtil.getDB();

		String dbType = db.getType();

		if (!dbType.equals(DB.TYPE_POSTGRESQL)) {
			return;
		}

		Statement statement = connection.createStatement();

		verifyRules(statement, db);
		deleteOrphanedLargeObjects(statement, db);
	}

	protected void verifyRules(Statement statement, DB db) throws Exception {

		StringBundler sb = new StringBundler(3);

		sb.append("SELECT * FROM pg_catalog.pg_rules WHERE ");
		sb.append("rulename = 'delete_dlcontent_data_' ");
		sb.append("OR rulename = 'update_dlcontent_data'");

		ResultSet rs = statement.executeQuery(sb.toString());

		if (!rs.next()) {

			if (_log.isInfoEnabled()) {
				_log.info(
					"Adding rules for deleting and updating large documents");
			}

			db.runSQLTemplate("rules.sql", false);
		}
	}

	protected void deleteOrphanedLargeObjects(Statement statement, DB db)
		throws Exception {

		StringBundler sb = new StringBundler(5);

		sb.append("SELECT lo_unlink(l.loid) ");
		sb.append("FROM pg_largeobject l ");
		sb.append("GROUP BY loid ");
		sb.append("HAVING (NOT EXISTS ");
		sb.append("(SELECT 1 FROM dlcontent t WHERE t.data_ = l.loid));");

		statement.executeQuery(sb.toString());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		VerifyPostgreSQL.class);

}