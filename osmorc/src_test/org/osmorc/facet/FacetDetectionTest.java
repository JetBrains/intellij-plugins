/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.facet;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.util.indexing.FileContentImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmorc.HeavyOsgiFixtureTestCase;
import org.osmorc.SwingRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
@RunWith(SwingRunner.class)
public class FacetDetectionTest extends HeavyOsgiFixtureTestCase {
  @Test
  public void testDetectFacet() throws IOException {
    var module = ModuleManager.getInstance(myFixture.getProject()).findModuleByName("t0");
    assertNotNull(module);

    var detector = new OsmorcFrameworkDetector();
    var filter = detector.createSuitableFilePattern();
    var manifestFile = myTempDirFixture.getFile("t0/src/META-INF/MANIFEST.MF");
    assertTrue(filter.accepts(FileContentImpl.createByFile(manifestFile)));

    var facetConfiguration = detector.createConfiguration(List.of(manifestFile));
    assertNotNull(facetConfiguration);
    assertEquals(manifestFile.getPath(), facetConfiguration.getManifestLocation());
    assertFalse(facetConfiguration.isUseProjectDefaultManifestFileLocation());

    var facet = OsmorcFacetType.getInstance().createFacet(module, "OSGi", facetConfiguration, null);
    var model = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      detector.setupFacet(facet, model);
      assertEquals("src/META-INF/MANIFEST.MF", facetConfiguration.getManifestLocation());
    }
    finally {
      model.dispose();
    }
  }

  @Test
  public void testDetectBundlorFacet() throws IOException {
    var moduleManager = ModuleManager.getInstance(myFixture.getProject());
    var module = moduleManager.findModuleByName("t2");
    assertNotNull(module);

    var detector = new OsmorcFrameworkDetector();
    var filter = detector.createSuitableFilePattern();
    var manifestFile = myTempDirFixture.getFile("t2/src/META-INF/template.mf");
    assertTrue(filter.accepts(FileContentImpl.createByFile(manifestFile)));

    var facetConfiguration = detector.createConfiguration(List.of(manifestFile));
    assertNotNull(facetConfiguration);
    assertEquals(manifestFile.getPath(), facetConfiguration.getManifestLocation());
    assertFalse(facetConfiguration.isUseProjectDefaultManifestFileLocation());

    var facet = OsmorcFacetType.getInstance().createFacet(module, "OSGi", facetConfiguration, null);
    var model = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      detector.setupFacet(facet, model);
      assertEquals("", facetConfiguration.getManifestLocation());
      assertEquals("src/META-INF/template.mf", facetConfiguration.getBundlorFileLocation());
      assertTrue(facetConfiguration.isUseBundlorFile());
    }
    finally {
      model.dispose();
    }
  }

  @Test
  public void testDetectNoFacet() throws IOException {
    var filter = new OsmorcFrameworkDetector().createSuitableFilePattern();
    var manifestFile = myTempDirFixture.getFile("t1/src/META-INF/MANIFEST.MF");
    assertFalse(filter.accepts(FileContentImpl.createByFile(manifestFile)));
  }

  @Test
  public void testDetectingFacetByBndFile() throws IOException {
    var module = ModuleManager.getInstance(myFixture.getProject()).findModuleByName("t3");
    assertNotNull(module);

    var detector = new BndOsmorcFrameworkDetector();
    var filter = detector.createSuitableFilePattern();
    var bndFile = myTempDirFixture.getFile("t3/bnd.bnd");
    assertTrue(filter.accepts(FileContentImpl.createByFile(bndFile)));

    var facetConfiguration = detector.createConfiguration(List.of(bndFile));
    assertNotNull(facetConfiguration);
    assertEquals(bndFile.getPath(), facetConfiguration.getBndFileLocation());
    assertFalse(facetConfiguration.isUseProjectDefaultManifestFileLocation());

    var facet = OsmorcFacetType.getInstance().createFacet(module, "OSGi", facetConfiguration, null);
    var model = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      detector.setupFacet(facet, model);
      assertEquals("", facetConfiguration.getManifestLocation());
      assertEquals("bnd.bnd", facetConfiguration.getBndFileLocation());
      assertTrue(facetConfiguration.isUseBndFile());
    }
    finally {
      model.dispose();
    }
  }
}
