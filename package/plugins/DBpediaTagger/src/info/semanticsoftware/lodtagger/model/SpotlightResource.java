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

import com.google.gson.annotations.SerializedName;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize","PMD.ImmutableField"}) // PMD doesn't understand Gson
public class SpotlightResource {

	@SerializedName("@URI")
	private String dbpediaURI; // NOPMD by rene on 10/7/16 5:24 PM

	@SerializedName("@support")
	private long support = -1;

	@SerializedName("@types")
	private String types;

	@SerializedName("@surfaceForm")
	private String surfaceForm;

	@SerializedName("@offset")
	private long offset = -1;

	@SerializedName("@similarityScore")
	private double similarityScore = -1;

	@SerializedName("@percentageOfSecondRank")
	private double percentageOfSecondRank = -1;

	/**
	 * @return the dbpediaURI
	 */
	public final String getDbpediaURI() {
		return dbpediaURI;
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
	 * @return the surfaceForm
	 */
	public final String getSurfaceForm() {
		return surfaceForm;
	}
	
	/**
	 * Set the surfaceForm.
	 * @param input the surfaceForm to set
	 */
	public final void setSurfaceForm(final String input) {
		surfaceForm = input;
	}

	/**
	 * @return the offset
	 */
	public final long getOffset() {
		return offset;
	}

	/**
	 * @return the similarityScore
	 */
	public final double getSimilarityScore() {
		return similarityScore;
	}

	/**
	 * @return the percentageOfSecondRank
	 */
	public final double getPercentageOfSecondRank() {
		return percentageOfSecondRank;
	}
	
	@Override
	public final String toString(){
		return "\tURI: " + dbpediaURI + "\n" + 
			   "\tsupport: " + support  + "\n" +
			   "\ttypes: " + types  + "\n" +
			   "\tsurfaceForm: " + surfaceForm  + "\n" +
			   "\toffset: " + offset  + "\n" +
			   "\tsimilarityScore: " + similarityScore  + "\n" +
			   "\tpercentageOfSecondRank: " + percentageOfSecondRank + "\n";
	}
}
