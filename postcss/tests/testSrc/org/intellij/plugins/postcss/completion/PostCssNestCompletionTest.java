package org.intellij.plugins.postcss.completion;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/completion/nesting")
public class PostCssNestCompletionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testNestInsideSelector() {
    doTest();
  }

  @Test
  public void testNestInsideSelectorList() {
    doTest();
  }

  @Test
  public void testNestTopLevel() {
    doTest();
  }

  @Test
  public void testNestInsideTopLevelAtRule() {
    doTest();
  }

  @Test
  public void testNestInsidePageAtRule() {
    doTest();
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    String testName = postCss.get().getTestName();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testCompletion(testName + ".pcss", testName + "_after.pcss"));
  }
}
