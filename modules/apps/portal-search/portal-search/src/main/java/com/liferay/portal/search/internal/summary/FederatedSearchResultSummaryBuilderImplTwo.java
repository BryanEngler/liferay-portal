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

package com.liferay.portal.search.internal.summary;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.spi.federated.summary.FederatedSearchResultSummaryBuilder;
import com.liferay.portal.search.summary.FederatedSearchSummary;
import com.liferay.portal.search.summary.Summary;
import com.liferay.portal.search.summary.SummaryBuilder;
import com.liferay.portal.search.summary.SummaryBuilderFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = FederatedSearchResultSummaryBuilder.class)
public class FederatedSearchResultSummaryBuilderImplTwo implements
	FederatedSearchResultSummaryBuilder {

	public FederatedSearchSummary getSummary(Document document) {

		SummaryBuilder builder = summaryBuilderFactory.newInstance();

		builder.setTitle(document.get("federatedTitle"));
		builder.setContent(document.get("federatedContent"));

		Summary summary = builder.build();

		String youtubeVideoID =  document.get("youtubeVideoID");

		FederatedSearchSummary federatedSearchSummary = new
			FederatedSearchSummaryImpl(
				summary.getTitle(), summary.getContent(), youtubeVideoID,
				summary.getLocale());

		return federatedSearchSummary;
	}

	public String getSourceDisplayName() {
		//must match FederatedSearcherImplTwo.getSourceDisplayName()
		return "Federated Cluster Two";
	}

	@Reference
	protected SummaryBuilderFactory summaryBuilderFactory;

}