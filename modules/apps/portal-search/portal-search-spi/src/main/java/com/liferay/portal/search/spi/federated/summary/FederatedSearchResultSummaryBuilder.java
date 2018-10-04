package com.liferay.portal.search.spi.federated.summary;

import aQute.bnd.annotation.ConsumerType;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.summary.FederatedSearchSummary;

/**
 * @author Bryan Engler
 */
@ConsumerType
public interface FederatedSearchResultSummaryBuilder {

	public FederatedSearchSummary getSummary(Document document);

	public String getSourceDisplayName();
}
