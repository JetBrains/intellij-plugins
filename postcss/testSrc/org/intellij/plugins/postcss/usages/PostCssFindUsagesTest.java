package org.intellij.plugins.postcss.usages;

import org.intellij.plugins.postcss.PostCssFixtureTestCase;

public class PostCssFindUsagesTest extends PostCssFixtureTestCase {

  public void testCustomSelectors() {
    doTest(2);
  }

  public void testCustomSelectorsDifferentCases() {
    doTest(1);
  }

  public void testCustomMedia() {
    doTest(2);
  }

  public void testCustomMediaDifferentCases() {
    doTest(1);
  }

  private void doTest(int count) {
    assertEquals(count, myFixture.testFindUsages(getTestName(true) + ".pcss").size());
  }
}