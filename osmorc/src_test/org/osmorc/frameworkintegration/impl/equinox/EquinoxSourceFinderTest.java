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
package org.osmorc.frameworkintegration.impl.equinox;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class EquinoxSourceFinderTest {
    public EquinoxSourceFinderTest() {
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
                IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder();
        fixture = fixtureBuilder.getFixture();

    }

    @Before
    public void setUp() throws Exception {
        fixture.setUp();
        root = ModuleRootManager.getInstance(fixture.getModule()).getContentRoots()[0];
        testObject = new EquinoxSourceFinder();
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    testBundle = root.createChildData(this, "somebundle_1.0.0.jar");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        fixture.tearDown();
    }


    @Test
    public void testGetSourceForLibraryClassesNoSourceFolder() {
        List<VirtualFile> sources = testObject.getSourceForLibraryClasses(testBundle);
        assertThat(sources, notNullValue());
        assertThat(sources.size(), equalTo(0));
    }

    @Test
    public void testGetSourceForLibraryClassesSourceFolderWithoutBundleSource() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    root.createChildDirectory(this, "source").createChildDirectory(this, "src");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        List<VirtualFile> sources = testObject.getSourceForLibraryClasses(testBundle);
        assertThat(sources, notNullValue());
        assertThat(sources.size(), equalTo(0));
    }

    @Test
    public void testGetSourceForLibraryClassesSourceWithBundleSource() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    root.createChildDirectory(this, "some_source").createChildDirectory(this, "src")
                            .createChildDirectory(this, "somebundle_1.0.0").createChildData(this, "src.zip");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        List<VirtualFile> sources = testObject.getSourceForLibraryClasses(testBundle);
        assertThat(sources, notNullValue());
        assertThat(sources.size(), equalTo(1));
        assertThat(sources.get(0), sameInstance(root.findFileByRelativePath("some_source/src/somebundle_1.0.0/src.zip")));
    }

    private IdeaProjectTestFixture fixture;
    private VirtualFile root;
    private EquinoxSourceFinder testObject;
    private VirtualFile testBundle;
}
