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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootManager;
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

import java.util.Arrays;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class InvalidImportInspectionTest {
    public InvalidImportInspectionTest() {
        fixture = TestUtil.createTestFixture();
    }

    @Before
    public void setUp() throws Exception {
        myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
        myTempDirFixture.setUp();
        fixture.setUp();
        TestUtil.loadModules("InvalidImportInspectionTest", fixture.getProject(), myTempDirFixture.getTempDirPath());
        TestUtil.createOsmorcFacetForAllModules(fixture.getProject());

        ModuleManager moduleManager = ModuleManager.getInstance(fixture.getProject());

        t0 = moduleManager.findModuleByName("t0");
        t1 = moduleManager.findModuleByName("t1");
        t2 = moduleManager.findModuleByName("t2");
        t3 = moduleManager.findModuleByName("t3");
        t4 = moduleManager.findModuleByName("t4");
    }

    @After
    public void tearDown() throws Exception {
        fixture.tearDown();
        myTempDirFixture.tearDown();
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testImportPackageImport() {
        assertThat(ModuleRootManager.getInstance(t1).getDependencies().length, equalTo(1));
        assertThat(Arrays.asList(ModuleRootManager.getInstance(t1).getDependencies()), hasItem(t0));
        assertThat(TestUtil.getOrderEntry(t0, t1).isExported(), equalTo(false));

        PsiFile psiFile = TestUtil.loadPsiFile(fixture.getProject(), "t1", "t1/Importer.java");

        List<ProblemDescriptor> list = TestUtil.runInspection(new InvalidImportInspection(), psiFile, fixture.getProject());

        assertThat(list, notNullValue());
        assertThat(list.size(), equalTo(6));

        ProblemDescriptor problem = list.get(0);
        assertThat(problem.getLineNumber(), equalTo(4));

        problem = list.get(1);
        assertThat(problem.getLineNumber(), equalTo(5));

        problem = list.get(2);
        assertThat(problem.getLineNumber(), equalTo(12));

        problem = list.get(3);
        assertThat(problem.getLineNumber(), equalTo(12));

        problem = list.get(4);
        assertThat(problem.getLineNumber(), equalTo(13));

        problem = list.get(5);
        assertThat(problem.getLineNumber(), equalTo(13));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testDirectRequireBundleImport() {
        assertThat(ModuleRootManager.getInstance(t2).getDependencies().length, equalTo(1));
        assertThat(Arrays.asList(ModuleRootManager.getInstance(t2).getDependencies()), hasItem(t0));
        assertThat(TestUtil.getOrderEntry(t0, t2).isExported(), equalTo(true));

        PsiFile psiFile = TestUtil.loadPsiFile(fixture.getProject(), "t2", "t2/Importer.java");

        List<ProblemDescriptor> list = TestUtil.runInspection(new InvalidImportInspection(), psiFile, fixture.getProject());

        assertThat(list, notNullValue());
        assertThat(list.size(), equalTo(3));

        ProblemDescriptor problem = list.get(0);
        assertThat(problem.getLineNumber(), equalTo(5));

        problem = list.get(1);
        assertThat(problem.getLineNumber(), equalTo(13));

        problem = list.get(2);
        assertThat(problem.getLineNumber(), equalTo(13));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testRecursiveRequireBundleWithReexportImport() {
        assertThat(ModuleRootManager.getInstance(t3).getDependencies().length, equalTo(1));
        assertThat(Arrays.asList(ModuleRootManager.getInstance(t3).getDependencies()), hasItem(t2));
        assertThat(TestUtil.getOrderEntry(t2, t3).isExported(), equalTo(false));

        PsiFile psiFile = TestUtil.loadPsiFile(fixture.getProject(), "t3", "t3/Importer.java");

        List<ProblemDescriptor> list = TestUtil.runInspection(new InvalidImportInspection(), psiFile, fixture.getProject());

        assertThat(list, notNullValue());
        assertThat(list.size(), equalTo(3));

        ProblemDescriptor problem = list.get(0);
        assertThat(problem.getLineNumber(), equalTo(5));

        problem = list.get(1);
        assertThat(problem.getLineNumber(), equalTo(13));

        problem = list.get(2);
        assertThat(problem.getLineNumber(), equalTo(13));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testRecursiveRequireBundleWithoutReexportImport() {
        assertThat(ModuleRootManager.getInstance(t4).getDependencies().length, equalTo(1));
        assertThat(Arrays.asList(ModuleRootManager.getInstance(t4).getDependencies()), hasItem(t3));
        assertThat(TestUtil.getOrderEntry(t3, t4).isExported(), equalTo(false));

        PsiFile psiFile = TestUtil.loadPsiFile(fixture.getProject(), "t4", "t4/Importer.java");

        List<ProblemDescriptor> list = TestUtil.runInspection(new InvalidImportInspection(), psiFile, fixture.getProject());

        assertThat(list, nullValue());
    }

    private IdeaProjectTestFixture fixture;
    private TempDirTestFixture myTempDirFixture;
    private Module t0;
    private Module t1;
    private Module t2;
    private Module t3;
    private Module t4;
}
