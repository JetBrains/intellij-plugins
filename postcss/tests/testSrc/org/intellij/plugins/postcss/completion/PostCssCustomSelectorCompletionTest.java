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
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/completion/customSelector")
public class PostCssCustomSelectorCompletionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testCustomSelectorTopLevel() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomSelectorInsideRuleset() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomSelectorInsideAtRule() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomSelectorInsideNest() {
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
  public void testCustomSelectorOneDefinition() {
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomSelectorOneDefinitionIsFirstVariant() {
    postCss.get().doTestPreferred("--test");
  }

  @Test
  public void testCustomSelectorTwoDefinitions() {
    postCss.get().doTestPreferred("--test", "--test2");
  }

  @Test
  public void testCustomSelectorPriorityWithImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    // order of completion items is different on different classpaths:
    // in Webstorm "--test" is the first one (alpha-sort),
    // in IntelliJ IDEA com.intellij.psi.util.proximity.ExplicitlyImportedWeigher bubbles "--z-in-file" up, because it is in the same file
    postCss.get().doTestPreferredNotStrict(Pair.create("--test", "definition.pcss:1"),
                             Pair.create("--z-in-file", "customSelectorPriorityWithImport.pcss:3"));
  }

  @Test
  public void testCustomSelectorPriorityWithoutImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestPreferred(Pair.create("--z-in-file", "customSelectorPriorityWithoutImport.pcss:1"), Pair.create("--test", "definition.pcss:1"));
  }

  @Test
  public void testCustomSelectorWithImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomSelectorWithPartialImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomSelectorWithoutImport() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile("definition.pcss");
    postCss.get().doTestCompletion();
  }

  @Test
  public void testCustomSelectorInInline() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    String testName = postCss.get().getTestName();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testCompletion(testName + ".html", testName + "_after.html"));
  }

  @Test
  public void testTwoColons() {
    postCss.get().doTestCompletion();
  }
}
