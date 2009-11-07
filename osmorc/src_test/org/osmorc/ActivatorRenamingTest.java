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

package org.osmorc;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.MoveDestination;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ActivatorRenamingTest {
    public ActivatorRenamingTest() {
        fixture = TestUtil.createTestFixture();
    }

    @Before
    public void setUp() throws Exception {
        myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
        myTempDirFixture.setUp();
        fixture.setUp();
        TestUtil.loadModules("ActivatorRenamingTest", fixture.getProject(), myTempDirFixture.getTempDirPath());
        TestUtil.createOsmorcFacetForAllModules(fixture.getProject());
    }

    @After
    public void tearDown() throws Exception {
        fixture.tearDown();
        myTempDirFixture.tearDown();
    }

    //@Test
    public void testRenameForManuallyEditedManifest() {
        PsiFile activatorClassFile = TestUtil.loadPsiFile(fixture.getProject(), "t1", "t1/Activator.java");
        PsiFile manifest = TestUtil.loadPsiFileUnderContent(fixture.getProject(), "t1", "META-INF/MANIFEST.MF");

        final PsiClass activatorClass = PsiTreeUtil.getChildOfType(activatorClassFile, PsiClass.class);

        assertThat(manifest.getText(), endsWith("Bundle-Activator: t1.Activator\n"));

        CommandProcessor.getInstance().executeCommand(fixture.getProject(), new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        RefactoringFactory.getInstance(fixture.getProject()).createRename(activatorClass, "RenamedActivator").run();
                    }
                });
            }
        }, "test", "testid");

        assertThat(manifest.getText(), endsWith("Bundle-Activator: t1.RenamedActivator\n"));
    }


    //@Test
    public void testMoveForManuallyEditedManifest() {
        PsiFile activatorClassFile = TestUtil.loadPsiFile(fixture.getProject(), "t1", "t1/Activator.java");
        PsiFile manifest = TestUtil.loadPsiFileUnderContent(fixture.getProject(), "t1", "META-INF/MANIFEST.MF");

        final PsiClass activatorClass = PsiTreeUtil.getChildOfType(activatorClassFile, PsiClass.class);

        assertThat(manifest.getText(), endsWith("Bundle-Activator: t1.Activator\n"));

        CommandProcessor.getInstance().executeCommand(fixture.getProject(), new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        final ModuleRootManager rootManager = TestUtil.getModuleRootManager(fixture.getProject(), "t1");
                        final VirtualFile root = rootManager.getSourceRoots()[0];
                        MoveDestination moveDestination = JavaRefactoringFactory.getInstance(fixture.getProject()).createSourceRootMoveDestination("tx", root);
                        JavaRefactoringFactory.getInstance(fixture.getProject()).createMoveClassesOrPackages(new PsiElement[]{activatorClass}, moveDestination).run();
                    }
                });
            }
        }, "test", "testid");

        assertThat(manifest.getText(), endsWith("Bundle-Activator: tx.Activator\n"));
    }

    // @Test
    public void testRenameForGeneratedManifest() {
        PsiFile activatorClassFile = TestUtil.loadPsiFile(fixture.getProject(), "t1", "t1/Activator.java");
        final OsmorcFacetConfiguration configuration = OsmorcFacet.getInstance(activatorClassFile).getConfiguration();
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                configuration.setOsmorcControlsManifest(true);
                configuration.setBundleActivator("t1.Activator");
            }
        });

        final PsiClass activatorClass = PsiTreeUtil.getChildOfType(activatorClassFile, PsiClass.class);

        CommandProcessor.getInstance().executeCommand(fixture.getProject(), new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        RefactoringFactory.getInstance(fixture.getProject()).createRename(activatorClass, "RenamedActivator").run();
                    }
                });
            }
        }, "test", "testid");

        assertThat(configuration.getBundleActivator(), equalTo("t1.RenamedActivator"));
    }


    // @Test
    public void testMoveForGeneratedManifest() {
        PsiFile activatorClassFile = TestUtil.loadPsiFile(fixture.getProject(), "t1", "t1/Activator.java");
        final OsmorcFacetConfiguration configuration = OsmorcFacet.getInstance(activatorClassFile).getConfiguration();
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                configuration.setOsmorcControlsManifest(true);
                configuration.setBundleActivator("t1.Activator");
            }
        });

        final PsiClass activatorClass = PsiTreeUtil.getChildOfType(activatorClassFile, PsiClass.class);

        CommandProcessor.getInstance().executeCommand(fixture.getProject(), new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        final ModuleRootManager rootManager = TestUtil.getModuleRootManager(fixture.getProject(), "t1");
                        final VirtualFile root = rootManager.getSourceRoots()[0];
                        MoveDestination moveDestination = JavaRefactoringFactory.getInstance(fixture.getProject()).createSourceRootMoveDestination("tx", root);
                        JavaRefactoringFactory.getInstance(fixture.getProject()).createMoveClassesOrPackages(new PsiElement[]{activatorClass}, moveDestination).run();
                    }
                });
            }
        }, "test", "testid");

        assertThat(configuration.getBundleActivator(), equalTo("tx.Activator"));
    }


    private final IdeaProjectTestFixture fixture;
    private TempDirTestFixture myTempDirFixture;
}
