package org.intellij.plugins.postcss.usages;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/usages")
public class PostCssFindUsagesTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testCustomSelectors() {
    doTest(2);
  }

  @Test
  public void testCustomSelectorsDifferentCases() {
    doTest(1);
  }

  @Test
  public void testCustomMedia() {
    doTest(2);
  }

  @Test
  public void testCustomMediaDifferentCases() {
    doTest(1);
  }

  private void doTest(int count) {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    assertEquals(count, myFixture.testFindUsages(postCss.get().getTestName() + ".pcss").size());
  }
}