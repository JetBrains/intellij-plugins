package org.intellij.plugins.postcss.inspections;

import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/inspections/nesting/")
public class PostCssNestingInspectionTest extends PostCssFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssNestingInspection.class);
  }

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

  public void testEmptyNest() {
    doTest();
  }

  public void testCorrectNesting() {
    doTest();
  }

  public void testRuleInsideMedia() {
    doTest();
  }

  public void testRuleInsideDocument() {
    doTest();
  }

  public void testRuleInsideSupports() {
    doTest();
  }

  public void testNestedRuleInsideMedia() {
    doTest();
  }

  public void testNestedRuleInsideDocument() {
    doTest();
  }

  public void testNestedRuleInsideSupports() {
    doTest();
  }

  public void testNestedRulesetInsideNestAtRule() {
    doTest();
  }

  public void testCustomSelectorDefinitionInsideRuleset() {
    doTest();
  }

  public void testCustomSelectorDefinitionInsideAtRule() {
    doTest();
  }

  public void testCustomSelectorWithAmpersand() {
    doTest();
  }

  private long doTest() {
    return myFixture.testHighlighting(true, false, false, getTestName(true) + ".pcss");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "nesting";
  }
}