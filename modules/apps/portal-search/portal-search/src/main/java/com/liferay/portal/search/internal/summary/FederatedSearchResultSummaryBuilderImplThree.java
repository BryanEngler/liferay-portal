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

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.search.spi.federated.summary.FederatedSearchResultSummaryBuilder;
import com.liferay.portal.search.summary.FederatedSearchSummary;
import com.liferay.portal.search.summary.Summary;
import com.liferay.portal.search.summary.SummaryBuilder;
import com.liferay.portal.search.summary.SummaryBuilderFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = FederatedSearchResultSummaryBuilder.class)
public class FederatedSearchResultSummaryBuilderImplThree implements
	FederatedSearchResultSummaryBuilder {

	public FederatedSearchSummary getSummary(Document document) {

		SummaryBuilder builder = summaryBuilderFactory.newInstance();

		builder.setTitle(document.get("page_title"));

		builder.setContent(buildContent(getSnippets(document)));

		builder.setHighlight(true);
		builder.setEscape(false);

		Summary summary = builder.build();

		String url =  document.get("url");

		FederatedSearchSummary federatedSearchSummary = new
			FederatedSearchSummaryImpl(
				summary.getTitle(),summary.getContent(), url,
				summary.getLocale());

		return federatedSearchSummary;
	}

	public String getSourceDisplayName() {
		//must match FederatedSearcherImplThree.getSourceDisplayName()
		return "Federated Page Search";
	}

	protected String buildContent(List<String> snipets) {
		StringBundler sb = new StringBundler();

		for (String snippet : snipets) {
			sb.append(snippet);
			sb.append(StringPool.TRIPLE_PERIOD);
		}

		if (sb.index() > 1) {
			sb.setIndex(sb.index() - 1);
		}

		return sb.toString();
	}

	protected List<String> getSnippets(Document document) {
		List<String> snippets = new ArrayList<>();

		Map<String, Field> fieldMap = document.getFields();

		for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
			String fieldName = entry.getKey();

			if (fieldName.startsWith(Field.SNIPPET)) {
				Field field = entry.getValue();

				String snippet = field.getValue();

				snippets.add(snippet);
			}
		}

		return snippets;
	}

	@Reference
	protected SummaryBuilderFactory summaryBuilderFactory;

}