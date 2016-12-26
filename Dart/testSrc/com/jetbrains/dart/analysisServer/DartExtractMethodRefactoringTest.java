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
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import com.jetbrains.lang.dart.ide.refactoring.ServerExtractMethodRefactoring;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;

public class DartExtractMethodRefactoringTest extends CodeInsightFixtureTestCase {
  protected String getBasePath() {
    return "/analysisServer/refactoring/extract/method";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  public void testFunctionAll() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", true, false);
  }

  public void testMethodAll() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", true, false);
  }

  public void testMethodGetter() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", true, true);
  }

  public void testMethodSingle() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", false, false);
  }

  @NotNull
  private ServerExtractMethodRefactoring createRefactoring(String filePath) {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    final PsiFile psiFile = myFixture.configureByFile(filePath);
    myFixture.doHighlighting(); // make sure server is warmed up
    // find the Element to rename
    final SelectionModel selectionModel = getEditor().getSelectionModel();
    int offset = selectionModel.getSelectionStart();
    final int length = selectionModel.getSelectionEnd() - offset;
    return new ServerExtractMethodRefactoring(getProject(), psiFile.getVirtualFile(), offset, length);
  }

  private void doTest(String filePath, boolean all, boolean asGetter) {
    final ServerExtractMethodRefactoring refactoring = createRefactoring(filePath);
    // check initial conditions
    final RefactoringStatus initialConditions = refactoring.checkInitialConditions();
    assertNotNull(initialConditions);
    assertTrue(initialConditions.isOK());
    // configure
    refactoring.setName("test");
    refactoring.setExtractAll(all);
    refactoring.setCreateGetter(asGetter);
    // check final conditions
    final RefactoringStatus finalConditions = refactoring.checkFinalConditions();
    assertNotNull(finalConditions);
    assertTrue(finalConditions.isOK());
    // apply the SourceChange
    final SourceChange change = refactoring.getChange();
    assertNotNull(change);
    ApplicationManager.getApplication().runWriteAction(() -> {
      try {
        AssistUtils.applySourceChange(myFixture.getProject(), change, false);
      }
      catch (DartSourceEditException e) {
        fail(e.getMessage());
      }
    });
    // validate
    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }
}
