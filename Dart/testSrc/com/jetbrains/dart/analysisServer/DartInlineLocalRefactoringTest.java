package com.jetbrains.dart.analysisServer;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import com.jetbrains.lang.dart.ide.refactoring.ServerInlineLocalRefactoring;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;

public class DartInlineLocalRefactoringTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  @Override
  protected String getBasePath() {
    return "/analysisServer/refactoring/inline/local";
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
    return new ServerInlineLocalRefactoring(getProject(), psiFile.getVirtualFile(), offset, 0);
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
