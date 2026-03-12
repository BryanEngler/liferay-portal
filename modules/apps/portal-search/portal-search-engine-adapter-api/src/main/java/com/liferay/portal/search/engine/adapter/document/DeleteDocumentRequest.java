/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.engine.adapter.document;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.engine.adapter.ccr.CrossClusterRequest;

/**
 * @author Michael C. Han
 */
public class DeleteDocumentRequest
	extends CrossClusterRequest
	implements BulkableDocumentRequest<DeleteDocumentResponse> {

	public DeleteDocumentRequest(String indexName, String uid) {
		_indexName = indexName;
		_uid = uid;

		if (ArrayUtil.contains(_UIDS, uid)) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Deleting ", uid, StringPool.NEW_LINE,
						_getStackTraceString()));
			}
		}
	}

	@Override
	public DeleteDocumentResponse accept(
		DocumentRequestExecutor documentRequestExecutor) {

		return documentRequestExecutor.executeDocumentRequest(this);
	}

	public String getIndexName() {
		return _indexName;
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), with no direct replacement
	 */
	@Deprecated
	public String getType() {
		return _type;
	}

	public String getUid() {
		return _uid;
	}

	public boolean isRefresh() {
		return _refresh;
	}

	public void setRefresh(boolean refresh) {
		_refresh = refresh;
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), with no direct replacement
	 */
	@Deprecated
	public void setType(String type) {
		_type = type;
	}

	private String _getStackTraceString() {
		StringBundler sb = new StringBundler(100);

		Thread thread = Thread.currentThread();

		for (StackTraceElement stackTraceElement : thread.getStackTrace()) {
			sb.append(stackTraceElement.toString());
			sb.append(" @@ ");

			if (sb.index() == 100) {
				break;
			}
		}

		sb.setIndex(sb.index() - 1);

		sb.setStringAt(StringPool.BLANK, 0);
		sb.setStringAt(StringPool.BLANK, 1);
		sb.setStringAt(StringPool.BLANK, 2);
		sb.setStringAt(StringPool.BLANK, 3);

		return sb.toString();
	}

	private static final String[] _UIDS = {
		"com.liferay.document.library.kernel.model." +
			"DLFileEntry_PORTLET_10269722",
		"com.liferay.document.library.kernel.model.DLFileEntry_PORTLET_9394323",
		"com.liferay.document.library.kernel.model.DLFileEntry_PORTLET_8997372",
		"com.liferay.document.library.kernel.model.DLFileEntry_PORTLET_8997358",
		"com.liferay.document.library.kernel.model.DLFileEntry_PORTLET_8997344",
		"com.liferay.document.library.kernel.model.DLFileEntry_PORTLET_8997329",
		"com.liferay.document.library.kernel.model.DLFileEntry_PORTLET_8997311",
		"com.liferay.document.library.kernel.model.DLFileEntry_PORTLET_8997301",
		"com.liferay.document.library.kernel.model.DLFileEntry_PORTLET_8997287"
	};

	private static final Log _log = LogFactoryUtil.getLog(
		DeleteDocumentRequest.class);

	private final String _indexName;
	private boolean _refresh;
	private String _type;
	private final String _uid;

}