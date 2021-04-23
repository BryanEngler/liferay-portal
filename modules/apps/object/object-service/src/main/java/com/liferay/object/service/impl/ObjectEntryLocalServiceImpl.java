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

package com.liferay.object.service.impl;

import com.liferay.object.exception.ObjectEntryValuesException;
import com.liferay.object.internal.petra.sql.dsl.DynamicObjectDefinitionTable;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryTable;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.base.ObjectEntryLocalServiceBaseImpl;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.object.service.persistence.ObjectDefinitionPersistence;
import com.liferay.object.service.persistence.ObjectFieldPersistence;
import com.liferay.petra.sql.dsl.Column;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.expression.Expression;
import com.liferay.portal.kernel.systemevent.SystemEvent;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.dao.jdbc.postgresql.PostgreSQLJDBCUtil;
import com.liferay.portal.kernel.dao.jdbc.CurrentConnectionUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.SQLQuery;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.InlineSQLHelper;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.sort.SortFieldBuilder;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.kernel.search.Indexable;
import com.liferay.portal.kernel.search.IndexableType;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;

import com.liferay.portal.search.sort.Sorts;
import com.liferay.petra.sql.dsl.DSLFunctionFactoryUtil;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.portal.vulcan.util.TransformUtil;
import com.liferay.petra.sql.dsl.query.FromStep;
import com.liferay.petra.sql.dsl.query.GroupByStep;
import com.liferay.petra.sql.dsl.query.JoinStep;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.aop.AopService;

import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.ModelHintsUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserTable;
import com.liferay.portal.kernel.search.BaseModelSearchResult;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Indexable;
import com.liferay.portal.kernel.search.IndexableType;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.sort.FieldSort;
import com.liferay.portal.search.sort.SortFieldBuilder;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;

import com.liferay.users.admin.kernel.file.uploads.UserFileUploadsSettings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;
import java.math.BigDecimal;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
@Component(
	property = "model.class.name=com.liferay.object.model.ObjectEntry",
	service = AopService.class
)
public class ObjectEntryLocalServiceImpl
	extends ObjectEntryLocalServiceBaseImpl {

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public ObjectEntry addObjectEntry(
			long userId, long groupId, long objectDefinitionId,
			Map<String, Object> values)
		throws PortalException {

		ObjectEntry objectEntry = objectEntryPersistence.create(
			counterLocalService.increment());

		objectEntry.setGroupId(groupId);

		User user = _userLocalService.getUser(userId);

		objectEntry.setCompanyId(user.getCompanyId());
		objectEntry.setUserId(user.getUserId());
		objectEntry.setUserName(user.getFullName());

		objectEntry.setCreateDate(new Date());
		objectEntry.setModifiedDate(new Date());
		objectEntry.setObjectDefinitionId(objectDefinitionId);
		objectEntry.setStatus(WorkflowConstants.STATUS_DRAFT);
		objectEntry.setStatusByUserId(user.getUserId());
		objectEntry.setStatusDate(new Date());

		objectEntry = objectEntryPersistence.update(objectEntry);

		ObjectDefinition objectDefinition =
			_objectDefinitionPersistence.findByPrimaryKey(objectDefinitionId);

		_insertIntoTable(
			objectEntry.getObjectEntryId(), objectDefinition, values);

		return objectEntry;
	}

	@Override
	public ObjectEntry deleteObjectEntry(long objectEntryId)
		throws PortalException {

		ObjectEntry objectEntry = objectEntryPersistence.findByPrimaryKey(
			objectEntryId);

		return objectEntryLocalService.deleteObjectEntry(objectEntry);
	}

	@Indexable(type = IndexableType.DELETE)
	@Override
	@SystemEvent(type = SystemEventConstants.TYPE_DELETE)
	public ObjectEntry deleteObjectEntry(ObjectEntry objectEntry)
		throws PortalException {

		objectEntry = objectEntryPersistence.remove(objectEntry);

		_deleteFromTable(objectEntry);

		return objectEntry;
	}

	@Override
	public int getObjectEntriesCount(long objectDefinitionId) {
		return objectEntryPersistence.countByObjectDefinitionId(
			objectDefinitionId);
	}

	@Override
	public List<Map<String, Object>> getObjectEntriesValuesList(
			long objectDefinitionId, int[] statuses, int start, int end)
		throws PortalException {

		DynamicObjectDefinitionTable dynamicObjectDefinitionTable =
			_getDynamicObjectDefinitionTable(objectDefinitionId);

		Session session = objectEntryPersistence.openSession();

		try {
			Predicate predicate =
				ObjectEntryTable.INSTANCE.objectDefinitionId.eq(
					objectDefinitionId);

			if (!ArrayUtil.isEmpty(statuses)) {
				predicate = predicate.and(
					ObjectEntryTable.INSTANCE.status.in(
						ArrayUtil.toArray(statuses)));
			}
			
			if (PermissionThreadLocal.getPermissionChecker() != null) {
				predicate.and(
					_inlineSQLHelper.getPermissionWherePredicate(
						dynamicObjectDefinitionTable.getName(),
						dynamicObjectDefinitionTable.getPrimaryKeyColumn()));
			}

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(
				DSLQueryFactoryUtil.selectDistinct(
					dynamicObjectDefinitionTable.getSelectExpressions()
				).from(
					dynamicObjectDefinitionTable
				).innerJoinON(
					ObjectEntryTable.INSTANCE,
					ObjectEntryTable.INSTANCE.objectEntryId.eq(
						dynamicObjectDefinitionTable.getPrimaryKeyColumn())
				).where(
					predicate
				));

			List<Object[]> rows = (List<Object[]>)QueryUtil.list(
				sqlQuery, objectEntryPersistence.getDialect(), start, end);

			List<Map<String, Object>> valuesList = new ArrayList<>(rows.size());

			for (Object[] objects : rows) {
				Map<String, Object> values = _getValues(
					dynamicObjectDefinitionTable, objects);

				valuesList.add(values);
			}

			return valuesList;
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			objectEntryPersistence.closeSession(session);
		}
	}

	@Override
	public Map<String, Object> getObjectEntryValues(long objectEntryId)
		throws PortalException {

		ObjectEntry objectEntry = objectEntryPersistence.findByPrimaryKey(
			objectEntryId);

		DynamicObjectDefinitionTable dynamicObjectDefinitionTable =
			_getDynamicObjectDefinitionTable(
				objectEntry.getObjectDefinitionId());

		Session session = objectEntryPersistence.openSession();

		try {
			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(
				DSLQueryFactoryUtil.selectDistinct(
					dynamicObjectDefinitionTable.getSelectExpressions()
				).from(
					dynamicObjectDefinitionTable
				).where(
					dynamicObjectDefinitionTable.getPrimaryKeyColumn(
					).eq(
						objectEntryId
					)
				));

			List<Object[]> rows = (List<Object[]>)QueryUtil.list(
				sqlQuery, objectEntryPersistence.getDialect(),
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

			if (ListUtil.isEmpty(rows)) {
				return null;
			}

			return _getValues(dynamicObjectDefinitionTable, rows.get(0));
		}
		finally {
			objectEntryPersistence.closeSession(session);
		}
	}

	//@Override
	public BaseModelSearchResult<ObjectEntry> searchObjectEntries(
			long objectDefinitionId, String keywords, LinkedHashMap<String, Object> params,
			int cur, int delta, String orderByField, boolean reverse)
		throws PortalException {

		ObjectDefinition objectDefinition =
			_objectDefinitionPersistence.findByPrimaryKey(objectDefinitionId);

		SearchResponse searchResponse = _searcher.search(
			_getSearchRequest(
				objectDefinition, keywords, params, cur, delta, orderByField,
				reverse));

		SearchHits searchHits = searchResponse.getSearchHits();

		List<ObjectEntry> objectEntries = TransformUtil.transform(
			searchHits.getSearchHits(),
			searchHit -> {
				Document document = searchHit.getDocument();
System.out.println("## Document " + document);
				/*long accountEntryId = document.getLong(Field.ENTRY_CLASS_PK);

				AccountEntry objectEntry = fetchAccountEntry(accountEntryId);

				if (accountEntry == null) {
					Indexer<AccountEntry> indexer =
						IndexerRegistryUtil.getIndexer(AccountEntry.class);

					indexer.delete(
						document.getLong(Field.COMPANY_ID),
						document.getString(Field.UID));
				}*/

				return objectEntryPersistence.create(0);
			});

		return new BaseModelSearchResult<>(
			objectEntries, searchResponse.getTotalHits());
	}

	private SearchRequest _getSearchRequest(
		ObjectDefinition objectDefinition, String keywords, LinkedHashMap<String, Object> params,
		int cur, int delta, String orderByField, boolean reverse) {

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder();

		searchRequestBuilder/*.entryClassNames(
			User.class.getName()
		)*/.emptySearchEnabled(
			true
		/*).highlightEnabled(
			false
		*/).withSearchContext(
			searchContext -> _populateSearchContext(
				searchContext, objectDefinition, keywords, params)
		);

		if (cur != QueryUtil.ALL_POS) {
			searchRequestBuilder.from(cur);
			searchRequestBuilder.size(delta);
		}

		if (Validator.isNotNull(orderByField)) {
			SortOrder sortOrder = SortOrder.ASC;

			if (reverse) {
				sortOrder = SortOrder.DESC;
			}

			/*FieldSort fieldSort = _sorts.field(
				_sortFieldBuilder.getSortField(
					AccountEntry.class.getName(), orderByField),
				sortOrder);

			searchRequestBuilder.sorts(fieldSort);*/
		}

		return searchRequestBuilder.build();
	}

	private void _populateSearchContext(
		SearchContext searchContext, ObjectDefinition objectDefinition, String keywords,
		LinkedHashMap<String, Object> params) {

		searchContext.setAttribute("objectDefinitionId", objectDefinition.getObjectDefinitionId());
		searchContext.setCompanyId(objectDefinition.getCompanyId());

		if (Validator.isNotNull(keywords)) {
			searchContext.setKeywords(keywords);
		}

		/*if (MapUtil.isEmpty(params)) {
			return;
		}

		long[] accountGroupIds = (long[])params.get("accountGroupIds");

		if (ArrayUtil.isNotEmpty(accountGroupIds)) {
			searchContext.setAttribute("accountGroupIds", accountGroupIds);
		}

		long[] accountUserIds = (long[])params.get("accountUserIds");

		if (ArrayUtil.isNotEmpty(accountUserIds)) {
			searchContext.setAttribute("accountUserIds", accountUserIds);
		}

		String[] domains = (String[])params.get("domains");

		if (ArrayUtil.isNotEmpty(domains)) {
			searchContext.setAttribute("domains", domains);
		}

		long[] organizationIds = (long[])params.get("organizationIds");

		if (ArrayUtil.isNotEmpty(organizationIds)) {
			searchContext.setAttribute("organizationIds", organizationIds);
		}

		long parentAccountEntryId = GetterUtil.getLong(
			params.get("parentAccountEntryId"),
			AccountConstants.ACCOUNT_ENTRY_ID_ANY);

		if (parentAccountEntryId != AccountConstants.ACCOUNT_ENTRY_ID_ANY) {
			searchContext.setAttribute(
				"parentAccountEntryId", parentAccountEntryId);
		}

		int status = GetterUtil.getInteger(
			params.get("status"), WorkflowConstants.STATUS_APPROVED);

		searchContext.setAttribute(Field.STATUS, status);

		String type = (String)params.get("type");

		if (Validator.isNotNull(type)) {
			searchContext.setAttribute(Field.TYPE, type);
		}*/
	}

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public ObjectEntry updateObjectEntry(
			long objectEntryId, Map<String, Object> values)
		throws PortalException {

		ObjectEntry objectEntry = objectEntryPersistence.findByPrimaryKey(
			objectEntryId);

		objectEntry.setModifiedDate(new Date());

		objectEntry = objectEntryPersistence.update(objectEntry);

		ObjectDefinition objectDefinition =
			_objectDefinitionPersistence.findByPrimaryKey(
				objectEntry.getObjectDefinitionId());

		_updateTable(objectEntryId, objectDefinition, values);

		return objectEntry;
	}

	private void _deleteFromTable(ObjectEntry objectEntry)
		throws PortalException {

		ObjectDefinition objectDefinition =
			_objectDefinitionPersistence.findByPrimaryKey(
				objectEntry.getObjectDefinitionId());

		runSQL(
			StringBundler.concat(
				"delete from ", objectDefinition.getDBTableName(), " where ",
				objectDefinition.getDBPrimaryKeyColumnName(), " = ",
				objectEntry.getObjectEntryId()));
	}

	private DynamicObjectDefinitionTable _getDynamicObjectDefinitionTable(
			long objectDefinitionId)
		throws PortalException {

		// TODO Cache this across the cluster with proper invalidation when the
		// object definition or its object fields are updated

		return new DynamicObjectDefinitionTable(
			_objectDefinitionPersistence.findByPrimaryKey(objectDefinitionId),
			_objectFieldPersistence.findByObjectDefinitionId(
				objectDefinitionId));
	}

	private Map<String, Object> _getValues(
		DynamicObjectDefinitionTable dynamicObjectDefinitionTable,
		Object[] objects) {

		Map<String, Object> values = new HashMap<>();

		Expression<?>[] selectExpressions =
			dynamicObjectDefinitionTable.getSelectExpressions();

		for (int i = 0; i < selectExpressions.length; i++) {
			Column<?, ?> column = (Column<?, ?>)selectExpressions[i];

			String columnName = column.getName();

			if (columnName.endsWith(StringPool.UNDERLINE)) {
				columnName = columnName.substring(0, columnName.length() - 1);
			}

			_putValue(column, columnName, objects[i], values);
		}

		return values;
	}

	private void _insertIntoTable(
			long objectEntryId, ObjectDefinition objectDefinition,
			Map<String, Object> values)
		throws PortalException {

		Connection connection = CurrentConnectionUtil.getConnection(
			objectEntryPersistence.getDataSource());

		StringBundler sb = new StringBundler();

		sb.append("insert into ");
		sb.append(objectDefinition.getDBTableName());
		sb.append(" (");
		sb.append(objectDefinition.getDBPrimaryKeyColumnName());

		int count = 1;

		DynamicObjectDefinitionTable dynamicObjectDefinitionTable =
			_getDynamicObjectDefinitionTable(
				objectDefinition.getObjectDefinitionId());

		List<ObjectField> objectFields =
			dynamicObjectDefinitionTable.getObjectFields();

		for (ObjectField objectField : objectFields) {
			Object value = values.get(objectField.getName());

			if (value == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"No value was provided for object field " +
							objectField.getName());
				}

				continue;
			}

			sb.append(", ");
			sb.append(objectField.getDBColumnName());

			count++;
		}

		if (count == 1) {
			throw new ObjectEntryValuesException(
				"No values were provided for object definition " +
					objectDefinition.getObjectDefinitionId());
		}

		sb.append(") values (?");

		for (int i = 1; i < count; i++) {
			sb.append(", ?");
		}

		sb.append(")");

		String sql = sb.toString();

		if (_log.isDebugEnabled()) {
			_log.debug("SQL: " + sql);
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				sql)) {

			int index = 1;

			_setColumn(preparedStatement, index++, Types.BIGINT, objectEntryId);

			for (ObjectField objectField : objectFields) {
				Object value = values.get(objectField.getName());

				if (value == null) {
					continue;
				}

				Column<?, ?> column = dynamicObjectDefinitionTable.getColumn(
					objectField.getDBColumnName());

				_setColumn(
					preparedStatement, index++, column.getSQLType(), value);
			}

			preparedStatement.executeUpdate();
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
	}

	private void _putValue(
		Column<?, ?> column, String name, Object object,
		Map<String, Object> values) {

		Class<?> clazz = column.getJavaType();

		if (clazz == BigDecimal.class) {
			values.put(name, (BigDecimal)object);
		}
		else if (clazz == Blob.class) {
			byte[] bytes = null;

			if (object != null) {
				if (object instanceof Blob) {

					// Hypersonic

					Blob blob = (Blob)object;

					try {
						bytes = blob.getBytes(1, (int)blob.length());
					}
					catch (SQLException sqlException) {
						throw new SystemException(sqlException);
					}
				}
				else if (object instanceof byte[]) {

					// MySQL

					bytes = (byte[])object;
				}
				else {
					Class<?> objectClass = object.getClass();

					throw new IllegalArgumentException(
						"Unknown object class " + objectClass.getName());
				}
			}

			values.put(name, bytes);
		}
		else if (clazz == Boolean.class) {
			if (object == null) {
				object = Boolean.FALSE;
			}

			if (object instanceof Byte) {
				Byte byteObject = (Byte)object;

				if (byteObject.intValue() == 0) {
					object = Boolean.FALSE;
				}
				else {
					object = Boolean.TRUE;
				}
			}

			values.put(name, (Boolean)object);
		}
		else if (clazz == Date.class) {
			values.put(name, (Date)object);
		}
		else if (clazz == Double.class) {
			Number number = (Number)object;

			if (number == null) {
				number = Double.valueOf(0D);
			}
			else if (!(number instanceof Double)) {
				number = number.doubleValue();
			}

			values.put(name, number);
		}
		else if (clazz == Integer.class) {
			Number number = (Number)object;

			if (number == null) {
				number = Integer.valueOf(0);
			}
			else if (!(number instanceof Integer)) {
				number = number.intValue();
			}

			values.put(name, number);
		}
		else if (clazz == Long.class) {
			Number number = (Number)object;

			if (number == null) {
				number = Long.valueOf(0L);
			}
			else if (!(number instanceof Long)) {
				number = number.longValue();
			}

			values.put(name, number);
		}
		else if (clazz == String.class) {
			values.put(name, (String)object);
		}
		else {
			throw new IllegalArgumentException(
				"Unknown class " + clazz.getName());
		}
	}

	/**
	 * @see com.liferay.portal.upgrade.util.Table#setColumn
	 */
	private void _setColumn(
			PreparedStatement preparedStatement, int index, int sqlType,
			Object value)
		throws Exception {

		if (sqlType == Types.BIGINT) {
			preparedStatement.setLong(index, GetterUtil.getLong(value));
		}
		else if (sqlType == Types.BLOB) {
			if (PostgreSQLJDBCUtil.isPGStatement(preparedStatement)) {
				PostgreSQLJDBCUtil.setLargeObject(
					preparedStatement, index, (byte[])value);
			}
			else {
				preparedStatement.setBytes(index, (byte[])value);
			}
		}
		else if (sqlType == Types.BOOLEAN) {
			preparedStatement.setBoolean(index, GetterUtil.getBoolean(value));
		}
		else if (sqlType == Types.DATE) {
			Date date = (Date)value;

			preparedStatement.setTimestamp(
				index, new Timestamp(date.getTime()));
		}
		else if (sqlType == Types.DECIMAL) {
			preparedStatement.setBigDecimal(
				index, (BigDecimal)GetterUtil.get(value, BigDecimal.ZERO));
		}
		else if (sqlType == Types.DOUBLE) {
			preparedStatement.setDouble(index, GetterUtil.getDouble(value));
		}
		else if (sqlType == Types.INTEGER) {
			preparedStatement.setInt(index, GetterUtil.getInteger(value));
		}
		else if (sqlType == Types.VARCHAR) {
			preparedStatement.setString(index, String.valueOf(value));
		}
		else {
			throw new IllegalArgumentException("Unknown SQL type " + sqlType);
		}
	}

	private void _updateTable(
			long objectEntryId, ObjectDefinition objectDefinition,
			Map<String, Object> values)
		throws PortalException {

		Connection connection = CurrentConnectionUtil.getConnection(
			objectEntryPersistence.getDataSource());

		StringBundler sb = new StringBundler();

		sb.append("update ");
		sb.append(objectDefinition.getDBTableName());
		sb.append(" set ");

		int count = 0;

		DynamicObjectDefinitionTable dynamicObjectDefinitionTable =
			_getDynamicObjectDefinitionTable(
				objectDefinition.getObjectDefinitionId());

		List<ObjectField> objectFields =
			dynamicObjectDefinitionTable.getObjectFields();

		for (ObjectField objectField : objectFields) {
			Object value = values.get(objectField.getName());

			if (value == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"No value was provided for object field " +
							objectField.getName());
				}

				continue;
			}

			if (count > 0) {
				sb.append(", ");
			}

			sb.append(objectField.getDBColumnName());
			sb.append(" = ?");

			count++;
		}

		if (count == 0) {
			throw new ObjectEntryValuesException(
				"No values were provided for object definition " +
					objectDefinition.getObjectDefinitionId());
		}

		sb.append(" where ");
		sb.append(objectDefinition.getDBPrimaryKeyColumnName());
		sb.append(" = ?");

		String sql = sb.toString();

		if (_log.isDebugEnabled()) {
			_log.debug("SQL: " + sql);
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				sql)) {

			int index = 1;

			for (ObjectField objectField : objectFields) {
				Object value = values.get(objectField.getName());

				if (value == null) {
					continue;
				}

				Column<?, ?> column = dynamicObjectDefinitionTable.getColumn(
					objectField.getDBColumnName());

				_setColumn(
					preparedStatement, index++, column.getSQLType(), value);
			}

			_setColumn(preparedStatement, index++, Types.BIGINT, objectEntryId);

			preparedStatement.executeUpdate();
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryLocalServiceImpl.class);

	@Reference
	private InlineSQLHelper _inlineSQLHelper;

	@Reference
	private ObjectDefinitionPersistence _objectDefinitionPersistence;

	@Reference
	private ObjectFieldPersistence _objectFieldPersistence;

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private SortFieldBuilder _sortFieldBuilder;

	@Reference
	private Sorts _sorts;

}