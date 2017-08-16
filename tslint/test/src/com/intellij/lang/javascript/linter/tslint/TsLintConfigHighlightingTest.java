package com.intellij.lang.javascript.linter.tslint;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

/**
 * @author Irina.Chernushina on 9/28/2015.
 */
public class TsLintConfigHighlightingTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/config/highlighting/";
  }

  @Override
  protected String getTestDataPath() {
    return TsLintTestUtil.BASE_TEST_DATA_PATH + getBasePath();
  }

  public void testDisabled() {
    doTest();
  }

  private long doTest() {
    return myFixture.testHighlighting(getTestName(true) + "/tslint.json");
  }

  public void testAlignParameters() {
    doTest();
  }

  public void testOneLine() {
    doTest();
  }

  public void testTypedefWhitespace() {
    doTest();
  }

  public void testWhitespace() {
    doTest();
  }

  public void testWrong() {
    doTest();
  }

  public void testAlignWrongParameters() {
    doTest();
  }

  public void testCompletedDocs() {
    doTest();
  }

  public void testPreferSwitchPreferTemplate() {
    doTest();
  }
}
