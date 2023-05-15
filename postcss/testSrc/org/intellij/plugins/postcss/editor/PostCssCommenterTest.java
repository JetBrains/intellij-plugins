package org.intellij.plugins.postcss.editor;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.actionSystem.IdeActions;
import org.intellij.plugins.postcss.PostCssFileType;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.intellij.plugins.postcss.settings.PostCssCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class PostCssCommenterTest extends PostCssFixtureTestCase {
  public void testLineComment() {
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

  public void testLineBlockComment() {
    doTestCommentBlock("a \n{\n  co<caret>lor: red\n}", "a \n{\n  co/*<caret>*/lor: red\n}");
  }

  public void testLineBlockUncomment() {
    doTestCommentBlock("a \n{\n  /*co<caret>lor: red*/\n}", "a \n{\n  co<caret>lor: red\n}");
  }

  public void testSelectionBlockComment() {
    doTestCommentBlock("a \n{\n  <selection>color: red</selection>\n}", "a \n{\n  <selection>/*color: red*/</selection>\n}");
  }

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
    myFixture.configureByText(PostCssFileType.POST_CSS, before);
    myFixture.performEditorAction(actionId);
    myFixture.checkResult(after);
  }
}
