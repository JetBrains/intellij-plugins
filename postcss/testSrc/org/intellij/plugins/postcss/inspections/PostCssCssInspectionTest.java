package org.intellij.plugins.postcss.inspections;

import com.intellij.psi.css.inspections.invalid.*;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;

@TestDataPath("$CONTENT_ROOT/testData/inspections/")
public class PostCssCssInspectionTest extends PostCssFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(CssInvalidImportInspection.class,
                                CssInvalidPseudoSelectorInspection.class,
                                CssInvalidMediaFeatureInspection.class,
                                CssInvalidAtRuleInspection.class);
  }

  public void testImportEverywhere() {
    doTest();
  }

  public void testInvalidPseudoClass() {
    doTest();
  }

  public void testMediaFeatureInvalid() {
    doTest();
  }

  public void testBadAtRule() {
    doTest();
  }

  public void testSimpleVariables() {
    myFixture.enableInspections(CssInvalidPropertyValueInspection.class, CssInvalidHtmlTagReferenceInspection.class);
    doTest();
  }

  public void testModuleValueReferences() {
    myFixture.enableInspections(PostCssUnresolvedModuleValueReferenceInspection.class,
                                CssUnknownTargetInspection.class,
                                CssUnresolvedClassInComposesRuleInspection.class,
                                CssInvalidPropertyValueInspection.class);
    doTest();
  }

  private void doTest() {
    myFixture.testHighlighting(true, false, false, getTestName(true) + ".pcss");
  }
}