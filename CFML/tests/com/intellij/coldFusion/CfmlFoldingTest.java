package com.intellij.coldFusion;

import com.intellij.codeInsight.folding.CodeFoldingSettings;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

/**
 * @author vnikolaenko
 * @date 14.02.11
 */
public class CfmlFoldingTest extends CfmlCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/folding";
  }

  private void doTest() {
    myFixture.testFolding(getTestDataPath() + "/" + getTestName(false) + ".test.cfml");
  }

  public void testComments() throws Throwable {
    doTest();
  }

  public void testMethods() throws Throwable {
    boolean oldValue = CodeFoldingSettings.getInstance().COLLAPSE_METHODS;
    CodeFoldingSettings.getInstance().COLLAPSE_METHODS = true;
    myFixture.testFoldingWithCollapseStatus(getTestDataPath() + "/" + getTestName(false) + ".test.cfml");
    CodeFoldingSettings.getInstance().COLLAPSE_METHODS = oldValue;
  }

}
