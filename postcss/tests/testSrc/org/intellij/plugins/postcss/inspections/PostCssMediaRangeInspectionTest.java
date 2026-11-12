package org.intellij.plugins.postcss.inspections;

import com.intellij.openapi.project.Project;
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
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/inspections/mediaRanges")
public class PostCssMediaRangeInspectionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.enableInspections(PostCssMediaRangeInspection.class);
  }

  @Test
  public void testIncorrectMediaRange() {
    doTest();
  }

  @Test
  public void testIncorrectSignDirection() {
    doTest();
  }

  @Test
  public void testIncorrectSignDirectionWithDifferentOperators() {
    doTest();
  }

  @Test
  public void testCorrectSignDirection() {
    doTest();
  }

  @Test
  public void testEqualSigns() {
    doTest();
  }

  @Test
  public void testOneEqualSignOfTwo() {
    doTest();
  }

  @Test
  public void testOneSign() {
    doTest();
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.testHighlighting(true, false, false, postCss.get().getTestName() + ".pcss");
  }
}