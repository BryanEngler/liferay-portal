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

package com.liferay.portal.search.internal.indexer;

import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.ResourcedModel;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.Tuple;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.indexer.BaseModelDocumentFactory;
import com.liferay.portal.search.model.uid.UIDStamper;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, service = BaseModelDocumentFactory.class)
public class BaseModelDocumentFactoryImpl implements BaseModelDocumentFactory {

	@Override
	public com.liferay.portal.kernel.search.Document createDocument(
		BaseModel<?> baseModel) {

		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		String className = baseModel.getModelClassName();

		Tuple classPKResourcePrimKeyTuple = getClassPKResourcePrimKey(
			baseModel);

		long classPK = (Long)classPKResourcePrimKeyTuple.getObject(0);

		documentBuilder.setString(
			Field.ENTRY_CLASS_NAME, className
		).setLong(
			Field.ENTRY_CLASS_PK, classPK
		).setLong(
			Field.ROOT_ENTRY_CLASS_PK,
			getRootEntryClassPK(classPKResourcePrimKeyTuple)
		).setString(
			Field.UID, getDocumentUID(className, classPK)
		).build();

		uidStamper.setUIDM(baseModel, documentBuilder);

		Document document = documentBuilder.build();

		uidStamper.getUIDM(document);

		return toLegacyDocument(document);
	}

	protected Tuple getClassPKResourcePrimKey(BaseModel<?> baseModel) {
		long classPK = 0;
		long resourcePrimKey = 0;

		if (baseModel instanceof ResourcedModel) {
			ResourcedModel resourcedModel = (ResourcedModel)baseModel;

			classPK = resourcedModel.getResourcePrimKey();
			resourcePrimKey = resourcedModel.getResourcePrimKey();
		}
		else {
			classPK = (Long)baseModel.getPrimaryKeyObj();
		}

		return new Tuple(classPK, resourcePrimKey);
	}

	protected String getDocumentUID(String className, long classPK) {
		return Field.getUID(className, String.valueOf(classPK));
	}

	protected Long getRootEntryClassPK(Tuple classPKResourcePrimKeyTuple) {
		long resourcePrimKey = (Long)classPKResourcePrimKeyTuple.getObject(1);

		if (resourcePrimKey > 0) {
			return resourcePrimKey;
		}

		return null;
	}

	protected com.liferay.portal.kernel.search.Document toLegacyDocument(
		Document document) {

		DocumentImpl documentImpl = new DocumentImpl();

		Map<String, com.liferay.portal.search.document.Field> fields =
			document.getFields();

		fields.forEach(
			(key, field) -> documentImpl.add(
				new Field(key, String.valueOf(field.getValue()))));

		return documentImpl;
	}

	@Reference
	protected DocumentBuilderFactory documentBuilderFactory;

	@Reference
	protected UIDStamper uidStamper;

}