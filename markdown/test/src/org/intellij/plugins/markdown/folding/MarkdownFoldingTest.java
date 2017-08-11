package org.intellij.plugins.markdown.folding;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.intellij.plugins.markdown.MarkdownTestingUtil;

public class MarkdownFoldingTest extends LightPlatformCodeInsightFixtureTestCase {
  public void testOrderedList() {
    doTest();
  }

  public void testUnorderedList() {
    doTest();
  }

  public void testComplexOrderedList() {
    doTest();
  }

  public void testComplexUnorderedList() {
    doTest();
  }

  public void testSingleLineOrderedList() {
    doTest();
  }

  public void testSingleLineUnorderedList() {
    doTest();
  }

  public void testUnorderedSublist() {
    doTest();
  }

  public void testTable() {
    doTest();
  }

  public void testBlockQuote() {
    doTest();
  }

  private void doTest() {
    myFixture.testFolding(getTestDataPath() + "/" + getTestName(true) + ".md");
  }

  @Override
  protected String getTestDataPath() {
    return MarkdownTestingUtil.TEST_DATA_PATH + "/folding";
  }
}