package org.intellij.plugins.postcss.rename;

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
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/rename")
public class PostCssRenameTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testCustomSelector() {
    doTest("new-name");
  }

  @Test
  public void testCustomSelectorWithTwoDashes() {
    doTest("--new-name");
  }

  @Test
  public void testCustomMedia() {
    doTest("new-name");
  }

  @Test
  public void testCustomMediaWithTwoDashes() {
    doTest("--new-name");
  }

  private void doTest(String newName) {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    String testName = postCss.get().getTestName();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename(testName + "-before.pcss", testName + "-after.pcss", newName));
  }
}