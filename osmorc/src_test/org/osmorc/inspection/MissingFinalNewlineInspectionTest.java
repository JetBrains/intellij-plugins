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
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.TestUtil;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class MissingFinalNewlineInspectionTest {
    private TempDirTestFixture myTempDirFixture;
    private IdeaProjectTestFixture fixture;
    private TestDialog orgTestDialog;

    @Before
    public void setUp() throws Exception {
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
                JavaTestFixtureFactory.createFixtureBuilder();
        final JavaModuleFixtureBuilder moduleBuilder = fixtureBuilder.addModule(JavaModuleFixtureBuilder.class);

        myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
        fixture = fixtureBuilder.getFixture();

        myTempDirFixture.setUp();
        moduleBuilder.addContentRoot(myTempDirFixture.getTempDirPath());
        fixture.setUp();

        orgTestDialog = Messages.setTestDialog(new TestDialog() {
            public int show(String message) {
                return DialogWrapper.CANCEL_EXIT_CODE;
            }
        });

    }

    @After
    public void tearDown() throws Exception {
        Messages.setTestDialog(orgTestDialog);
        fixture.tearDown();
        myTempDirFixture.tearDown();
    }


    @Test
    public void testFindAndFixError() throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    VirtualFile[] files = ModuleRootManager.getInstance(fixture.getModule()).getContentRoots();
                    VirtualFile childDirectory = files[0].createChildDirectory(this, "META-INF");
                    VirtualFile data = childDirectory.createChildData(this, "MANIFEST.MF");
                    VfsUtil.saveText(data,"Manifest-Version: 1.0\n" +
                            "Bundle-Version: 1.0.0");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        PsiFile psiFile = TestUtil.loadPsiFileUnderContent(fixture.getProject(), fixture.getModule().getName(), "META-INF/MANIFEST.MF");
        List<ProblemDescriptor> list = TestUtil.runInspection(new MissingFinalNewlineInspection(), psiFile, fixture.getProject());

        assertThat(list, notNullValue());
        assertThat(list.size(), equalTo(1));

        final ProblemDescriptor problem = list.get(0);
        assertThat(problem.getLineNumber(), equalTo(2));

        final QuickFix[] fixes = problem.getFixes();
        assertThat(fixes, notNullValue());
        assertThat(fixes.length, equalTo(1));

        assertThat(psiFile.getText(), Matchers.endsWith("Bundle-Version: 1.0.0"));

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
        assertThat(psiFile.getText(), Matchers.endsWith("Bundle-Version: 1.0.0\n"));

        list = TestUtil.runInspection(new MissingFinalNewlineInspection(), psiFile, fixture.getProject());
        assertThat(list, nullValue());
    }

    @Test
    public void testNoErrorFound() throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    VirtualFile[] files = ModuleRootManager.getInstance(fixture.getModule()).getContentRoots();
                    VirtualFile childDirectory = files[0].createChildDirectory(this, "META-INF");
                    VirtualFile data = childDirectory.createChildData(this, "MANIFEST.MF");
                    VfsUtil.saveText(data,"Manifest-Version: 1.0\n" +
                            "Bundle-Version: 1.0.0\n");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        PsiFile psiFile = TestUtil.loadPsiFileUnderContent(fixture.getProject(), fixture.getModule().getName(), "META-INF/MANIFEST.MF");
        List<ProblemDescriptor> list = TestUtil.runInspection(new MissingFinalNewlineInspection(), psiFile, fixture.getProject());

        assertThat(list, nullValue());
    }

  /**
   * Intended to test the empty file case, which caused an SIOOBE. http://ea.jetbrains.com/browser/ea_problems/22570
   * @throws Exception
   */
    @Test
    public void testOnEmptyFile() throws Exception {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
                 public void run() {
                     try {
                         VirtualFile[] files = ModuleRootManager.getInstance(fixture.getModule()).getContentRoots();
                         VirtualFile childDirectory = files[0].createChildDirectory(this, "META-INF");
                         VirtualFile data = childDirectory.createChildData(this, "MANIFEST.MF");
                         OutputStream outputStream = data.getOutputStream(this);
                         PrintWriter writer = new PrintWriter(outputStream);
                         writer.flush();
                         writer.close();
                     }
                     catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 }
             });
      PsiFile psiFile = TestUtil.loadPsiFileUnderContent(fixture.getProject(), fixture.getModule().getName(), "META-INF/MANIFEST.MF");
      List<ProblemDescriptor> list = TestUtil.runInspection(new MissingFinalNewlineInspection(), psiFile, fixture.getProject());

      assertThat(list, nullValue());

    }
}