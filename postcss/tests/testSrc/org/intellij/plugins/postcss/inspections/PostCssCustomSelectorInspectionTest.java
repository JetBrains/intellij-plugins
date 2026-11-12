package org.intellij.plugins.postcss.inspections;

import com.intellij.openapi.project.Project;
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
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/inspections/customSelectors")
public class PostCssCustomSelectorInspectionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.enableInspections(PostCssCustomSelectorInspection.class, CssInvalidPseudoSelectorInspection.class);
  }

  @Test
  public void testIncorrectCustomSelectorName() {
    doTest();
  }

  @Test
  public void testEmptyCustomSelectorName() {
    doTest();
  }

  @Test
  public void testEmptySelectorList() {
    doTest();
  }

  @Test
  public void testEmptySelectorListTwoInLine() {
    doTest();
  }

  @Test
  public void testEmptyCustomSelector() {
    doTest();
  }

  @Test
  public void testCustomSelectorWithoutDashes() {
    doTest();
  }

  @Test
  public void testCustomSelectorWithInvalidTokenAt() {
    doTest();
  }

  @Test
  public void testCustomSelectorWithInvalidTokenSlash() {
    doTest();
  }

  @Test
  public void testPseudoSelectorWithNesting() {
    doTest();
  }

  @Test
  public void testUnknownCustomSelectorName() {
    doTest();
  }

  @Test
  public void testKnownCustomSelectorName() {
    doTest();
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.testHighlighting(true, false, false, postCss.get().getTestName() + ".pcss");
  }
}