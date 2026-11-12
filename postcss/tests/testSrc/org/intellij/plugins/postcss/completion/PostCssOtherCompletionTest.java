package org.intellij.plugins.postcss.completion;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/completion/other")
public class PostCssOtherCompletionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testSimpleVarAsPropertyValue() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.copyFileToProject(postCss.get().getTestName() + "_imported.pcss");
    postCss.get().doTestCompletionVariants("foo", "bar", "baz", "in_imported");
  }

  @Test
  public void testSimpleVarInterpolationStart() {
    postCss.get().doTestCompletionVariants("foo", "bar", "baz");
  }

  @Test
  public void testSimpleVarInterpolationMiddle() {
    postCss.get().doTestCompletionVariants("bar", "baz");
  }

  @Test
  public void testSimpleVarInterpolationEnd() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testSimpleVarAsSelector() {
    postCss.get().doTestCompletionVariants("foo", "bar", "baz");
  }
}