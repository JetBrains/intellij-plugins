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
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/inspections/customMedia")
public class PostCssCustomMediaQuickFixTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.enableInspections(PostCssCustomMediaInspection.class);
  }

  @Test
  public void testAddCustomMediaPrefixEmpty() {
    doTestAddDashes();
  }

  @Test
  public void testAddCustomMediaPrefixDash() {
    doTestAddDashes();
  }

  private void doTestAddDashes() {
    postCss.get().doTestQuickFix("Add '--' to custom media");
  }
}