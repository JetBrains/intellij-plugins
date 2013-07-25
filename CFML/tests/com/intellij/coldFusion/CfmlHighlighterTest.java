package com.intellij.coldFusion;

import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 4/23/12
 */
public class CfmlHighlighterTest extends CfmlCodeInsightFixtureTestCase {
  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  protected void doTest() throws Exception {
    myFixture.testHighlighting(true, false, true, Util.getInputDataFileName(getTestName(true)));
  }

  public void testHighlightCfmlMixedWithJavascript() throws Exception {
    doTest();
  }

  @Override
  protected String getBasePath() {
    return "/highlighter";
  }
}
