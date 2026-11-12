package org.intellij.plugins.postcss.smartEnter;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.PostCssFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/smartEnter")
public class PostCssSmartEnterTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testCompleteCustomSelectorWithSemicolon() {
    doTest();
  }

  @Test
  public void testCompleteCustomMediaWithSemicolon() {
    doTest();
  }

  private void doTest() throws IncorrectOperationException {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile(postCss.get().getTestName() + "_before.pcss");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COMPLETE_STATEMENT);
    myFixture.checkResultByFile(postCss.get().getTestName() + "_after.pcss");
  }
}