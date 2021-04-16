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

package com.liferay.object.service.persistence;

import com.liferay.object.exception.NoSuchRelationshipException;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.portal.kernel.service.persistence.BasePersistence;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the object relationship service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Marco Leo
 * @see ObjectRelationshipUtil
 * @generated
 */
@ProviderType
public interface ObjectRelationshipPersistence
	extends BasePersistence<ObjectRelationship> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link ObjectRelationshipUtil} to access the object relationship persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

	/**
	 * Returns all the object relationships where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the matching object relationships
	 */
	public java.util.List<ObjectRelationship> findByUuid(String uuid);

	/**
	 * Returns a range of all the object relationships where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>ObjectRelationshipModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of object relationships
	 * @param end the upper bound of the range of object relationships (not inclusive)
	 * @return the range of matching object relationships
	 */
	public java.util.List<ObjectRelationship> findByUuid(
		String uuid, int start, int end);

	/**
	 * Returns an ordered range of all the object relationships where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>ObjectRelationshipModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of object relationships
	 * @param end the upper bound of the range of object relationships (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching object relationships
	 */
	public java.util.List<ObjectRelationship> findByUuid(
		String uuid, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
			orderByComparator);

	/**
	 * Returns an ordered range of all the object relationships where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>ObjectRelationshipModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of object relationships
	 * @param end the upper bound of the range of object relationships (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching object relationships
	 */
	public java.util.List<ObjectRelationship> findByUuid(
		String uuid, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first object relationship in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching object relationship
	 * @throws NoSuchRelationshipException if a matching object relationship could not be found
	 */
	public ObjectRelationship findByUuid_First(
			String uuid,
			com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
				orderByComparator)
		throws NoSuchRelationshipException;

	/**
	 * Returns the first object relationship in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching object relationship, or <code>null</code> if a matching object relationship could not be found
	 */
	public ObjectRelationship fetchByUuid_First(
		String uuid,
		com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
			orderByComparator);

	/**
	 * Returns the last object relationship in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching object relationship
	 * @throws NoSuchRelationshipException if a matching object relationship could not be found
	 */
	public ObjectRelationship findByUuid_Last(
			String uuid,
			com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
				orderByComparator)
		throws NoSuchRelationshipException;

	/**
	 * Returns the last object relationship in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching object relationship, or <code>null</code> if a matching object relationship could not be found
	 */
	public ObjectRelationship fetchByUuid_Last(
		String uuid,
		com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
			orderByComparator);

	/**
	 * Returns the object relationships before and after the current object relationship in the ordered set where uuid = &#63;.
	 *
	 * @param objectRelationshipId the primary key of the current object relationship
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next object relationship
	 * @throws NoSuchRelationshipException if a object relationship with the primary key could not be found
	 */
	public ObjectRelationship[] findByUuid_PrevAndNext(
			long objectRelationshipId, String uuid,
			com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
				orderByComparator)
		throws NoSuchRelationshipException;

	/**
	 * Removes all the object relationships where uuid = &#63; from the database.
	 *
	 * @param uuid the uuid
	 */
	public void removeByUuid(String uuid);

	/**
	 * Returns the number of object relationships where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the number of matching object relationships
	 */
	public int countByUuid(String uuid);

	/**
	 * Returns all the object relationships where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the matching object relationships
	 */
	public java.util.List<ObjectRelationship> findByUuid_C(
		String uuid, long companyId);

	/**
	 * Returns a range of all the object relationships where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>ObjectRelationshipModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of object relationships
	 * @param end the upper bound of the range of object relationships (not inclusive)
	 * @return the range of matching object relationships
	 */
	public java.util.List<ObjectRelationship> findByUuid_C(
		String uuid, long companyId, int start, int end);

	/**
	 * Returns an ordered range of all the object relationships where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>ObjectRelationshipModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of object relationships
	 * @param end the upper bound of the range of object relationships (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching object relationships
	 */
	public java.util.List<ObjectRelationship> findByUuid_C(
		String uuid, long companyId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
			orderByComparator);

	/**
	 * Returns an ordered range of all the object relationships where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>ObjectRelationshipModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of object relationships
	 * @param end the upper bound of the range of object relationships (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching object relationships
	 */
	public java.util.List<ObjectRelationship> findByUuid_C(
		String uuid, long companyId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first object relationship in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching object relationship
	 * @throws NoSuchRelationshipException if a matching object relationship could not be found
	 */
	public ObjectRelationship findByUuid_C_First(
			String uuid, long companyId,
			com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
				orderByComparator)
		throws NoSuchRelationshipException;

	/**
	 * Returns the first object relationship in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching object relationship, or <code>null</code> if a matching object relationship could not be found
	 */
	public ObjectRelationship fetchByUuid_C_First(
		String uuid, long companyId,
		com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
			orderByComparator);

	/**
	 * Returns the last object relationship in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching object relationship
	 * @throws NoSuchRelationshipException if a matching object relationship could not be found
	 */
	public ObjectRelationship findByUuid_C_Last(
			String uuid, long companyId,
			com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
				orderByComparator)
		throws NoSuchRelationshipException;

	/**
	 * Returns the last object relationship in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching object relationship, or <code>null</code> if a matching object relationship could not be found
	 */
	public ObjectRelationship fetchByUuid_C_Last(
		String uuid, long companyId,
		com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
			orderByComparator);

	/**
	 * Returns the object relationships before and after the current object relationship in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param objectRelationshipId the primary key of the current object relationship
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next object relationship
	 * @throws NoSuchRelationshipException if a object relationship with the primary key could not be found
	 */
	public ObjectRelationship[] findByUuid_C_PrevAndNext(
			long objectRelationshipId, String uuid, long companyId,
			com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
				orderByComparator)
		throws NoSuchRelationshipException;

	/**
	 * Removes all the object relationships where uuid = &#63; and companyId = &#63; from the database.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 */
	public void removeByUuid_C(String uuid, long companyId);

	/**
	 * Returns the number of object relationships where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the number of matching object relationships
	 */
	public int countByUuid_C(String uuid, long companyId);

	/**
	 * Caches the object relationship in the entity cache if it is enabled.
	 *
	 * @param objectRelationship the object relationship
	 */
	public void cacheResult(ObjectRelationship objectRelationship);

	/**
	 * Caches the object relationships in the entity cache if it is enabled.
	 *
	 * @param objectRelationships the object relationships
	 */
	public void cacheResult(
		java.util.List<ObjectRelationship> objectRelationships);

	/**
	 * Creates a new object relationship with the primary key. Does not add the object relationship to the database.
	 *
	 * @param objectRelationshipId the primary key for the new object relationship
	 * @return the new object relationship
	 */
	public ObjectRelationship create(long objectRelationshipId);

	/**
	 * Removes the object relationship with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param objectRelationshipId the primary key of the object relationship
	 * @return the object relationship that was removed
	 * @throws NoSuchRelationshipException if a object relationship with the primary key could not be found
	 */
	public ObjectRelationship remove(long objectRelationshipId)
		throws NoSuchRelationshipException;

	public ObjectRelationship updateImpl(ObjectRelationship objectRelationship);

	/**
	 * Returns the object relationship with the primary key or throws a <code>NoSuchRelationshipException</code> if it could not be found.
	 *
	 * @param objectRelationshipId the primary key of the object relationship
	 * @return the object relationship
	 * @throws NoSuchRelationshipException if a object relationship with the primary key could not be found
	 */
	public ObjectRelationship findByPrimaryKey(long objectRelationshipId)
		throws NoSuchRelationshipException;

	/**
	 * Returns the object relationship with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param objectRelationshipId the primary key of the object relationship
	 * @return the object relationship, or <code>null</code> if a object relationship with the primary key could not be found
	 */
	public ObjectRelationship fetchByPrimaryKey(long objectRelationshipId);

	/**
	 * Returns all the object relationships.
	 *
	 * @return the object relationships
	 */
	public java.util.List<ObjectRelationship> findAll();

	/**
	 * Returns a range of all the object relationships.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>ObjectRelationshipModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of object relationships
	 * @param end the upper bound of the range of object relationships (not inclusive)
	 * @return the range of object relationships
	 */
	public java.util.List<ObjectRelationship> findAll(int start, int end);

	/**
	 * Returns an ordered range of all the object relationships.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>ObjectRelationshipModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of object relationships
	 * @param end the upper bound of the range of object relationships (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of object relationships
	 */
	public java.util.List<ObjectRelationship> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
			orderByComparator);

	/**
	 * Returns an ordered range of all the object relationships.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>ObjectRelationshipModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of object relationships
	 * @param end the upper bound of the range of object relationships (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of object relationships
	 */
	public java.util.List<ObjectRelationship> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<ObjectRelationship>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Removes all the object relationships from the database.
	 */
	public void removeAll();

	/**
	 * Returns the number of object relationships.
	 *
	 * @return the number of object relationships
	 */
	public int countAll();

}