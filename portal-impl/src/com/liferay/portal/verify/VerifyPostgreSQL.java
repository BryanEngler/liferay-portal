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
import com.liferay.portal.kernel.io.unsync.UnsyncBufferedReader;
import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ClassLoaderUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.InputStream;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Bowerman
 */
public class VerifyPostgreSQL extends VerifyProcess {

	protected void deleteOrphanedLargeObjects(Statement statement, DB db)
		throws Exception {

		StringBundler sb = new StringBundler(3);

		sb.append("select lo_unlink(l.loid) from pg_largeobject l group by ");
		sb.append("loid having (not exists (select 1 from DLContent t where ");
		sb.append("t.data_ = l.loid));");

		statement.executeQuery(sb.toString());
	}

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
		ClassLoader classLoader = ClassLoaderUtil.getContextClassLoader();

		InputStream is = classLoader.getResourceAsStream(
			"com/liferay/portal/tools/sql/dependencies/rules.sql");

		if (is == null) {
			is = classLoader.getResourceAsStream("rules.sql");
		}

		if (is == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"rules.sql file does not exist. No new rules will be " +
					"created for this database.");
			}

			return;
		}

		List<String> ruleList = new ArrayList<>();

		String content = StringUtil.read(is);

		try (UnsyncBufferedReader unsyncBufferedReader =
				new UnsyncBufferedReader(new UnsyncStringReader(content))) {

			int ruleNameIndex = 0;
			int ruleDefinitionIndex = 0;

			String line = null;

			while ((line = unsyncBufferedReader.readLine()) != null) {
				if (line.startsWith(_SQL_CREATE_RULE)) {
					ruleNameIndex = _SQL_CREATE_RULE.length();
					ruleDefinitionIndex = line.indexOf(" ", ruleNameIndex);

					String rule = line.substring(
						ruleNameIndex, ruleDefinitionIndex);

					ruleList.add(rule);
				}
			}
		}

		ResultSet rs = null;

		for (String rule : ruleList) {
			StringBundler sb = new StringBundler(4);

			sb.append("select * from pg_catalog.pg_rules where rulename = ");
			sb.append("lower ('");
			sb.append(rule);
			sb.append("')");

			rs = statement.executeQuery(sb.toString());

			if (!rs.next()) {
				if (_log.isInfoEnabled()) {
					_log.info("Adding rules from rules.sql file");
				}

				db.runSQLTemplate("rules.sql", false);

				break;
			}
		}
	}

	private static final String _SQL_CREATE_RULE = "create or replace rule ";

	private static final Log _log = LogFactoryUtil.getLog(
		VerifyPostgreSQL.class);

}