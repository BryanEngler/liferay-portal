package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.Html;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.web.internal.display.context.KeywordsSupplier;
import com.liferay.portal.search.web.internal.display.context.PortalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.display.context.PortletURLFactory;
import com.liferay.portal.search.web.internal.display.context.SearchContainerSupplier;

import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;

import javax.servlet.http.HttpServletRequest;

public class SearchPortletSearchContainerSupplier implements SearchContainerSupplier {

	private final PortalHttpServletRequestSupplier _requestSupplier;
	private final KeywordsSupplier keywordsSupplier;
	private final Language language;
	private final Html html;
	private final RenderRequest renderRequest;
	private final PortletURLFactory portletURLFactory;

	public SearchPortletSearchContainerSupplier(
		RenderRequest renderRequest,
		Language language,
		PortalHttpServletRequestSupplier requestSupplier,
		Html html,
		KeywordsSupplier keywordsSupplier,
		PortletURLFactory portletURLFactory
		) {

			this.renderRequest = renderRequest;
			this.language = language;
			_requestSupplier = requestSupplier;
			this.html = html;
			this.keywordsSupplier = keywordsSupplier;
			this.portletURLFactory = portletURLFactory;
	}

	@Override
	public SearchContainer getSearchContainer() {
		HttpServletRequest request = _requestSupplier.get();
		String keywords = StringUtil.trim(keywordsSupplier.getKeywords());

		String emptyResultMessage = language.format(
			request, "no-results-were-found-that-matched-the-keywords-x",
			"<strong>" + html.escape(keywords) + "</strong>", false);

		SearchContainer<Document> searchContainer = new SearchContainer<>(
			renderRequest, getPortletURL(), null, emptyResultMessage);

		return searchContainer;
	}

	protected PortletURL getPortletURL() {
		try {
			return portletURLFactory.getPortletURL();
		}
		catch (PortletException pe) {
			throw new RuntimeException(pe);
		}
	}

}