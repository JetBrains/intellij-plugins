package org.intellij.plugins.postcss.inspections;

import com.intellij.openapi.project.Project;
import com.intellij.psi.css.inspections.invalid.CssInvalidHtmlTagReferenceInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/inspections/nesting")
public class PostCssNestingInspectionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.enableInspections(PostCssNestingInspection.class);
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection.class);
    myFixture.enableInspections(CssInvalidHtmlTagReferenceInspection.class);
  }

  @Test
  public void testNormalRulesetWithNestingSelectors() {
    doTest();
  }

  @Test
  public void testNestedRulesetWithoutAmpersand() {
    doTest();
  }

  @Test
  public void testNestedRulesetWithNestWithoutAmpersand() {
    doTest();
  }

  @Test
  public void testNestedRulesetWithoutNest() {
    doTest();
  }

  @Test
  public void testEmptyNest() {
    doTest();
  }

  @Test
  public void testCorrectNesting() {
    doTest();
  }

  @Test
  public void testRuleInsideMedia() {
    doTest();
  }

  @Test
  public void testRuleInsideDocument() {
    doTest();
  }

  @Test
  public void testRuleInsideSupports() {
    doTest();
  }

  @Test
  public void testNestedRuleInsideMedia() {
    doTest();
  }

  @Test
  public void testNestedRuleInsideDocument() {
    doTest();
  }

  @Test
  public void testNestedRuleInsideSupports() {
    doTest();
  }

  @Test
  public void testNestedRulesetInsideNestAtRule() {
    doTest();
  }

  @Test
  public void testCustomSelectorDefinitionInsideRuleset() {
    doTest();
  }

  @Test
  public void testCustomSelectorDefinitionInsideAtRule() {
    doTest();
  }

  @Test
  public void testCustomSelectorWithAmpersand() {
    doTest();
  }

  @Test
  public void testCustomSelectorWithNest() {
    doTest();
  }

  @Test
  public void testAmpersandInClass() {
    doTest();
  }

  @Test
  public void testAmpersandInId() {
    doTest();
  }

  @Test
  public void testAmpersandInSimpleSelector() {
    doTest();
  }

  @Test
  public void testAmpersandInPseudoClass() {
    doTest();
  }

  @Test
  public void testAmpersandInPseudoFunction() {
    doTest();
  }

  @Test
  public void testAmpersandInAttributes() {
    doTest();
  }

  @Test
  public void testAmpersandWithOperators() {
    doTest();
  }

  @Test
  public void testAmpersandInSelectorListMulti() {
    doTest();
  }

  @Test
  public void testPseudoClassStartsWithAmpersand() {
    doTest();
  }

  @Test
  public void testSimpleSelectorWithoutNestedRuleset() {
    doTest();
  }

  @Test
  public void testSimpleSelectorWithNestedRuleset() {
    doTest();
  }

  @Test
  public void testSimpleSelectorWithNestedRulesetInsideMedia() {
    doTest();
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.testHighlighting(true, false, false, postCss.get().getTestName() + ".pcss");
  }
}