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
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.ide.refactoring.DartRenameHandler;
import com.jetbrains.lang.dart.ide.refactoring.ServerRenameRefactoring;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.dartlang.analysis.server.protocol.SourceChange;

public class DartRenameHandlerTest extends CodeInsightFixtureTestCase {

  protected String getBasePath() {
    return "/analysisServer/refactoring/rename";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  public void testClass() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", "Test { // in B", "NewName");
  }

  public void testLocalVariable() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", "test = 0", "newName");
  }

  public void testMethod() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", "test() {} // B", "newName");
  }

  private void doTest(final String filePath, String atString, String newName) {
    final PsiFile psiFile = myFixture.configureByFile(filePath);
    myFixture.doHighlighting(); // make sure server is warmed up
    // find the Element to rename
    final Document document = myFixture.getDocument(psiFile);
    final int offset = document.getText().indexOf(atString);
    final PsiElement element = PsiTreeUtil.findElementOfClassAtOffset(psiFile, offset, PsiElement.class, true);
    assertNotNull(element);
    // check initial conditions
    final ServerRenameRefactoring refactoring = DartRenameHandler.createServerRenameRefactoring(element);
    final RefactoringStatus initialConditions = refactoring.checkInitialConditions();
    assertTrue(initialConditions.isOK());
    // check final conditions
    refactoring.setNewName(newName);
    final RefactoringStatus finalConditions = refactoring.checkFinalConditions();
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
}
