package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.lookup.LookupElement;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/completion/nestingRuleset")
public class PostCssNestingCompletionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testPropertiesBeforeTag() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile(postCss.get().getTestName() + ".pcss");
    LookupElement[] items = EdtTestUtil.runInEdtAndGet(() -> myFixture.completeBasic());
    assertEquals(0, items.length);
  }

  @Test
  public void testPropertiesLineBeforeTag() {
    postCss.get().doTestPreferred("padding");
  }

  @Test
  public void testTagsBeforeProperty() {
    postCss.get().doTestCompletion();
  }
}
