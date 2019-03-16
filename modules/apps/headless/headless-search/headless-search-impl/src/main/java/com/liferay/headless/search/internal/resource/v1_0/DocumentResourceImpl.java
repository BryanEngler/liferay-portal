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

package com.liferay.headless.search.internal.resource.v1_0;

import com.liferay.headless.search.dto.v1_0.Document;
import com.liferay.headless.search.resource.v1_0.DocumentResource;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.GetDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.GetDocumentResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Bryan Engler
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/document.properties",
	scope = ServiceScope.PROTOTYPE, service = DocumentResource.class
)
public class DocumentResourceImpl extends BaseDocumentResourceImpl {

	@Override
	public Document getDocumentIndexUid(String index, String uid)
		throws Exception {

		System.out.println("called REST");

		GetDocumentRequest getDocumentRequest = new GetDocumentRequest(
			index, uid);

		GetDocumentResponse getDocumentResponse = searchEngineAdapter.execute(
			getDocumentRequest);

		Document document = _toDocument(getDocumentResponse);

		return document;
	}

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	private Document _toDocument(GetDocumentResponse getDocumentResponse)
		throws PortalException {

		return new Document() {
			{
				source = getDocumentResponse.getSource();
			}
		};
	}

}