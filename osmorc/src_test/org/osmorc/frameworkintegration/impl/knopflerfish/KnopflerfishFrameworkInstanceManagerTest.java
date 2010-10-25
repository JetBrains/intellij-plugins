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

package org.osmorc.frameworkintegration.impl.knopflerfish;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmorc.SwingRunner;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.impl.LibraryHandlerImpl;
import org.osmorc.i18n.OsmorcBundle;

import java.io.File;
import java.io.IOException;

import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
@RunWith(SwingRunner.class)
public class KnopflerfishFrameworkInstanceManagerTest {
    public KnopflerfishFrameworkInstanceManagerTest() {
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
                IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder();
        fixture = fixtureBuilder.getFixture();
    }

    @Before
    public void setUp() throws Exception {
        fixture.setUp();
        root = ModuleRootManager.getInstance(fixture.getModule()).getContentRoots()[0];
        fileSystem = createMock(LocalFileSystem.class);
        testObject = new KnopflerfishFrameworkInstanceManager(new LibraryHandlerImpl(), fileSystem,
                ApplicationManager.getApplication());
        instanceDefinition = new FrameworkInstanceDefinition();
        instanceDefinition.setBaseFolder(new File(root.getPath(), "knopflerfish").getAbsolutePath());
        instanceDefinition.setName("test");
    }

    @After
    public void tearDown() throws Exception {
        fixture.tearDown();
    }

    @Test
    public void testCheckValidityFolderDoesNotExist() {
        expect(fileSystem.findFileByPath(instanceDefinition.getBaseFolder())).andReturn(root.findChild("knopflerfish"));
        replay(fileSystem);
        assertThat(testObject.checkValidity(instanceDefinition),
                equalTo(
                        OsmorcBundle.getTranslation("knopflerfish.folder.does.not.exist", instanceDefinition.getBaseFolder())));
        verify(fileSystem);
    }

    @Test
    public void testCheckValidityNoKnopflerfishOrgFolder() throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    root.createChildDirectory(this, "knopflerfish");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        expect(fileSystem.findFileByPath(instanceDefinition.getBaseFolder())).andReturn(root.findChild("knopflerfish"));
        replay(fileSystem);
        assertThat(testObject.checkValidity(instanceDefinition),
                equalTo(OsmorcBundle.getTranslation("knopflerfish.folder.knopflerfish.org.missing",
                        instanceDefinition.getBaseFolder())));
        verify(fileSystem);
    }

    @Test
    public void testCheckValidityNoOSGIFolder() throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    root.createChildDirectory(this, "knopflerfish").createChildDirectory(this, "knopflerfish.org");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        expect(fileSystem.findFileByPath(instanceDefinition.getBaseFolder())).andReturn(root.findChild("knopflerfish"));
        replay(fileSystem);
        assertThat(testObject.checkValidity(instanceDefinition),
                equalTo(OsmorcBundle.getTranslation("knopflerfish.folder.osgi.missing",
                        root.findFileByRelativePath("knopflerfish/knopflerfish.org").getPath())));
        verify(fileSystem);
    }

    @Test
    public void testCheckValidityNoBundleFolder() throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    root.createChildDirectory(this, "knopflerfish").createChildDirectory(this, "knopflerfish.org")
                            .createChildDirectory(this, "osgi");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        expect(fileSystem.findFileByPath(instanceDefinition.getBaseFolder())).andReturn(root.findChild("knopflerfish"));
        replay(fileSystem);
        assertThat(testObject.checkValidity(instanceDefinition),
                equalTo(OsmorcBundle.getTranslation("knopflerfish.folder.jars.missing",
                        root.findFileByRelativePath("knopflerfish/knopflerfish.org/osgi").getPath())));
        verify(fileSystem);
    }

    @Test
    public void testCheckValidityOK() throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    root.createChildDirectory(this, "knopflerfish").createChildDirectory(this, "knopflerfish.org")
                            .createChildDirectory(this, "osgi").createChildDirectory(this, "jars");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        expect(fileSystem.findFileByPath(instanceDefinition.getBaseFolder())).andReturn(root.findChild("knopflerfish"));
        replay(fileSystem);
        assertThat(testObject.checkValidity(instanceDefinition), nullValue());
        verify(fileSystem);
    }

    private final IdeaProjectTestFixture fixture;
    private VirtualFile root;
    private KnopflerfishFrameworkInstanceManager testObject;
    private FrameworkInstanceDefinition instanceDefinition;
    private LocalFileSystem fileSystem;
}
