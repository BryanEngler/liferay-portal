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

package com.liferay.portal.search.web.internal.demo;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author André de Oliveira
 */
public class DemoData implements SearchResultsData {

	@Override
	public List<Document> getDocuments() {
		List<Document> list = new ArrayList<>();

		Field title = new Field(Field.TITLE);
		Field content = new Field(Field.CONTENT);

		title.setValue("Pothole Repair (Internal)");
		content.setValue(
			"Pothole Repair (Internal) " +
				"{ latitude: 42.2844, longitude: -71.0663 }" +
				" 65 Bailey St Dorchester MA 02124 | " +
				"Case Closed Internal Case Performed " +
				"by Highway Maintenance crew.");

		Document document = new DocumentImpl();

		document.addGeoLocation(42.2844, -71.0663);
		document.add(title);
		document.add(content);

		list.add(document);

		content = new Field(Field.CONTENT);
		content.setValue(
			"Pothole Repair (Internal) " +
				"{ latitude: 42.2753, longitude: -71.0631 }" +
				" 116 Richmond St  Dorchester  MA  02124 | " +
				"Case Closed Internal Case Performed " +
				"by Highway Maintenance crew.");

		document = new DocumentImpl();

		document.addGeoLocation(42.2753, -71.0631);
		document.add(title);
		document.add(content);

		list.add(document);

		content = new Field(Field.CONTENT);
		content.setValue(
			"Pothole Repair (Internal) " +
				"{ latitude: 42.2872, longitude: -71.0634 }" +
				" 12 Dracut St  Dorchester  MA  02124 | " +
				"Case Closed Internal Case Performed " +
				"by Highway Maintenance crew.");

		document = new DocumentImpl();

		document.addGeoLocation(42.2872, -71.0634);
		document.add(title);
		document.add(content);

		list.add(document);

		content = new Field(Field.CONTENT);
		content.setValue(
			"Pothole Repair (Internal) " +
				"{ latitude: 42.2869, longitude: -71.0632 }" +
				" 13 Dracut St  Dorchester  MA  02124 | " +
				"Case Closed Internal Case Performed " +
				"by Highway Maintenance crew.");

		document = new DocumentImpl();

		document.addGeoLocation(42.2869, -71.0632);
		document.add(title);
		document.add(content);

		list.add(document);

		return list;
	}

	@Override
	public String[] getQueryTerms() {
		return null;
	}

}