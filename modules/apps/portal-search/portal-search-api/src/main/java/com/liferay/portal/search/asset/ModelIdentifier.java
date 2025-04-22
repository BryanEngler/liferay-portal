package com.liferay.portal.search.asset;

public interface ModelIdentifier {
	public String getClassName();

	public String getEntityERC();

	public String getGroupERC();

	public boolean hasERCInfo();

	public void setClassName(String className);

	public void setEntityERC(String entityERC);

	public void setGroupERC(String groupERC);

}
