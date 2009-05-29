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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.fix.impl.ReplaceUtil;
import org.osmorc.manifest.lang.psi.ManifestClause;
import org.osmorc.manifest.lang.psi.ManifestHeader;
import org.osmorc.manifest.lang.psi.ManifestHeaderValueImpl;

import java.util.Arrays;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ModuleDependencySynchronizerTest {

    public ModuleDependencySynchronizerTest() throws Exception {
        fixture = TestUtil.createTestFixture();
    }

    @Before
    public void setUp() throws Exception {
        myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
        myTempDirFixture.setUp();
        fixture.setUp();
        TestUtil.loadModules("ModuleDependencySynchronizerTest", fixture.getProject(), myTempDirFixture.getTempDirPath());
        TestUtil.createOsmorFacetForAllModules(fixture.getProject());
    }

    @After
    public void tearDown() throws Exception {
        fixture.tearDown();
        myTempDirFixture.tearDown();
    }

    @Test
    public void testDependencySynchronisation() {
        ModuleManager moduleManager = ModuleManager.getInstance(fixture.getProject());


        Module t0 = moduleManager.findModuleByName("t0");
        Module t1 = moduleManager.findModuleByName("t1");
        Module t2 = moduleManager.findModuleByName("t2");
        Module t3 = moduleManager.findModuleByName("t3");

        assertThat(ModuleRootManager.getInstance(t0).getDependencies().length, equalTo(0));
        assertThat(ModuleRootManager.getInstance(t1).getDependencies().length, equalTo(0));

        assertThat(ModuleRootManager.getInstance(t2).getDependencies().length, equalTo(1));
        assertThat(Arrays.asList(ModuleRootManager.getInstance(t2).getDependencies()), hasItem(t1));
        assertThat(TestUtil.getOrderEntry(t1, t2).isExported(), equalTo(false));

        assertThat(ModuleRootManager.getInstance(t3).getDependencies().length, equalTo(2));
        assertThat(Arrays.asList(ModuleRootManager.getInstance(t3).getDependencies()), hasItem(t2));
        assertThat(Arrays.asList(ModuleRootManager.getInstance(t3).getDependencies()), hasItem(t0));
        assertThat(TestUtil.getOrderEntry(t0, t3).isExported(), equalTo(true));
    }

    @Test
    public void testForwardDependency() throws Exception {
        ModuleManager moduleManager = ModuleManager.getInstance(fixture.getProject());

        Module t5 = moduleManager.findModuleByName("t5");
        Module t6 = moduleManager.findModuleByName("t6");

        assertThat(ModuleRootManager.getInstance(t6).getDependencies().length, equalTo(0));

        assertThat(ModuleRootManager.getInstance(t5).getDependencies().length, equalTo(1));
        assertThat(Arrays.asList(ModuleRootManager.getInstance(t5).getDependencies()), hasItem(t6));
        assertThat(TestUtil.getOrderEntry(t6, t5).isExported(), equalTo(false));
    }

    @Test
    public void testExportChange() throws Exception {
        ModuleManager moduleManager = ModuleManager.getInstance(fixture.getProject());

        final Module t7 = moduleManager.findModuleByName("t7");
        final Module t8 = moduleManager.findModuleByName("t8");

        assertThat(ModuleRootManager.getInstance(t7).getDependencies().length, equalTo(0));
        assertThat(ModuleRootManager.getInstance(t8).getDependencies().length, equalTo(0));

        replaceExportPackage(t8, "t8");

        Thread.sleep(100);
        assertThat(ModuleRootManager.getInstance(t7).getDependencies().length, equalTo(1));
        assertThat(Arrays.asList(ModuleRootManager.getInstance(t7).getDependencies()), hasItem(t8));
        assertThat(TestUtil.getOrderEntry(t8, t7).isExported(), equalTo(false));

        replaceExportPackage(t8, "t8.sub");

        Thread.sleep(100);
        assertThat(ModuleRootManager.getInstance(t7).getDependencies().length, equalTo(0));
    }

    private void replaceExportPackage(Module module, final String replacement) {
        VirtualFile contentRoot = ModuleRootManager.getInstance(module).getContentRoots()[0];
        VirtualFile manifestFile = contentRoot.findFileByRelativePath("META-INF/MANIFEST.MF");

        PsiFile manifestPsiFile = PsiManager.getInstance(fixture.getProject()).findFile(manifestFile);
        ManifestHeader lastHeader = null;

        for (PsiElement psiElement : manifestPsiFile.getChildren()) {
            if (psiElement instanceof ManifestHeader) {
                lastHeader = (ManifestHeader) psiElement;
            }
        }

        ManifestClause clause = (ManifestClause) lastHeader.getLastChild();
        final ManifestHeaderValueImpl value = (ManifestHeaderValueImpl) clause.getLastChild();

        CommandProcessor.getInstance().executeCommand(fixture.getProject(), new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        ReplaceUtil.replace(value, replacement);
                    }
                });
            }
        }, "test", "testid");
    }

    private IdeaProjectTestFixture fixture;
    private TempDirTestFixture myTempDirFixture;
}
