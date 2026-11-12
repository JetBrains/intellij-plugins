package org.intellij.plugins.postcss.inspections;

import com.intellij.openapi.project.Project;
import com.intellij.psi.css.inspections.invalid.CssInvalidAtRuleInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidHtmlTagReferenceInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidImportInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidMediaFeatureInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidPropertyValueInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection;
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection;
import com.intellij.psi.css.inspections.invalid.CssUnresolvedClassInComposesRuleInspection;
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
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/inspections")
public class PostCssCssInspectionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.enableInspections(CssInvalidImportInspection.class,
                                CssInvalidPseudoSelectorInspection.class,
                                CssInvalidMediaFeatureInspection.class,
                                CssInvalidAtRuleInspection.class);
  }

  @Test
  public void testImportEverywhere() {
    doTest();
  }

  @Test
  public void testInvalidPseudoClass() {
    doTest();
  }

  @Test
  public void testMediaFeatureInvalid() {
    doTest();
  }

  @Test
  public void testBadAtRule() {
    doTest();
  }

  @Test
  public void testSimpleVariables() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.enableInspections(CssInvalidPropertyValueInspection.class, CssInvalidHtmlTagReferenceInspection.class);
    doTest();
  }

  @Test
  public void testModuleValueReferences() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.enableInspections(PostCssUnresolvedModuleValueReferenceInspection.class,
                                CssUnknownTargetInspection.class,
                                CssUnresolvedClassInComposesRuleInspection.class,
                                CssInvalidPropertyValueInspection.class);
    doTest();
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.testHighlighting(true, false, false, postCss.get().getTestName() + ".pcss");
  }
}