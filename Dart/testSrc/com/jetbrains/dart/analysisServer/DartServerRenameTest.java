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

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.ide.refactoring.DartServerRenameHandler;
import com.jetbrains.lang.dart.ide.refactoring.ServerRenameRefactoring;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DartServerRenameTest extends CodeInsightFixtureTestCase {

  private static class DataContextForTest implements DataContext {
    private final Editor myEditor;
    private final VirtualFile myVirtualFile;
    private final PsiFile myPsiFile;
    private final PsiElement myPsiElement;

    public DataContextForTest(Editor editor, VirtualFile virtualFile, PsiFile psiFile, PsiElement psiElement) {
      myEditor = editor;
      myVirtualFile = virtualFile;
      myPsiFile = psiFile;
      myPsiElement = psiElement;
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
      if (dataId.equals(CommonDataKeys.EDITOR.getName())) return myEditor;
      if (dataId.equals(CommonDataKeys.VIRTUAL_FILE.getName())) return myVirtualFile;
      if (dataId.equals(CommonDataKeys.PSI_FILE.getName())) return myPsiFile;
      if (dataId.equals(CommonDataKeys.PSI_ELEMENT.getName())) return myPsiElement;
      return null;
    }
  }

  protected String getBasePath() {
    return "/analysisServer/refactoring/rename";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  public void _testAvailability() {
    final XmlFile htmlPsiFile = (XmlFile)myFixture.configureByText("foo.html", "<script type='application/dart'>\n" +
                                                                               "  var <caret>foo;\n" +
                                                                               "</script>");
    final VirtualFile htmlVirtualFile = htmlPsiFile.getVirtualFile();
    final XmlTag htmlTag = htmlPsiFile.getRootTag();
    final PsiElement dartElementInHtmlFile = htmlPsiFile.findElementAt(getEditor().getCaretModel().getOffset());

    final PsiFile dartPsiFile = myFixture.addFileToProject("bar.dart", "// comment");
    final VirtualFile dartVirtualFile = dartPsiFile.getVirtualFile();
    final PsiElement dartElement = dartPsiFile.findElementAt(0);

    final DartServerRenameHandler handler = new DartServerRenameHandler();

    assertFalse("no editor", handler.isRenaming(new DataContextForTest(null, htmlVirtualFile, htmlPsiFile, null)));
    assertFalse("html element at caret", handler.isRenaming(new DataContextForTest(getEditor(), htmlVirtualFile, htmlPsiFile, htmlTag)));
    assertTrue("dart element in html file at caret",
               handler.isRenaming(new DataContextForTest(getEditor(), htmlVirtualFile, htmlPsiFile, null)));
    assertTrue("dart element in html file",
               handler.isRenaming(new DataContextForTest(getEditor(), htmlVirtualFile, htmlPsiFile, dartElementInHtmlFile)));

    myFixture.openFileInEditor(dartVirtualFile);
    assertTrue("dart comment at caret", handler.isRenaming(new DataContextForTest(getEditor(), dartVirtualFile, dartPsiFile, null)));
    assertTrue("dart comment at caret", handler.isRenaming(new DataContextForTest(getEditor(), dartVirtualFile, dartPsiFile, dartElement)));
  }

  public void testCheckFinalConditionsNameFatalError() throws Throwable {
    final String testName = getTestName(false);
    final ServerRenameRefactoring refactoring = createRenameRefactoring(testName + ".dart", "test = 0");
    // initial status OK
    final RefactoringStatus initialConditions = refactoring.checkInitialConditions();
    assertNotNull(initialConditions);
    assertTrue(initialConditions.isOK());
    // final (actually options) status has a fatal error
    refactoring.setNewName("bad name");
    final RefactoringStatus finalConditions = refactoring.checkFinalConditions();
    assertNotNull(finalConditions);
    assertTrue(finalConditions.hasFatalError());
  }

  public void testCheckInitialConditionsCannotCreate() throws Throwable {
    final String testName = getTestName(false);
    final ServerRenameRefactoring refactoring = createRenameRefactoring(testName + ".dart", "345");
    final RefactoringStatus initialConditions = refactoring.checkInitialConditions();
    assertNotNull(initialConditions);
    assertTrue(initialConditions.hasFatalError());
  }

  public void testClass() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", "Test { // in B", "NewName");
  }

  public void testConstructorDefaultToNamed() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", "AAA() {}", "newName");
  }

  public void testIgnorePotential() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", "test() {}", "newName");
  }

  public void testLocalVariable() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", "test = 0", "newName");
  }

  public void testMethod() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", "test() {} // B", "newName");
  }

  @NotNull
  private ServerRenameRefactoring createRenameRefactoring(String filePath, String atString) {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    final PsiFile psiFile = myFixture.configureByFile(filePath);
    myFixture.doHighlighting(); // make sure server is warmed up
    // find the Element to rename
    final Document document = myFixture.getDocument(psiFile);
    final int offset = document.getText().indexOf(atString);
    return new ServerRenameRefactoring(getSystemPath(psiFile), offset, 0);
  }

  private void doTest(final String filePath, String atString, String newName) {
    final ServerRenameRefactoring refactoring = createRenameRefactoring(filePath, atString);
    // check initial conditions
    final RefactoringStatus initialConditions = refactoring.checkInitialConditions();
    assertNotNull(initialConditions);
    assertTrue(initialConditions.isOK());
    // check final conditions
    refactoring.setNewName(newName);
    final RefactoringStatus finalConditions = refactoring.checkFinalConditions();
    assertNotNull(finalConditions);
    assertTrue(finalConditions.isOK());
    // apply the SourceChange
    final SourceChange change = refactoring.getChange();
    assertNotNull(change);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        final Set<String> excludedIds = refactoring.getPotentialEdits();
        AssistUtils.applySourceChange(myFixture.getProject(), change, excludedIds);
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
