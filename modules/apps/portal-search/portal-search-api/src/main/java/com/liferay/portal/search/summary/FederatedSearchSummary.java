package com.liferay.portal.search.summary;

import aQute.bnd.annotation.ProviderType;

/**
 * @author Bryan Engler
 */
@ProviderType
public interface FederatedSearchSummary extends Summary {

	public String getURL();

}