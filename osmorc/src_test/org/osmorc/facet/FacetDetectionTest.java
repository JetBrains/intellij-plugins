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

import com.intellij.facet.FacetManager;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.TestUtil;
import org.osmorc.manifest.ManifestFileTypeFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
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
    public void testDetectFacet() {
        ModuleManager moduleManager = ModuleManager.getInstance(fixture.getProject());
        final List<Object> arguments = new ArrayList<Object>();
        Module t0 = moduleManager.findModuleByName("t0");
        FacetDetectorRegistry registry = createMock(FacetDetectorRegistry.class);
        registry.registerUniversalDetector(same(ManifestFileTypeFactory.MANIFEST), (VirtualFileFilter) anyObject(), (FacetDetector) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                arguments.addAll(Arrays.asList(getCurrentArguments()));
                return null;
            }
        });

        replay(registry);

        OsmorcFacetType.INSTANCE.registerDetectors(registry);
        VirtualFileFilter filter = (VirtualFileFilter) arguments.get(1);
        FacetDetector<VirtualFile, OsmorcFacetConfiguration> detector = (FacetDetector<VirtualFile, OsmorcFacetConfiguration>) arguments.get(2);

        VirtualFile manifestFile = myTempDirFixture.getFile("t0/src/META-INF/MANIFEST.MF");

        assertThat(filter.accept(manifestFile), equalTo(true));

        OsmorcFacetConfiguration osmorcFacetConfiguration = detector.detectFacet(manifestFile, new ArrayList<OsmorcFacetConfiguration>());
        assertThat(osmorcFacetConfiguration.getManifestLocation(), equalTo(manifestFile.getPath()));
        assertThat(osmorcFacetConfiguration.isUseProjectDefaultManifestFileLocation(), equalTo(false));

        OsmorcFacet osmorcFacet = OsmorcFacetType.INSTANCE.createFacet(t0, "OSGi", osmorcFacetConfiguration, null);

        ModifiableRootModel model = ModuleRootManager.getInstance(t0).getModifiableModel();
        try {
            detector.beforeFacetAdded(osmorcFacet, FacetManager.getInstance(t0), model);

            assertThat(osmorcFacetConfiguration.getManifestLocation(), equalTo("src/META-INF/MANIFEST.MF"));
        }
        finally {
            model.dispose();
        }

        verify(registry);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testDetectNoFacet() {
        final List<Object> arguments = new ArrayList<Object>();
        FacetDetectorRegistry registry = createMock(FacetDetectorRegistry.class);
        registry.registerUniversalDetector(same(ManifestFileTypeFactory.MANIFEST), (VirtualFileFilter) anyObject(), (FacetDetector) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                arguments.addAll(Arrays.asList(getCurrentArguments()));
                return null;
            }
        });

        replay(registry);

        OsmorcFacetType.INSTANCE.registerDetectors(registry);
        VirtualFileFilter filter = (VirtualFileFilter) arguments.get(1);
        VirtualFile manifestFile = myTempDirFixture.getFile("t1/src/META-INF/MANIFEST.MF");

        assertThat(filter.accept(manifestFile), equalTo(false));
        verify(registry);
    }
}
