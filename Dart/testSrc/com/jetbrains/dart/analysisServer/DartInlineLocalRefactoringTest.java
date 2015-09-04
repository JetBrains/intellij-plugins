/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.dart.analysisServer;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.ide.refactoring.ServerInlineLocalRefactoring;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;

public class DartInlineLocalRefactoringTest extends CodeInsightFixtureTestCase {
  protected String getBasePath() {
    return "/analysisServer/refactoring/inline/local";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  public void testTest() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart");
  }

  @NotNull
  private ServerInlineLocalRefactoring createInlineLocalRefactoring(String filePath) {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    final PsiFile psiFile = myFixture.configureByFile(filePath);
    myFixture.doHighlighting(); // make sure server is warmed up
    // find the Element to rename
    final int offset = getEditor().getCaretModel().getOffset();
    return new ServerInlineLocalRefactoring(getSystemPath(psiFile), offset, 0);
  }

  private void doTest(String filePath) {
    final ServerInlineLocalRefactoring refactoring = createInlineLocalRefactoring(filePath);
    // check initial conditions
    final RefactoringStatus initialConditions = refactoring.checkInitialConditions();
    assertNotNull(initialConditions);
    assertTrue(initialConditions.isOK());
    // check final conditions
    final RefactoringStatus finalConditions = refactoring.checkFinalConditions();
    assertNotNull(finalConditions);
    assertTrue(finalConditions.isOK());
    // apply the SourceChange
    final SourceChange change = refactoring.getChange();
    assertNotNull(change);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        AssistUtils.applySourceChange(myFixture.getProject(), change);
      }
    });
    // validate
    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  @NotNull
  private static String getSystemPath(PsiFile psiFile) {
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    final String path = virtualFile.getPath();
    return FileUtil.toSystemDependentName(path);
  }
}
