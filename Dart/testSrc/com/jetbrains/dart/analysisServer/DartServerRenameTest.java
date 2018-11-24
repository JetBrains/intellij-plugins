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
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
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
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  private void doTest(@NotNull final String newName) {
    final ServerRenameRefactoring refactoring = createRenameRefactoring();
    doTest(refactoring, newName);
  }

  private void doTest(@NotNull final ServerRenameRefactoring refactoring, @NotNull final String newName) {
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
    ApplicationManager.getApplication().runWriteAction(() -> {
      final Set<String> excludedIds = refactoring.getPotentialEdits();
      try {
        AssistUtils.applySourceChange(myFixture.getProject(), change, false, excludedIds);
      }
      catch (DartSourceEditException e) {
        fail(e.getMessage());
      }
    });
    // validate
    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  @NotNull
  private ServerRenameRefactoring createRenameRefactoring() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.doHighlighting(); // make sure server is warmed up
    final int offset = getEditor().getCaretModel().getOffset();
    return new ServerRenameRefactoring(getFile().getVirtualFile(), offset, 0);
  }

  public void testAvailability() {
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
    final ServerRenameRefactoring refactoring = createRenameRefactoring();
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
    final ServerRenameRefactoring refactoring = createRenameRefactoring();
    final RefactoringStatus initialConditions = refactoring.checkInitialConditions();
    assertNotNull(initialConditions);
    assertTrue(initialConditions.hasFatalError());
  }

  public void testClass() throws Throwable {
    doTest("NewName");
  }

  public void testConstructorDefaultToNamed() throws Throwable {
    doTest("newName");
  }

  public void testIgnorePotential() throws Throwable {
    doTest("newName");
  }

  public void testTypeAndImmediatelyRenameLocalVar() throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.doHighlighting(); // warm up
    myFixture.type('\n');
    final int offset = getEditor().getCaretModel().getOffset();
    final ServerRenameRefactoring refactoring = new ServerRenameRefactoring(getFile().getVirtualFile(), offset, 0);

    doTest(refactoring, "newName");
  }

  public void testMethod() throws Throwable {
    doTest("newName");
  }

  public void testFileRename() {
    final PsiFile barFile = myFixture.addFileToProject("src/bar.dart", "");
    final PsiFile fooFile = myFixture.addFileToProject("foo.dart", "import  r'''src/bar.dart ''' ;");
    final PsiFile bazFile = myFixture.addFileToProject("src/baz.dart", "export  'bar.dart';");
    myFixture.openFileInEditor(barFile.getVirtualFile());
    myFixture.doHighlighting(); // warm up
    myFixture.renameElement(barFile, "renamed.dart");
    myFixture.openFileInEditor(fooFile.getVirtualFile());
    myFixture.checkResult("import r'''src/renamed.dart''';");
    myFixture.openFileInEditor(bazFile.getVirtualFile());
    myFixture.checkResult("export 'renamed.dart';");
  }

  public void testTargetFileMove() {
    final PsiFile fooFile = myFixture.addFileToProject("web/src/foo.dart", "import \"bar.dart\";");
    myFixture.addFileToProject("web/src/bar.dart", "");
    myFixture.openFileInEditor(fooFile.getVirtualFile());
    myFixture.doHighlighting(); // warm up

    myFixture.moveFile("web/src/bar.dart", "web");

    myFixture.openFileInEditor(fooFile.getVirtualFile());
    myFixture.checkResult("import \"../bar.dart\";");
  }
}
