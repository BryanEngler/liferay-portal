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

package com.liferay.portal.search.page.search;

import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.generic.MatchAllQuery;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.DocumentRequestExecutor;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.index.CreateIndexRequest;
import com.liferay.portal.search.engine.adapter.index.DeleteIndexRequest;
import com.liferay.portal.search.engine.adapter.search.SearchRequestExecutor;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Wade Cao
 */
@Component(service = PageSearchCommands.class)
public class PageSearchCommands {

	public void writeConfig(String username, String password) throws Exception {
		String data =
		"<?xml version=\"1.0\"?> " +
		"<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>\n" +
		"<configuration>\n" +
			"<property>\n"+
				"<name>http.basic.auth.username</name>\n"+
				"<value>" + username + "</value>\n"+
				"<description>basic auth un\n"+
				"</description>\n"+
			"</property>\n"+

			"<property>\n"+
				"<name>http.basic.auth.password</name>\n"+
				"<value>" + password + "</value>\n"+
				"<description>basic auth pw\n"+
				"</description>\n"+
			"</property>\n"+
		"</configuration>";

		try {
            Files.write(
				Paths.get("apache-nutch-1.15/conf/nutch-site.xml"),
				data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

		Thread.sleep(2000);
	}

	public void crawl() throws Exception {
		User user;
		String password;

		user = UserLocalServiceUtil.fetchUserByScreenName(20099, "t");
		password = "t";
		doCrawl(user, password);
		ingest(user);

		user = UserLocalServiceUtil.fetchUserByScreenName(20099, "y");
		password = "y";
		doCrawl(user, password);
		ingest(user);

		System.out.println("DONE CRAWLING");
	}

	public void doCrawl(User user, String password) throws Exception {
		System.out.println("START CRAWLING with " + user.getScreenName());

		try {
			deleteNutchIndex();
		}
		catch (Exception e) {
		}

		createNutchIndex();

		writeConfig(user.getScreenName(), password);

		Runtime rt = Runtime.getRuntime();

		System.out.println("\n");

		Process pr = rt.exec(
			"apache-nutch-1.15/bin/nutch inject " +
			"apache-nutch-1.15/crawl/crawldb " +
			"apache-nutch-1.15/urls/seed.txt");

		BufferedReader stdInput = new BufferedReader(
			new InputStreamReader(pr.getInputStream()));

		String line = null;

		while ((line = stdInput.readLine()) != null) {
			System.out.println(line);
		}

		pr.waitFor();

		System.out.println("\n");

		pr = rt.exec(
			"apache-nutch-1.15/bin/nutch generate " +
			"apache-nutch-1.15/crawl/crawldb " +
			"apache-nutch-1.15/crawl/segments");

		stdInput = new BufferedReader(
			new InputStreamReader(pr.getInputStream()));

		line = null;
		String date = null;

		while ((line = stdInput.readLine()) != null) {
			System.out.println(line);

			if (line.startsWith("Generator: segment:")) {
				date = line.substring(
					line.lastIndexOf("/") + 1, line.length());
			}
		}

		pr.waitFor();

		System.out.println("\n");

		pr = rt.exec(
			"apache-nutch-1.15/bin/nutch fetch " +
			"apache-nutch-1.15/crawl/segments/" + date);

		stdInput = new BufferedReader(
			new InputStreamReader(pr.getInputStream()));

		line = null;

		while ((line = stdInput.readLine()) != null) {
			System.out.println(line);
		}

		pr.waitFor();

		System.out.println("\n");

		pr = rt.exec(
			"apache-nutch-1.15/bin/nutch parse " +
			"apache-nutch-1.15/crawl/segments/" + date);

		stdInput = new BufferedReader(
			new InputStreamReader(pr.getInputStream()));

		line = null;

		while ((line = stdInput.readLine()) != null) {
			System.out.println(line);
		}

		pr.waitFor();

		System.out.println("\n");

		pr = rt.exec(
			"apache-nutch-1.15/bin/nutch index " +
			"apache-nutch-1.15/crawl/crawldb " +
			"apache-nutch-1.15/crawl/segments/" + date + " -addBinaryContent");

		stdInput = new BufferedReader(
			new InputStreamReader(pr.getInputStream()));

		line = null;

		while ((line = stdInput.readLine()) != null) {
			System.out.println(line);
		}

		pr.waitFor();

		Thread.sleep(2000);
	}

	public void ingest(User user) throws Exception {
		System.out.println("INGESTING");

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(new String[]{"nutch"});

		searchSearchRequest.setQuery(new MatchAllQuery());
		searchSearchRequest.setSize(10);

		//only needed if nutch index is not in same cluster as liferay index
		searchSearchRequest.setConnectionId("federated");

		SearchSearchResponse searchSearchResponse =
			searchSearchRequest.accept(searchRequestExecutor);

		Hits hits = searchSearchResponse.getHits();

		for (Document document : hits.getDocs()) {
			Document liferayDoc = new DocumentImpl();

			liferayDoc.addNumber("roleIds", user.getRoleIds());
			liferayDoc.addText("user", user.getScreenName());
			liferayDoc.addText(Field.UID, document.get("url") + "_" + user.getScreenName());
			liferayDoc.addText(Field.TYPE, "doc");
			liferayDoc.addText("page_title", document.get("title"));
			liferayDoc.addText("url", document.get("url"));
			//liferayDoc.addText("groupId", );
			//liferayDoc.addText("roleId", );
			//liferayDoc.addText("plid", );

			String binaryContent = document.get("binaryContent");

			org.jsoup.nodes.Document doc =
				Jsoup.parseBodyFragment(binaryContent);

			//add META elements

			Elements metaElements = doc.getElementsByTag("meta");

			addMetaData(liferayDoc, metaElements, "keywords", "meta_keywords");

			addMetaData(
				liferayDoc, metaElements, "description", "meta_description");

			//add TARGET elements

			CrawlerTarget wcContentCrawlerTarget = new CrawlerTarget(
				"com_liferay_journal_content_web_portlet_JournalContentPortlet",
				"content",
				".journal-content-article");

			CrawlerTarget wcTitleCrawlerTarget = new CrawlerTarget(
				"com_liferay_journal_content_web_portlet_JournalContentPortlet",
				"title",
				".portlet-title-text");

			CrawlerTarget blogContentCrawlerTarget = new CrawlerTarget(
				"com_liferay_blogs_web_portlet_BlogsPortlet",
				"content",
				".widget-content > p");

			CrawlerTarget blogTitleCrawlerTarget = new CrawlerTarget(
				"com_liferay_blogs_web_portlet_BlogsPortlet",
				"title",
				".title");

			CrawlerTarget blogUserNameCrawlerTarget = new CrawlerTarget(
				"com_liferay_blogs_web_portlet_BlogsPortlet",
				"userName",
				".username");

			CrawlerTarget blogAggContentCrawlerTarget = new CrawlerTarget(
				"com_liferay_blogs_web_portlet_BlogsAgreggatorPortlet",
				"content",
				".entry-body > p");

			CrawlerTarget blogAggTitleCrawlerTarget = new CrawlerTarget(
				"com_liferay_blogs_web_portlet_BlogsAgreggatorPortlet",
				"title",
				".entry-title");

			CrawlerTarget wikiTitleCrawlerTarget = new CrawlerTarget(
				"com_liferay_wiki_web_portlet_WikiDisplayPortlet",
				"title",
				".header-title");

			CrawlerTarget wikiContentCrawlerTarget = new CrawlerTarget(
				"com_liferay_wiki_web_portlet_WikiDisplayPortlet",
				"content",
				".wiki-body > p");

			CrawlerTarget mbTitleCrawlerTarget = new CrawlerTarget(
				"com_liferay_message_boards_web_portlet_MBPortlet",
				"title",
				".main-content-body h4");

			CrawlerTarget mbContentCrawlerTarget = new CrawlerTarget(
				"com_liferay_message_boards_web_portlet_MBPortlet",
				"content",
				".message-content");

			CrawlerTarget fragmentCrawlerTarget = new CrawlerTarget(
				"fragment",
				"content",
				null);

			List<CrawlerTarget> crawlerTargets = new ArrayList<>();

			crawlerTargets.add(wcContentCrawlerTarget);
			crawlerTargets.add(wcTitleCrawlerTarget);
			crawlerTargets.add(blogContentCrawlerTarget);
			crawlerTargets.add(blogTitleCrawlerTarget);
			crawlerTargets.add(blogUserNameCrawlerTarget);
			crawlerTargets.add(blogAggContentCrawlerTarget);
			crawlerTargets.add(blogAggTitleCrawlerTarget);
			crawlerTargets.add(wikiTitleCrawlerTarget);
			crawlerTargets.add(wikiContentCrawlerTarget);
			crawlerTargets.add(fragmentCrawlerTarget);
			crawlerTargets.add(mbTitleCrawlerTarget);
			crawlerTargets.add(mbContentCrawlerTarget);

			addTargetFields(liferayDoc, doc, crawlerTargets);

			//INDEX

			index(liferayDoc);
		}

		Thread.sleep(2000);
	}


	protected class CrawlerTarget {

		public CrawlerTarget(
			String portletId,
			String fieldPrefix,
			String contentSelectorQuery) {

			_contentSelectorQuery = contentSelectorQuery;
			_portletId = portletId;
			_fieldPrefix = fieldPrefix;
		}

		public String getPortletId() {
			return _portletId;
		}

		public String getContentSelectorQuery() {
			return _contentSelectorQuery;
		}

		public String getFieldPrefix() {
			return _fieldPrefix;
		}

		private String _contentSelectorQuery;
		private String _portletId;
		private String _fieldPrefix;
	}

	protected void addMetaData(
		Document document, Elements metaElements, String name, String field) {

		for (Element metaElement : metaElements) {
			String elementName = metaElement.attr("name");

			if (elementName.equals(name)) {
				document.addText(field, metaElement.attr("content"));

				return;
			}
		}
	}

	protected void addTargetFields(
		Document liferayDocument, org.jsoup.nodes.Document doc,
		List<CrawlerTarget> crawlerTargets) {

		for (CrawlerTarget crawlerTarget : crawlerTargets) {
			Elements portletOrFragmentElements =
				doc.select(buildPortletSelectorQuery(crawlerTarget));

			for (Element portletOrFragmentElement : portletOrFragmentElements) {
				String cssQuery = crawlerTarget.getContentSelectorQuery();

				String id = portletOrFragmentElement.attr("id");

				if (id.startsWith("fragment")) {
					cssQuery = "#" + id;
				}

				Elements contentElements =
					portletOrFragmentElement.select(cssQuery);

				int count = 0;

				for (Element contentElement : contentElements) {
					String content = contentElement.text();

					liferayDocument.addText(
						buildFieldName(crawlerTarget.getFieldPrefix(), id, count),
						content);

					count++;
				}
			}
		}
	}

	protected String buildFieldName(
		String prefix, String id, int count) {

		StringBundler sb = new StringBundler(5);

		sb.append(prefix);
		sb.append("_");
		sb.append(id);

		if (!id.contains("INSTANCE") && !id.startsWith("fragment")) {
			sb.append("_");
			sb.append(count);
		}

		return sb.toString();
	}

	protected String buildPortletSelectorQuery(CrawlerTarget crawlerTarget) {
		StringBundler sb = new StringBundler(3);

		String portletId = crawlerTarget.getPortletId();

		if (portletId.equals("fragment")) {
			sb.append("div[id~=fragment");
		}
		else {
			sb.append("section[id~=portlet_");
			sb.append(portletId);
		}

		sb.append("*]");

		return sb.toString();
	}

	protected void index(Document document) {
		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			"liferay_page_index", document);

		documentRequestExecutor.executeDocumentRequest(indexDocumentRequest);
	}

	protected void createNutchIndex() throws Exception {
		CreateIndexRequest createIndexRequest = new CreateIndexRequest(
			"nutch");

		StringBundler sb = new StringBundler(14);

		sb.append("{\n");
		sb.append("    \"mappings\": {\n");
		sb.append("        \"doc\": {\n");
		sb.append("            \"date_detection\": false,\n");
		sb.append("            \"dynamic_templates\": [{\n");
		sb.append("                \"template_all_text\": {\n");
		sb.append("                  \"mapping\": {\n");
		sb.append("                    \"store\": true,\n");
		sb.append("                    \"type\": \"text\"\n");
		sb.append("                },\n");
		sb.append("                \"match\": \"*\"");
		sb.append("                }\n");
		sb.append("            }]\n");
		sb.append("        }\n");
		sb.append("    }\n");
		sb.append("}");

		createIndexRequest.setSource(sb.toString());

		searchEngineAdapter.execute(createIndexRequest);

		Thread.sleep(2000);
	}

	protected void deleteNutchIndex() throws Exception {
		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(
			"nutch");

		searchEngineAdapter.execute(deleteIndexRequest);

		Thread.sleep(2000);
	}

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	@Reference
	protected DocumentRequestExecutor documentRequestExecutor;

	@Reference
	protected SearchRequestExecutor searchRequestExecutor;

}