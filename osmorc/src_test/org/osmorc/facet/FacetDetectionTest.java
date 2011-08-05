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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import com.intellij.util.indexing.FileContent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmorc.SwingRunner;
import org.osmorc.TestUtil;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
@RunWith(SwingRunner.class)
public class FacetDetectionTest {
    private final TempDirTestFixture myTempDirFixture;
    private final IdeaProjectTestFixture fixture;
    private TestDialog orgTestDialog;

    public FacetDetectionTest() {
        myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
        fixture = TestUtil.createTestFixture();
    }

    @Before
    public void setUp() throws Exception {
        myTempDirFixture.setUp();
        fixture.setUp();

        orgTestDialog = Messages.setTestDialog(new TestDialog() {

          public int show(String message) {
            return DialogWrapper.CANCEL_EXIT_CODE;
          }
        });
        TestUtil.loadModules("FacetDetectionTest", fixture.getProject(), myTempDirFixture.getTempDirPath());
    }

    @After
    public void tearDown() throws Exception {
        Messages.setTestDialog(orgTestDialog);
        fixture.tearDown();
        myTempDirFixture.tearDown();
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Test
    public void testDetectFacet() throws IOException {
        ModuleManager moduleManager = ModuleManager.getInstance(fixture.getProject());
        Module t0 = moduleManager.findModuleByName("t0");

        OsmorcFrameworkDetector detector = new OsmorcFrameworkDetector();

        ElementPattern<FileContent> filter = detector.createSuitableFilePattern();

        VirtualFile manifestFile = myTempDirFixture.getFile("t0/src/META-INF/MANIFEST.MF");

        assertThat(filter.accepts(new FileContent(manifestFile, manifestFile.contentsToByteArray())), equalTo(true));

        OsmorcFacetConfiguration osmorcFacetConfiguration = detector.createConfiguration(Collections.singletonList(manifestFile));
        assertThat(osmorcFacetConfiguration.getManifestLocation(), equalTo(manifestFile.getPath()));
        assertThat(osmorcFacetConfiguration.isUseProjectDefaultManifestFileLocation(), equalTo(false));

        OsmorcFacet osmorcFacet = OsmorcFacetType.getInstance().createFacet(t0, "OSGi", osmorcFacetConfiguration, null);

        ModifiableRootModel model = ModuleRootManager.getInstance(t0).getModifiableModel();
        try {
            detector.setupFacet(osmorcFacet, model);

            assertThat(osmorcFacetConfiguration.getManifestLocation(), equalTo("src/META-INF/MANIFEST.MF"));
        }
        finally {
            model.dispose();
        }
    }

     @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Test
    public void testDetectBundlorFacet() throws IOException {
        ModuleManager moduleManager = ModuleManager.getInstance(fixture.getProject());
        Module t2 = moduleManager.findModuleByName("t2");
        final OsmorcFrameworkDetector detector = new OsmorcFrameworkDetector();

        ElementPattern<FileContent> filter = detector.createSuitableFilePattern();

        VirtualFile manifestFile = myTempDirFixture.getFile("t2/src/META-INF/template.mf");

        assertThat(filter.accepts(new FileContent(manifestFile, manifestFile.contentsToByteArray())), equalTo(true));

        OsmorcFacetConfiguration osmorcFacetConfiguration = detector.createConfiguration(Collections.singletonList(manifestFile));
        assertThat(osmorcFacetConfiguration.getManifestLocation(), equalTo(manifestFile.getPath()));
        assertThat(osmorcFacetConfiguration.isUseProjectDefaultManifestFileLocation(), equalTo(false));

        OsmorcFacet osmorcFacet = OsmorcFacetType.getInstance().createFacet(t2, "OSGi", osmorcFacetConfiguration, null);

        ModifiableRootModel model = ModuleRootManager.getInstance(t2).getModifiableModel();
        try {
            detector.setupFacet(osmorcFacet, model);

            assertThat(osmorcFacetConfiguration.getManifestLocation(), equalTo(""));
            assertThat(osmorcFacetConfiguration.getBundlorFileLocation(), equalTo("src/META-INF/template.mf"));
            assertThat(osmorcFacetConfiguration.isUseBundlorFile(), equalTo(true));

        }
        finally {
            model.dispose();
        }
    }
    @SuppressWarnings({"unchecked"})
    @Test
    public void testDetectNoFacet() throws IOException {
        ElementPattern<FileContent> filter = new OsmorcFrameworkDetector().createSuitableFilePattern();
        VirtualFile manifestFile = myTempDirFixture.getFile("t1/src/META-INF/MANIFEST.MF");

        assertThat(filter.accepts(new FileContent(manifestFile, manifestFile.contentsToByteArray())), equalTo(false));
    }
}
