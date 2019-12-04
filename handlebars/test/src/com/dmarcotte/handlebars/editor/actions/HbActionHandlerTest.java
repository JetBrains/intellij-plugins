package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.file.HbFileType;
import com.intellij.codeInsight.generation.CommentByBlockCommentHandler;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

/**
 * Base test for plugin action handlers<br/>
 * <br/>
 * In general, all the tests here work on the same principles: run an action on the given "before" text and compare
 * the result to the given "expected" text.<br/>
 * <br/>
 * Both "before" and "expected" text must include substring "&lt;caret&gt;" to indicate the caret position in the text.<br/>
 * <br/>
 * Optionally, a chunk of the given text may be marked with "&lt;selection&gt;SELECTED TEXT&lt;/selection&gt;"
 * to indicate a selection.
 */
public abstract class HbActionHandlerTest extends BasePlatformTestCase {
  private void performWriteAction(final Project project, final Runnable action) {
    ApplicationManager.getApplication().runWriteAction(() -> CommandProcessor.getInstance().executeCommand(project, action, "test command", null));
  }

  private void validateTestStrings(@NotNull String before, @NotNull String expected) {
    if (!before.contains("<caret>")
        || !expected.contains("<caret>")) {
      throw new IllegalArgumentException("Test strings must contain \"<caret>\" to indicate caret position");
    }
  }

  /**
   * Call this method to test behavior when the given charToType is typed at the &lt;caret&gt;.
   * See class documentation for more info: {@link HbActionHandlerTest}
   */
  void doCharTest(final char charToType, @NotNull String before, @NotNull String expected) {
    EditorActionManager.getInstance();
    final TypedAction typedAction = TypedAction.getInstance();
    doExecuteActionTest(before, expected, () -> typedAction.actionPerformed(myFixture.getEditor(), charToType, ((EditorEx)myFixture.getEditor()).getDataContext()));
  }

  /**
   * Call this method to test behavior when Enter is typed.
   * See class documentation for more info: {@link HbActionHandlerTest}
   */
  protected void doEnterTest(@NotNull String before, @NotNull String expected) {
    final EditorActionHandler enterActionHandler = EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_ENTER);
    doExecuteActionTest(before, expected,
                        () -> enterActionHandler.execute(myFixture.getEditor(), null, ((EditorEx)myFixture.getEditor()).getDataContext()));
  }

  /**
   * Call this method to test behavior when the "Comment with Line Comment" action is executed.
   * See class documentation for more info: {@link HbActionHandlerTest}
   */
  void doLineCommentTest(@NotNull String before, @NotNull String expected) {
    doExecuteActionTest(before, expected,
                        () -> PlatformTestUtil.invokeNamedAction(IdeActions.ACTION_COMMENT_LINE));
  }

  /**
   * Call this method to test behavior when the "Comment with Block Comment" action is executed.
   * See class documentation for more info: {@link HbActionHandlerTest}
   */
  void doBlockCommentTest(@NotNull String before, @NotNull String expected) {
    doExecuteActionTest(before, expected, () -> new CommentByBlockCommentHandler().invoke(myFixture.getProject(), myFixture.getEditor(),
                                                                                      myFixture.getEditor().getCaretModel().getPrimaryCaret(), myFixture.getFile()));
  }

  private void doExecuteActionTest(@NotNull String before, @NotNull String expected, @NotNull Runnable action) {
    validateTestStrings(before, expected);

    myFixture.configureByText(HbFileType.INSTANCE, before);
    performWriteAction(myFixture.getProject(), action);
    myFixture.checkResult(expected);
  }
}
