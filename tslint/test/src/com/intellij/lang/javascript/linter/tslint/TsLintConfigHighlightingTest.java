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

  public void testDisabled() throws Exception {
    doTest();
  }

  private long doTest() {
    return myFixture.testHighlighting(getTestName(true) + "/tslint.json");
  }

  public void testAlignParameters() throws Exception {
    doTest();
  }

  public void testOneLine() throws Exception {
    doTest();
  }

  public void testTypedefWhitespace() throws Exception {
    doTest();
  }

  public void testWhitespace() throws Exception {
    doTest();
  }

  public void testWrong() throws Exception {
    doTest();
  }

  public void testAlignWrongParameters() throws Exception {
    doTest();
  }

  public void testCompletedDocs() throws Exception {
    doTest();
  }

  public void testPreferSwitchPreferTemplate() throws Exception {
    doTest();
  }
}
