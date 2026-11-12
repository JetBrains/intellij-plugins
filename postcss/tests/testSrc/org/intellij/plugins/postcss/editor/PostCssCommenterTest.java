package org.intellij.plugins.postcss.editor;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssFileType;
import org.intellij.plugins.postcss.PostCssFixture;
import org.intellij.plugins.postcss.settings.PostCssCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/editor")
public class PostCssCommenterTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testLineComment() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    PostCssCodeStyleSettings settings = CodeStyle.getSettings(myFixture.getProject()).getCustomSettings(PostCssCodeStyleSettings.class);
    boolean initialValue = settings.COMMENTS_INLINE_STYLE;
    try {
      settings.COMMENTS_INLINE_STYLE = true;
      doTestCommentLine("a \n{\n  <caret>color: red\n}", "a \n{\n  //color: red\n}");

      settings.COMMENTS_INLINE_STYLE = false;
      doTestCommentLine("a \n{\n  <caret>color: red\n}", "a \n{\n  /*color: red*/\n}");
    }
    finally {
      settings.COMMENTS_INLINE_STYLE = initialValue;
    }
  }

  @Test
  public void testLineBlockComment() {
    doTestCommentBlock("a \n{\n  co<caret>lor: red\n}", "a \n{\n  co/*<caret>*/lor: red\n}");
  }

  @Test
  public void testLineBlockUncomment() {
    doTestCommentBlock("a \n{\n  /*co<caret>lor: red*/\n}", "a \n{\n  co<caret>lor: red\n}");
  }

  @Test
  public void testSelectionBlockComment() {
    doTestCommentBlock("a \n{\n  <selection>color: red</selection>\n}", "a \n{\n  <selection>/*color: red*/</selection>\n}");
  }

  @Test
  public void testBlockCommentOnEmptyLine() {
    doTestCommentBlock("<caret>", "/*<caret>*/");
  }

  protected void doTestCommentLine(@NotNull String before, @NotNull String after) {
    doTestAction(before, after, IdeActions.ACTION_COMMENT_LINE);
  }

  protected void doTestCommentBlock(@NotNull String before, @NotNull String after) {
    doTestAction(before, after, IdeActions.ACTION_COMMENT_BLOCK);
  }

  protected void doTestAction(@NotNull String before, @NotNull String after, @NotNull String actionId) {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByText(PostCssFileType.POST_CSS, before);
    myFixture.performEditorAction(actionId);
    myFixture.checkResult(after);
  }
}
