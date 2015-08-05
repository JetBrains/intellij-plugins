package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartServerCompletionTest extends CodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    final DartSdk sdk = DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    DartAnalysisServerService.getInstance().serverReadyForRequest(getProject(), sdk);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  protected String getBasePath() {
    return "/analysisServer/completion";
  }

  private void doTest() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.complete(CompletionType.BASIC);
    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  public void testFunctionWithArgsInvocation() throws Throwable {
    doTest();
  }

  public void testFunctionNoArgsInvocation() throws Throwable {
    doTest();
  }

  public void testFunctionAfterShow() throws Throwable {
    doTest();
  }

  public void testFunctionAsArgument() throws Throwable {
    doTest();
  }
}
