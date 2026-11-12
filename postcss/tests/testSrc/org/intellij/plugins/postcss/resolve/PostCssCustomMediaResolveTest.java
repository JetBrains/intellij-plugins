package org.intellij.plugins.postcss.resolve;

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
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/resolve/customMedia")
public class PostCssCustomMediaResolveTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testResolve() {
    postCss.get().doTestResolve();
  }

  @Test
  public void testResolveInCustomMediaAtRule() {
    postCss.get().doTestResolve();
  }

  @Test
  public void testResolveWithImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestResolve();
  }

  @Test
  public void testResolveWithoutImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestResolve();
  }

  @Test
  public void testResolveMulti() {
    postCss.get().doTestResolve(2);
  }

  @Test
  public void testResolveMultiInDifferentFiles() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestResolve(2);
  }

  @Test
  public void testInline() {
    postCss.get().doTestResolve(1, "html");
  }
}