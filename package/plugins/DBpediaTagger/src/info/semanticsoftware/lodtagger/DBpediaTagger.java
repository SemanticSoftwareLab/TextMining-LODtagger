/*
 * LODtagger - http://www.semanticsoftware.info/lodtagger
 *
 * This file is part of the LODtagger package.
 *
 * Copyright (c) 2015, Semantic Software Lab, http://www.semanticsoftware.info
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

package info.semanticsoftware.lodtagger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import info.semanticsoftware.lodtagger.model.SpotlightResource;
import info.semanticsoftware.lodtagger.model.SpotlightResult;

import com.google.gson.Gson;

import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;

/**
 * This class make a web service call to a given DBpedia Spolight endpoint and
 * transforms the response to GATE annotations.
 */
@CreoleResource(name = "DBpediaTagger", comment = "Transforms DBpedia Spotlight response objects to GATE annotations.")
public class DBpediaTagger extends AbstractLanguageAnalyser implements ProcessingResource {

	private static final long serialVersionUID = 1L;

	private static int HTTP_OK = 200;

	@CreoleParameter(comment = "Output annotation name", defaultValue = "DBpediaLink")
	@RunTime
	private String outputAnnotationName;

	@CreoleParameter(comment = "Spotlight endpoint URL", defaultValue = "http://localhost:2222/rest/annotate")
	@RunTime
	private String endpoint;

	@CreoleParameter(comment = "Confidence", defaultValue = "0.1")
	@RunTime
	private Double confidence = 0.1;
	
	@CreoleParameter(comment = "Support", defaultValue = "10")
	@RunTime
	private Integer support = 10;

	@CreoleParameter(comment = "outputASName", defaultValue = "")
	@RunTime
	private String outputASName = "";
		
	public final String getOutputAnnotationName() {
		return outputAnnotationName;
	}

	public final void setOutputAnnotationName(final String myOutputAnnotationName) {
		this.outputAnnotationName = myOutputAnnotationName;
	}

	/**
	 * @return the endpoint
	 */
	public final String getEndpoint() {
		return endpoint;
	}

	/**
	 * @param myEndpoint
	 *            the endpoint to set
	 */
	public final void setEndpoint(final String myEndpoint) {
		this.endpoint = myEndpoint;
	}

	/**
	 * @return the confidence
	 */
	public final Double getConfidence() {
		return confidence;
	}

	/**
	 * @param myConfidence
	 *            the confidence to set
	 */
	public final void setConfidence(final Double myConfidence) {
		this.confidence = myConfidence;
	}

	/**
	 * @return the support
	 */
	public final Integer getSupport() {
		return support;
	}

	/**
	 * @param mySupport
	 *            the support to set
	 */
	public final void setSupport(final Integer mySupport) {
		this.support = mySupport;
	}

	/**
	 * @return the outputAS
	 */
	public final String getOutputASName() {
		return outputASName;
	}

	/**
	 * @param myOutputASName
	 *            the outputAS name to set
	 */
	public final void setOutputASName(final String myOutputASName) {
		this.outputASName = myOutputASName;
	}


	@Override
	public gate.Resource init() throws ResourceInstantiationException {
		return this;
	}

	@Override
	public void reInit() throws ResourceInstantiationException {
		init();
	}

	@Override
	public void execute() throws ExecutionException {
		//String docContent = gate.Utils.stringFor(document,document.getAnnotations(inputASName));
		String docContent = document.getContent().toString();
		final SpotlightResult result = callSpotlight(docContent);
		final List<SpotlightResource> list = result.getResources();
		AnnotationSet outputAS = document.getAnnotations(outputASName);

		for (SpotlightResource rsrc : list) {
			FeatureMap feats = Factory.newFeatureMap();
			feats.put("URI", rsrc.getURI());
			feats.put("similarityScore", rsrc.getSimilarityScore());
			// FIXME workaround for annotations that have "null" as their actual
			// surface form DBpedia Spotlight returns surfaceForms as unquoted strings which
			// tricks Gson into parsing them as null objects
			if (rsrc.getSurfaceForm() == null) {
				rsrc.setSurfaceForm("null");
			}
			final long endOffset = rsrc.getOffset() + rsrc.getSurfaceForm().length();
			try {
				outputAS.add(rsrc.getOffset(), endOffset, getOutputAnnotationName(), feats);
			} catch (InvalidOffsetException e) {
				e.printStackTrace();
			}
		}
	}

	private SpotlightResult callSpotlight(final String content) {
		try {
			final URL url = new URL(endpoint);
			final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");

			final String input = "text=" + URLEncoder.encode(content, "UTF-8") + "&confidence=" + confidence + "&support" + support;

			conn.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(input);
			wr.flush();
			wr.close();

			if (conn.getResponseCode() != HTTP_OK) {
				throw new RuntimeException("Failed: Http error code " + conn.getResponseCode());
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));

			final Gson gson = new Gson();
			return gson.fromJson(in, SpotlightResult.class);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
} // class DBpediaTagger
