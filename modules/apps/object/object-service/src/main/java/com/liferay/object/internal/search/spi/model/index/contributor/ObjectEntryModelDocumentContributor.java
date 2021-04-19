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

package com.liferay.object.internal.search.spi.model.index.contributor;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.math.BigDecimal;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
@Component(
	immediate = true,
	property = "indexer.class.name=com.liferay.object.model.ObjectEntry",
	service = ModelDocumentContributor.class
)
public class ObjectEntryModelDocumentContributor
	implements ModelDocumentContributor<ObjectEntry> {

	@Override
	public void contribute(Document document, ObjectEntry objectEntry) {
		try {
			_contribute(document, objectEntry);
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to index object entry " +
						objectEntry.getObjectEntryId(),
					exception);
			}
		}
	}

	private void _contribute(Document document, ObjectEntry objectEntry)
		throws Exception {

		Map<String, Object> values =
			_objectEntryLocalService.getObjectEntryValues(
				objectEntry.getObjectEntryId());

		document.addKeyword(
			"objectDefinitionId", objectEntry.getObjectDefinitionId());

		Set<Map.Entry<String, Object>> entries = values.entrySet();

		for (Map.Entry<String, Object> entry : entries) {
			String name = entry.getKey();
			Object value = entry.getValue();

			if (value instanceof BigDecimal) {
				document.addNumber(name, (BigDecimal)value);
			}
			else if (value instanceof Boolean) {
				document.addKeyword(name, (Boolean)value);
			}
			else if (value instanceof Date) {
				document.addDate(name, (Date)value);
			}
			else if (value instanceof Double) {
				document.addNumber(name, (Double)value);
			}
			else if (value instanceof Integer) {
				document.addNumber(name, (Integer)value);
			}
			else if (value instanceof Long) {
				document.addNumber(name, (Long)value);
			}
			else if (value instanceof String) {
				document.addText(name, (String)value);
			}
			else if (value instanceof byte[]) {
				document.addText(name, new String((byte[])value));
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryModelDocumentContributor.class);

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

}