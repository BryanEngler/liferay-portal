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

package com.liferay.portal.search.groupby;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.kernel.search.Sort;

import java.io.Serializable;

/**
 * @author Bryan Engler
 * @author Michael C. Han
 */
@ProviderType
public class GroupBy implements Serializable {

	public GroupBy(String field) {
		_field = field;
	}

	public int getDocsSize() {
		return _docsSize;
	}

	public Sort[] getDocsSorts() {
		return _docsSorts;
	}

	public int getDocsStart() {
		return _docsStart;
	}

	public String getField() {
		return _field;
	}

	public int getTermsSize() {
		return _termsSize;
	}

	public Sort[] getTermsSorts() {
		return _termsSorts;
	}

	public int getTermsStart() {
		return _termsStart;
	}

	public void setDocsSize(int docsSize) {
		_docsSize = docsSize;
	}

	public void setDocsSorts(Sort[] docsSorts) {
		_docsSorts = docsSorts;
	}

	public void setDocsStart(int docsStart) {
		_docsStart = docsStart;
	}

	public void setField(String field) {
		_field = field;
	}

	public void setTermsSize(int termsSize) {
		_termsSize = termsSize;
	}

	public void setTermsSorts(Sort[] termsSorts) {
		_termsSorts = termsSorts;
	}

	public void setTermsStart(int termsStart) {
		_termsStart = termsStart;
	}

	private int _docsSize;
	private Sort[] _docsSorts;
	private int _docsStart;
	private String _field;
	private int _termsSize;
	private Sort[] _termsSorts;
	private int _termsStart;

}