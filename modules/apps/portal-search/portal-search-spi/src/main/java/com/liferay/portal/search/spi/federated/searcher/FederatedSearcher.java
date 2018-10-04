package com.liferay.portal.search.spi.federated.searcher;

import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;

import aQute.bnd.annotation.ConsumerType;

/**
 * @author Bryan Engler
 */
@ConsumerType
public interface FederatedSearcher {
	public Hits getHits(SearchContext searchContext);

	public String getSourceDisplayName();

}
