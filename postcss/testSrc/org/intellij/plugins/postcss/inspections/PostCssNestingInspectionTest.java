package org.intellij.plugins.postcss.inspections;

import com.intellij.psi.css.inspections.invalid.CssInvalidHtmlTagReferenceInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/inspections/nesting/")
public class PostCssNestingInspectionTest extends PostCssFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssNestingInspection.class);
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection.class);
    myFixture.enableInspections(CssInvalidHtmlTagReferenceInspection.class);
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

  public void testCustomSelectorWithNest() {
    doTest();
  }

  public void testAmpersandInClass() {
    doTest();
  }

  public void testAmpersandInId() {
    doTest();
  }

  public void testAmpersandInSimpleSelector() {
    doTest();
  }

  public void testAmpersandInPseudoClass() {
    doTest();
  }

  public void testAmpersandInPseudoFunction() {
    doTest();
  }

  public void testAmpersandInAttributes() {
    doTest();
  }

  public void testAmpersandWithOperators() {
    doTest();
  }

  public void testAmpersandInSelectorListMulti() {
    doTest();
  }

  public void testPseudoClassStartsWithAmpersand() {
    doTest();
  }

  public void testSimpleSelectorWithoutNestedRuleset() {
    doTest();
  }

  public void testSimpleSelectorWithNestedRuleset() {
    doTest();
  }

  public void testSimpleSelectorWithNestedRulesetInsideMedia() {
    doTest();
  }

  private void doTest() {
    myFixture.testHighlighting(true, false, false, getTestName(true) + ".pcss");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "nesting";
  }
}