/*
 * LODtagger - http://www.semanticsoftware.info/lodtagger
 *
 * This file is part of the LODtagger package.
 *
 * Copyright (c) 2015, 2016 Semantic Software Lab, http://www.semanticsoftware.info
 *    Rene Witte
 *    Bahar Sateli
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package info.semanticsoftware.lodtagger.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize","PMD.ImmutableField"}) // PMD doesn't understand Gson
public class SpotlightResult {

	@SerializedName("@text")
	private String text;
	
	@SerializedName("Resources")
	private List<SpotlightResource> resources = new ArrayList<SpotlightResource>();
	
	@SerializedName("@policy")
	private String policy;

	@SerializedName("@confidence")
	private double confidence = -1;
	
	@SerializedName("@sparql")
	private String sparql;
	
	@SerializedName("@support")
	private long support = -1;
	
	@SerializedName("@types")
	private transient String types;

	/**
	 * @return the text
	 */
	public final String getText() {
		return text;
	}

	/**
	 * @return the confidence
	 */
	public final double getConfidence() {
		return confidence;
	}

	/**
	 * @return the support
	 */
	public final long getSupport() {
		return support;
	}

	/**
	 * @return the types
	 */
	public final String getTypes() {
		return types;
	}

	/**
	 * @return the sparql
	 */
	public final String getSparql() {
		return sparql;
	}

	/**
	 * @return the policy
	 */
	public final String getPolicy() {
		return policy;
	}
	/**
	 * @return the resources
	 */
	public final List<SpotlightResource> getResources() {
		return resources;
	}

	@Override
	public final String toString(){
		final StringBuffer buffer = new StringBuffer(100);
		buffer.append("text: " + text + "\n" +
				   "confidence: " + confidence + "\n" +
				   "support: " + support + "\n" +
				   "types: " + types + "\n" +
				   "sparql: " + sparql + "\n" +
				   "policy: " + policy + "\n");
		for(final SpotlightResource rsrc : resources){
			buffer.append(rsrc.toString() + "\n");
		}
		return buffer.toString();
	}	
}
