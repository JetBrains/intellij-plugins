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

package org.osmorc.inspection;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.QuickFix;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.TestUtil;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.facet.OsmorcFacet;

import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class UnregisteredActivatorInspectionTest {
    public UnregisteredActivatorInspectionTest() {
        fixture = TestUtil.createTestFixture();
    }

    @Before
    public void setUp() throws Exception {
        myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
        myTempDirFixture.setUp();
        fixture.setUp();
        TestUtil.loadModules("UnregisteredActivatorInspectionTest", fixture.getProject(), myTempDirFixture.getTempDirPath());
        TestUtil.createOsmorcFacetForAllModules(fixture.getProject());
    }

    @After
    public void tearDown() throws Exception {
        fixture.tearDown();
        myTempDirFixture.tearDown();
    }

    @Test
    public void testInspectionWithErrorForManuallyEditedManifest() {
        PsiFile psiFile = TestUtil.loadPsiFile(fixture.getProject(), "t0", "t0/Activator.java");

        List<ProblemDescriptor> list = TestUtil.runInspection(new UnregisteredActivatorInspection(), psiFile, fixture.getProject());

        assertThat(list, notNullValue());
        assertThat(list.size(), equalTo(1));

        final ProblemDescriptor problem = list.get(0);
        assertThat(problem.getLineNumber(), equalTo(6));

        final QuickFix[] fixes = problem.getFixes();
        assertThat(fixes, notNullValue());
        assertThat(fixes.length, equalTo(1));

        CommandProcessor.getInstance().executeCommand(fixture.getProject(), new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        //noinspection unchecked
                        fixes[0].applyFix(fixture.getProject(), problem);
                    }
                });
            }
        }, "test", "test");

        list = TestUtil.runInspection(new UnregisteredActivatorInspection(), psiFile, fixture.getProject());
        assertThat(list, nullValue());
    }

    @Test
    public void testInspectionWithoutErrorForManuallyEditedManifest() {
        PsiFile psiFile = TestUtil.loadPsiFile(fixture.getProject(), "t1", "t1/Activator.java");

        List<ProblemDescriptor> list = TestUtil.runInspection(new UnregisteredActivatorInspection(), psiFile, fixture.getProject());

        assertThat(list, nullValue());
    }

    @Test
    public void testInspectionWithErrorForGeneratedManifest() {
        PsiFile psiFile = TestUtil.loadPsiFile(fixture.getProject(), "t0", "t0/Activator.java");
        final OsmorcFacetConfiguration configuration = OsmorcFacet.getInstance(psiFile).getConfiguration();
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                configuration.setOsmorcControlsManifest(true);
                configuration.setBundleActivator("");
            }
        });

        List<ProblemDescriptor> list = TestUtil.runInspection(new UnregisteredActivatorInspection(), psiFile, fixture.getProject());

        assertThat(list, notNullValue());
        assertThat(list.size(), equalTo(1));

        final ProblemDescriptor problem = list.get(0);
        assertThat(problem.getLineNumber(), equalTo(6));

        final QuickFix[] fixes = problem.getFixes();
        assertThat(fixes, notNullValue());
        assertThat(fixes.length, equalTo(1));

        CommandProcessor.getInstance().executeCommand(fixture.getProject(), new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        //noinspection unchecked
                        fixes[0].applyFix(fixture.getProject(), problem);
                    }
                });
            }
        }, "test", "test");

        list = TestUtil.runInspection(new UnregisteredActivatorInspection(), psiFile, fixture.getProject());
        assertThat(list, nullValue());
    }

    @Test
    public void testInspectionWithoutErrorForGeneratedManifest() {
        PsiFile psiFile = TestUtil.loadPsiFile(fixture.getProject(), "t1", "t1/Activator.java");
        final OsmorcFacetConfiguration configuration = OsmorcFacet.getInstance(psiFile).getConfiguration();
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                configuration.setOsmorcControlsManifest(true);
                configuration.setBundleActivator("t1.Activator");
            }
        });


        List<ProblemDescriptor> list = TestUtil.runInspection(new UnregisteredActivatorInspection(), psiFile, fixture.getProject());

        assertThat(list, nullValue());
    }


    private final IdeaProjectTestFixture fixture;
    private TempDirTestFixture myTempDirFixture;
}
