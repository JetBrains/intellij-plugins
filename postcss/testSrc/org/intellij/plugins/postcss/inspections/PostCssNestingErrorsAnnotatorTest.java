package org.intellij.plugins.postcss.inspections;

import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/inspections/annotator/")
public class PostCssNestingErrorsAnnotatorTest extends PostCssFixtureTestCase {
  public void testNormalRulesetWithNestingSelectors() {
    doTest();
  }

  public void testNestedRulesetWithoutAmpersand() {
    doTest();
  }

  public void testNestedRulesetWithNestWithoutAmpersand() {
    doTest();
  }
  
  public void testNestedRulesetWithoutNest() {
    doTest();
  }
  
  public void testCorrectNesting() {
    doTest();
  }

  private long doTest() {
    return myFixture.testHighlighting(true, false, false, getTestName(true) + ".pcss");
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }
  
  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "annotator";
  }
}