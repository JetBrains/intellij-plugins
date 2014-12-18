package com.jetbrains.lang.dart.analyzer.analysisServer;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;

public abstract class DartAnalysisServerHighlightingTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  protected String getBasePath() {
    return "/analysisServer/highlighting";
  }

  private void doHighlightingTest() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting();
  }

  public void testErrorsHighlighting() throws Exception {
    doHighlightingTest();
  }
}
