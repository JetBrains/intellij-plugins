package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

public class DartServerIntentionsTest extends CodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    DartAnalysisServerService.getInstance().serverReadyForRequest(getProject());
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  protected String getBasePath() {
    return "/analysisServer/intentions";
  }

  private void doTest(@NotNull final String intentionName) {
    myFixture.configureByFile(getTestName(false) + ".dart");
    final IntentionAction intention = myFixture.findSingleIntention(intentionName);

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        intention.invoke(getProject(), getEditor(), getFile());
      }
    });

    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  public void testIntroduceVariableNoSelection() throws Throwable {
    doTest("Assign value to new local variable");
  }

  public void testSurroundWithTryCatch() throws Throwable {
    // TODO selection in the 'after' file is incorrect
    doTest("Surround with 'try-catch'");
  }
}
