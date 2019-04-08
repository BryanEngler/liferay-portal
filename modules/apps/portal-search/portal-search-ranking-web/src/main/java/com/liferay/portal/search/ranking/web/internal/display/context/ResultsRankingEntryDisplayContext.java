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

package com.liferay.portal.search.ranking.web.internal.display.context;

import java.util.Date;

/**
 * @author Wade Cao
 */
public class ResultsRankingEntryDisplayContext {

	public String getAliases() {
		return _aliases;
	}

	public Date getDisplayDate() {
		return _displayDate;
	}

	public String getHiddenResultsCount() {
		return _hiddenResultsCount;
	}

	public String getIndex() {
		return _index;
	}

	public String getKeywords() {
		return _keywords;
	}

	public Date getModifiedDate() {
		return _modifiedDate;
	}

	public String getPinnedResultsCount() {
		return _pinnedResultsCount;
	}

	public String getStatus() {
		return _status;
	}

	public String getUid() {
		return _uid;
	}

	public void setAliases(String aliases) {
		_aliases = aliases;
	}

	public void setDisplayDate(Date displayDate) {
		_displayDate = displayDate;
	}

	public void setHiddenResultsCount(String hiddenResultsCount) {
		_hiddenResultsCount = hiddenResultsCount;
	}

	public void setIndex(String index) {
		_index = index;
	}

	public void setKeywords(String keywords) {
		_keywords = keywords;
	}

	public void setModifiedDate(Date modifiedDate) {
		_modifiedDate = modifiedDate;
	}

	public void setPinnedResultsCount(String pinnedResultsCount) {
		_pinnedResultsCount = pinnedResultsCount;
	}

	public void setStatus(String status) {
		_status = status;
	}

	public void setUid(String uid) {
		_uid = uid;
	}

	private String _aliases;
	private Date _displayDate;
	private String _hiddenResultsCount;
	private String _index;
	private String _keywords;
	private Date _modifiedDate;
	private String _pinnedResultsCount;
	private String _status;
	private String _uid;

}