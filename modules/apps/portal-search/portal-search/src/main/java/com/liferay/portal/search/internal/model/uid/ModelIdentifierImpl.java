package com.liferay.portal.search.internal.model.uid;

import com.liferay.portal.search.asset.ModelIdentifier;

public class ModelIdentifierImpl implements ModelIdentifier {

	private String _className;
	private String _entityERC;
	private String _groupERC;

	@Override
	public String getClassName() {
		return _className;
	}

	@Override
	public String getEntityERC() {
		return _entityERC;
	}

	@Override
	public String getGroupERC() {
		return _groupERC;
	}

	@Override
	public boolean hasERCInfo() {
		if (_entityERC != null && _groupERC != null) {
			return true;
		}

		return false;
	}

	@Override
	public void setClassName(String className) {
		_className = className;
	}

	@Override
	public void setEntityERC(String entityERC) {
		_entityERC = entityERC;
	}

	@Override
	public void setGroupERC(String groupERC) {
		_groupERC = groupERC;
	}
}
