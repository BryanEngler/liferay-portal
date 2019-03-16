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

package com.liferay.headless.search.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import javax.annotation.Generated;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Bryan Engler
 * @generated
 */
@Generated("")
@GraphQLName("Results")
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "Results")
public class Results {

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	@JsonIgnore
	public void setResourceType(
		UnsafeSupplier<String, Exception> resourceTypeUnsafeSupplier) {

		try {
			resourceType = resourceTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String resourceType;

	public String getResults() {
		return results;
	}

	public void setResults(String results) {
		this.results = results;
	}

	@JsonIgnore
	public void setResults(
		UnsafeSupplier<String, Exception> resultsUnsafeSupplier) {

		try {
			results = resultsUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String results;

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		sb.append("\"resourceType\": ");

		sb.append("\"");
		sb.append(resourceType);
		sb.append("\"");
		sb.append(", ");

		sb.append("\"results\": ");

		sb.append("\"");
		sb.append(results);
		sb.append("\"");

		sb.append("}");

		return sb.toString();
	}

}