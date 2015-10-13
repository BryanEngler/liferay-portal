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

package com.liferay.dynamic.data.mapping.verify;

import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.verify.VerifyProcess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.osgi.service.component.annotations.Component;

/**
 * @author Christopher Kian
 */
@Component(
	property = {
		"verify.process.name=com.liferay.dynamic.data.mapping.verify.DDMTemplateVerifyProcess"
	},
	service = VerifyProcess.class
)
public class DDMTemplateVerifyProcess extends VerifyProcess {

	@Override
	protected void doVerify() throws Exception {
		verifyDuplicateDDMTemplates();
	}

	protected void removeDuplicateDDMTemplates() throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String pkColumnName = "templateId";

		StringBuilder sb = new StringBuilder(11);

		sb.append("select alpha.");
		sb.append(pkColumnName);
		sb.append(" from DDMTemplate alpha, ");
		sb.append("DDMTemplate beta where ");
		sb.append("alpha.groupId = beta.groupId AND ");
		sb.append("alpha.classNameId = beta.classNameId AND ");
		sb.append("alpha.templateKey = beta.templateKey AND ");
		sb.append("alpha.");
		sb.append(pkColumnName);
		sb.append(" > beta.");
		sb.append(pkColumnName);

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(sb.toString());

			rs = ps.executeQuery();

			while (rs.next()) {
				long primKey = rs.getLong(pkColumnName);

				removeDuplicateDDMTemplates(primKey);
			}
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected void removeDuplicateDDMTemplates(long primKey) throws Exception {
		DDMTemplate ddmTemplate = DDMTemplateLocalServiceUtil.fetchDDMTemplate(
			primKey);

		if (Validator.isNotNull(ddmTemplate)) {
			if (_log.isDebugEnabled()) {
				_log.debug("Removing DDMTemplate with ID: {" + primKey + "}");
			}

			DDMTemplateLocalServiceUtil.deleteDDMTemplate(primKey);
		}
		else {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No template found for DDMTemplate with ID: {" +
					primKey + "}. Possibly could have already been removed.");
			}
		}
	}

	protected void verifyDuplicateDDMTemplates() throws Exception {
		try {
			runSQL("drop index IX_E6DFAB84 on DDMTemplate");
		}
		catch (Exception e) {
		}

		StringBuilder sb = new StringBuilder(2);

		sb.append("create unique index IX_E6DFAB84 on DDMTemplate ");
		sb.append("(groupId, classNameId, templateKey)");

		try {
			runSQL(sb.toString());
		}
		catch (Exception e) {
			removeDuplicateDDMTemplates();

			runSQL(sb.toString());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DDMTemplateVerifyProcess.class);

}