package com.liferay.portal.search.web.search.result;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;

import aQute.bnd.annotation.ProviderType;

/**
 * @author Bryan Engler
 */
@ProviderType
public interface FederatedSearchResults {
	public void add(String source, Hits hits);

	public Hits getHitsFromSource(String source);

	public Document[] getDocumentsFromSource(String source);

	public int getTotalCountFromSource(String source);
}
