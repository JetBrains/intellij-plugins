package com.jetbrains.dart.analysisServer;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import com.jetbrains.lang.dart.ide.refactoring.ServerExtractLocalVariableRefactoring;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DartExtractLocalVariableRefactoringTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  @Override
  protected String getBasePath() {
    return "/analysisServer/refactoring/extract/localVariable";
  }

  public void testExpressionAll() {
    final String testName = getTestName(false);
    doTest(testName + ".dart", true);
  }

  public void testExpressionSingle() {
    final String testName = getTestName(false);
    doTest(testName + ".dart", false);
  }

  @NotNull
  private ServerExtractLocalVariableRefactoring createRefactoring(String filePath) {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    final PsiFile psiFile = myFixture.configureByFile(filePath);

    ApplicationManager.getApplication().runWriteAction(() -> {
      try {
        VfsUtil.saveText(psiFile.getVirtualFile(), StringUtil.convertLineSeparators(psiFile.getText(), "\r\n"));
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    myFixture.doHighlighting(); // make sure server is warmed up
    // find the Element to rename
    final SelectionModel selectionModel = getEditor().getSelectionModel();
    int offset = selectionModel.getSelectionStart();
    final int length = selectionModel.getSelectionEnd() - offset;
    return new ServerExtractLocalVariableRefactoring(getProject(), psiFile.getVirtualFile(), offset, length);
  }

  private void doTest(String filePath, boolean all) {
    final ServerExtractLocalVariableRefactoring refactoring = createRefactoring(filePath);
    // check initial conditions
    final RefactoringStatus initialConditions = refactoring.checkInitialConditions();
    assertNotNull(initialConditions);
    assertTrue(initialConditions.isOK());
    // configure
    //refactoring.setName("test");
    refactoring.setExtractAll(all);
    // check final conditions
    final RefactoringStatus finalConditions = refactoring.checkFinalConditions();
    assertNotNull(finalConditions);
    assertTrue(finalConditions.isOK());
    // apply the SourceChange
    final SourceChange change = refactoring.getChange();
    assertNotNull(change);
    ApplicationManager.getApplication().runWriteAction(() -> {
      try {
        AssistUtils.applySourceChange(myFixture.getProject(), change, true);
      }
      catch (DartSourceEditException e) {
        fail(e.getMessage());
      }
    });
    // validate
    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }
}
