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
import java.util.Arrays;
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
import org.apache.log4j.Logger;

/**
 * This class make a web service call to a given DBpedia Spolight endpoint and
 * transforms the response to GATE annotations.
 */
@CreoleResource(name = "DBpediaTagger", comment = "Transforms DBpedia Spotlight response objects to GATE annotations.")
public class DBpediaTagger extends AbstractLanguageAnalyser implements ProcessingResource {

	private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(DBpediaTagger.class);

	private static final int HTTP_OK = 200;
	private static final int SPOTLIGHT_SUPPORT = 10;
	private static final double SPOTLIGHT_CONFIDENCE = 0.1;
	
	@CreoleParameter(comment = "Output annotation name", defaultValue = "DBpediaLink")
	@RunTime
	private String outputAnnotationName;

	@CreoleParameter(comment = "Spotlight endpoint URL", defaultValue = "http://localhost:2222/rest/annotate")
	@RunTime
	private String endpoint;

	@CreoleParameter(comment = "Confidence", defaultValue = "0.1")
	@RunTime
	private Double confidence = SPOTLIGHT_CONFIDENCE;
	
	@CreoleParameter(comment = "Support", defaultValue = "10")
	@RunTime
	private Integer support = SPOTLIGHT_SUPPORT;

	@CreoleParameter(comment = "outputASName", defaultValue = "")
	@RunTime
	private String outputASName = "";
		
	/**
	 * @return outputAnnotationName
	 */
	public final String getOutputAnnotationName() {
		return outputAnnotationName;
	}

	/**
	 * @param myOutputAnnotationName outputAnnotationName to set
	 */
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


	/* (non-Javadoc)
	 * @see gate.creole.AbstractProcessingResource#init()
	 */
	@Override
	public final gate.Resource init() throws ResourceInstantiationException {
		return this;
	}

	/* (non-Javadoc)
	 * @see gate.creole.AbstractProcessingResource#reInit()
	 */
	@Override
	public final void reInit() throws ResourceInstantiationException {
		init();
	}

	/* (non-Javadoc)
	 * @see gate.creole.AbstractProcessingResource#execute()
	 */
	@Override
	public final void execute() throws ExecutionException {
		//String docContent = gate.Utils.stringFor(document,document.getAnnotations(inputASName));
		final String docContent = document.getContent().toString();
		final SpotlightResult result = callSpotlight(docContent);
		final List<SpotlightResource> list = result.getResources();
		final AnnotationSet outputAS = document.getAnnotations(outputASName);

		for (final SpotlightResource rsrc : list) {
			final FeatureMap feats = Factory.newFeatureMap();
			feats.put("URI", rsrc.getDbpediaURI());
			feats.put("similarityScore", rsrc.getSimilarityScore());
			final List<String> types = Arrays.asList(rsrc.getTypes().split(","));
			feats.put("types", types); // TODO make optional run-time parameter
			// FIXME workaround for annotations that have "null" as their actual
			// surface form: DBpedia Spotlight returns surfaceForms as unquoted strings which
			// tricks Gson into parsing them as null objects
			if (rsrc.getSurfaceForm() == null) {
				rsrc.setSurfaceForm("null");
			}
			final long endOffset = rsrc.getOffset() + rsrc.getSurfaceForm().length();
			try {
				outputAS.add(rsrc.getOffset(), endOffset, getOutputAnnotationName(), feats);
			} catch (InvalidOffsetException e) {
				LOGGER.error(e);
			}
		}
	}

	private SpotlightResult callSpotlight(final String content) throws ExecutionException {
		try {
			final URL url = new URL(endpoint);
			final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");

			final String input = "text=" + URLEncoder.encode(content, "UTF-8") + "&confidence=" + confidence + "&support" + support;

			conn.setDoOutput(true);
			final DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(input);
			wr.flush();
			wr.close();

			if (conn.getResponseCode() != HTTP_OK) {
				throw new ExecutionException("Failed: Http error code " + conn.getResponseCode());
			}

			final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));

			final Gson gson = new Gson();
			return gson.fromJson(in, SpotlightResult.class);
			
		} catch (MalformedURLException e) {
			LOGGER.error(e);
		} catch (IOException e) {
			LOGGER.error(e);
		}
		return null;
	}
} // class DBpediaTagger
