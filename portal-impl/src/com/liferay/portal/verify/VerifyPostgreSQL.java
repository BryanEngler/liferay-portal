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
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Bowerman
 */
public class VerifyPostgreSQL extends VerifyProcess {

	protected void addRule(
			Statement statement, String ruleName, String tableName,
			String columnName)
		throws SQLException {

		String[] tokens = StringUtil.split(ruleName, StringPool.UNDERLINE);

		String ruleType = tokens[0];

		StringBundler sb = new StringBundler();

		if (ruleType.equals(_RULE_TYPE_DELETE)) {
			sb.append("create or replace rule ");
			sb.append(ruleName);
			sb.append(" as on delete to ");
			sb.append(tableName);
			sb.append(" do also select case when exists( select 1 from ");
			sb.append("pg_catalog.pg_largeobject where (loid = old.");
			sb.append(columnName);
			sb.append(")) then lo_unlink(old.");
			sb.append(columnName);
			sb.append(") end from ");
			sb.append(tableName);
			sb.append(" where ");
			sb.append(tableName);
			sb.append(StringPool.PERIOD);
			sb.append(columnName);
			sb.append(" = old.");
			sb.append(columnName);
			sb.append(StringPool.SEMICOLON);
		}
		else if (ruleType.equals(_RULE_TYPE_UPDATE)) {
			sb.append("create or replace rule ");
			sb.append(ruleName);
			sb.append(" as on update to ");
			sb.append(tableName);
			sb.append(" where old.");
			sb.append(columnName);
			sb.append(" is distinct from new.");
			sb.append(columnName);
			sb.append(" and old.");
			sb.append(columnName);
			sb.append(" is not null do also select case when exists( select ");
			sb.append("1 from pg_catalog.pg_largeobject where (loid = old.");
			sb.append(columnName);
			sb.append(")) then lo_unlink(old.");
			sb.append(columnName);
			sb.append(") end from ");
			sb.append(tableName);
			sb.append(" where ");
			sb.append(tableName);
			sb.append(StringPool.PERIOD);
			sb.append(columnName);
			sb.append(" = old.");
			sb.append(columnName);
			sb.append(StringPool.SEMICOLON);
		}
		else {
			if (_log.isErrorEnabled()) {
				StringBundler errorSB = new StringBundler();

				errorSB.append("Unknown rule type: ");
				errorSB.append(ruleType);
				errorSB.append(". The following rule will not be added: ");
				errorSB.append(ruleName);

				_log.error(errorSB.toString());

				return;
			}
		}

		statement.execute(sb.toString());
	}

	protected void deleteOrphanedLargeObjects(
			Statement statement, HashMap<String, String> columnsWithOids)
		throws SQLException {

		StringBundler sb = new StringBundler(3);

		sb.append("select lo_unlink(l.loid) from pg_largeobject l group by ");
		sb.append("loid having ");

		int count = 1;
		int size = columnsWithOids.size();

		for (Map.Entry<String, String> column : columnsWithOids.entrySet()) {
			String tableName = column.getKey();
			String columnName = column.getValue();

			sb.append("(not exists (select 1 from ");
			sb.append(tableName);
			sb.append(" t where t.");
			sb.append(columnName);
			sb.append(" = l.loid))");

			if (count < size) {
				sb.append(" and ");
			}

			count++;
		}

		if (_log.isInfoEnabled()) {
			_log.info("Deleting orphaned large objects");
		}

		statement.execute(sb.toString());
	}

	@Override
	protected void doVerify() throws Exception {
		DB db = DBManagerUtil.getDB();

		if (db.getDBType() != DBType.POSTGRESQL) {
			return;
		}

		Statement statement = connection.createStatement();

		HashMap<String, String> columnsWithOids = getColumnsWithOids(statement);

		verifyRules(statement, columnsWithOids);
		deleteOrphanedLargeObjects(statement, columnsWithOids);
	}

	protected HashMap<String, String> getColumnsWithOids(Statement statement)
		throws SQLException {

		HashMap<String, String> columnsWithOids = new HashMap<>();

		StringBundler sb = new StringBundler(3);

		sb.append("select table_name, column_name from ");
		sb.append("information_schema.columns where table_schema='public' ");
		sb.append("and data_type='oid';");

		ResultSet rs = null;

		rs = statement.executeQuery(sb.toString());

		while (rs.next()) {
			String table = (String)rs.getObject("table_name");
			String column = (String)rs.getObject("column_name");

			columnsWithOids.put(table, column);
		}

		return columnsWithOids;
	}

	protected void verifyRules(
			Statement statement, HashMap<String, String> columnsWithOids)
		throws SQLException {

		ResultSet rs = null;

		for (Map.Entry<String, String> column : columnsWithOids.entrySet()) {
			String tableName = column.getKey();
			String columnName = column.getValue();

			StringBundler deleteRuleSB = new StringBundler(5);
			deleteRuleSB.append(_RULE_TYPE_DELETE);
			deleteRuleSB.append(StringPool.UNDERLINE);
			deleteRuleSB.append(tableName);
			deleteRuleSB.append(StringPool.UNDERLINE);
			deleteRuleSB.append(columnName);

			StringBundler updateRuleSB = new StringBundler(5);
			updateRuleSB.append(_RULE_TYPE_UPDATE);
			updateRuleSB.append(StringPool.UNDERLINE);
			updateRuleSB.append(tableName);
			updateRuleSB.append(StringPool.UNDERLINE);
			updateRuleSB.append(columnName);

			List<String> ruleList = new ArrayList<>();

			ruleList.add(deleteRuleSB.toString());
			ruleList.add(updateRuleSB.toString());

			for (String ruleName : ruleList) {
				StringBundler sb = new StringBundler(4);

				sb.append("select * from pg_catalog.pg_rules where rulename ");
				sb.append("= lower ('");
				sb.append(ruleName);
				sb.append("')");

				rs = statement.executeQuery(sb.toString());

				if (!rs.next()) {
					if (_log.isInfoEnabled()) {
						_log.info("Adding the following rule: " + ruleName);
					}

					addRule(statement, ruleName, tableName, columnName);
				}
			}
		}
	}

	private static final String _RULE_TYPE_DELETE = "delete";

	private static final String _RULE_TYPE_UPDATE = "update";

	private static final Log _log = LogFactoryUtil.getLog(
		VerifyPostgreSQL.class);

}