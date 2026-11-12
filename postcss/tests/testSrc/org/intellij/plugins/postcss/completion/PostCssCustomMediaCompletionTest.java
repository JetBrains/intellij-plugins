package org.intellij.plugins.postcss.completion;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
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
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/completion/customMedia")
public class PostCssCustomMediaCompletionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testCustomMediaTopLevel() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomMediaInsideRuleset() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomMediaInsideAtRule() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomMediaInsideNest() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testSpaceAfterCaret() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testSemicolonAfterCaret() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testSpaceAndSemicolonAfterCaret() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testSemicolonWithWhitespacesAfterCaret() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomMediaType() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomMediaTwoDefinitions() {
    postCss.get().doTestPreferred("--test", "--test2");
  }

  @Test
  public void testCustomMediaPriorityWithImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestPreferred(Pair.create("z-in-file", "customMediaPriorityWithImport.pcss:3"), Pair.create("test", "definition.pcss:1"));
  }

  @Test
  public void testCustomMediaPriorityWithoutImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestPreferred(Pair.create("z-in-file", "customMediaPriorityWithoutImport.pcss:1"), Pair.create("test", "definition.pcss:1"));
  }

  @Test
  public void testCustomMediaWithImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomMediaWithoutImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomMediaInInline() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    String testName = postCss.get().getTestName();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testCompletion(testName + ".html", testName + "_after.html"));
  }
}