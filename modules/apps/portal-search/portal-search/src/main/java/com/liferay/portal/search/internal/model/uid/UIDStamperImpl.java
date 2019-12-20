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

package com.liferay.portal.search.internal.model.uid;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.ClassedModel;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.model.uid.UIDStamper;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;

/**
 * @author André de Oliveira
 */
@Component(service = UIDStamper.class)
public class UIDStamperImpl implements UIDStamper {

	@Override
	public String getUIDM(ClassedModel classedModel) {
		return getUIDM(
			classedModel.getModelClassName(),
			(Long)classedModel.getPrimaryKeyObj());
	}

	@Override
	public String getUIDM(com.liferay.portal.kernel.search.Document document) {
		return guardUIDM(document.get("uidm"), document.getUID());
	}

	@Override
	public String getUIDM(Document document) {
		return guardUIDM(
			document.getString("uidm"), document.getString(Field.UID));
	}

	@Override
	public void setUIDM(
		ClassedModel classedModel,
		com.liferay.portal.kernel.search.Document document) {

		document.addKeyword(
			"uidm", guardUIDM(getUIDM(classedModel), document.getUID()));
	}

	@Override
	public void setUIDM(
		ClassedModel classedModel, DocumentBuilder documentBuilder) {

		documentBuilder.setString("uidm", getUIDM(classedModel));
	}

	protected String getUIDM(String className, long classPK) {
		return Field.getUID(className, String.valueOf(classPK));
	}

	protected String guardUIDM(String uidm, String uid) {
		if (!uid.startsWith("com_liferay_configuration_admin_web_portlet_SystemSettingsPortlet")) {
			if (!Objects.equals(uid, uidm)) {
				throw new RuntimeException(
					StringBundler.concat(
						"(uid) ", uid, " <> ", uidm, " (uidm)"));
			}
		}

		return uidm;
	}

}