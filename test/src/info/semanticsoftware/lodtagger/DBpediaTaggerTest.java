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

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.util.GateException;
import gate.util.OffsetComparator;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Bahar Sateli
 */
public class DBpediaTaggerTest {
    private static CorpusController dbpediaTaggerApp;
    private transient Corpus testCorpus;
    
    @BeforeClass
    public static void loadApp() throws MalformedURLException, IOException, GateException  {
        if (!Gate.isInitialised()) {
            Gate.init();
        }
        
        final Properties sysProps = System.getProperties();
        final String appName = sysProps.getProperty("dbpediatagger.test.app.name");
        dbpediaTaggerApp = (CorpusController) PersistenceManager.loadObjectFromFile(new File(appName));
    }
    
    @Before
    public void setUp() throws GateException  {
        testCorpus = Factory.newCorpus("Test corpus");
        dbpediaTaggerApp.setCorpus(testCorpus);
    }
    
    @After
    public void tearDown() throws Exception {
        Factory.deleteResource(testCorpus);
    }
    
    @AfterClass
    public static void unloadApp() throws Exception {
        Factory.deleteResource(dbpediaTaggerApp);
    }
    
    @Test
    public final void testNE() throws Exception {
        final Document testDoc = Factory.newDocument("I live in Canada.");
        try {
            testCorpus.add(testDoc);
            try {
                dbpediaTaggerApp.execute();
                
                // Check the results
                final AnnotationSet annots = testDoc.getAnnotations();
                assertNotNull("test document has no annotations!", annots);
                
                final AnnotationSet dbpediaNEs = annots.get("DBpediaNE");
                assertNotNull("test document has no DBpediaNE annotations!", dbpediaNEs);
                
                final List<Annotation> neList = new ArrayList<Annotation>(dbpediaNEs);
                // sort in document order
                Collections.sort(neList, new OffsetComparator());
                assertEquals("Document should have one DBpediaNE", 1, neList.size());
                
                final Annotation ne = neList.get(0);
                assertEquals("The DBpediaNE annotation must be grounded to http://dbpedia.org/resource/Canada.", "http://dbpedia.org/resource/Canada", getNEURIString(ne));
                
            } finally {
                testCorpus.remove(testDoc);
            }
        } finally {
            Factory.deleteResource(testDoc);
        }
    }
    
    private static String getNEURIString(final Annotation ann) {
        final FeatureMap features = ann.getFeatures();
        return (String)features.get("URI");
    }
}
