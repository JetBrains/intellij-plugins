package org.intellij.plugins.postcss.inspections;

import com.intellij.openapi.project.Project;
import com.intellij.psi.css.inspections.invalid.CssInvalidMediaFeatureInspection;
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
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/inspections/customMedia")
public class PostCssCustomMediaInspectionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.enableInspections(PostCssCustomMediaInspection.class, CssInvalidMediaFeatureInspection.class);
  }

  @Test
  public void testIncorrectCustomMediaName() {
    doTest();
  }

  @Test
  public void testEmptyCustomMediaName() {
    doTest();
  }

  @Test
  public void testEmptyCustomMedia() {
    doTest();
  }

  @Test
  public void testCorrectCustomMedia() {
    doTest();
  }

  @Test
  public void testMediaFeatureCustom() {
    doTest();
  }

  @Test
  public void testMediaFeatureCustomIncorrect() {
    doTest();
  }

  @Test
  public void testMediaFeatureCustomNotDefined() {
    doTest();
  }

  @Test
  public void testMediaFeatureCustomDefinedTwoTimes() {
    doTest();
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.testHighlighting(true, false, false, postCss.get().getTestName() + ".pcss");
  }
}