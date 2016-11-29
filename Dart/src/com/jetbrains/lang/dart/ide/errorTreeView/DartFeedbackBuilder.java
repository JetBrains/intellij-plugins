package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Plugins may define alternative implementations that cause issues to be opened
 * for projects other than the Dart plugin. For example, the Flutter plugin may
 * send issues to the flutter project on github.
 */
public interface DartFeedbackBuilder {
  int MAX_URL_LENGTH = 4000;

  ExtensionPointName<DartFeedbackBuilder> EP_NAME = ExtensionPointName.create("Dart.feedbackBuilder");

  @NotNull
  static DartFeedbackBuilder getFeedbackBuilder() {
    final DartFeedbackBuilder[] builders = EP_NAME.getExtensions();
    assert builders.length > 0;
    return builders[0];
  }

  /**
   * The title should indicate what sort of action will occur (eg open a browser).
   *
   * @return The string to display in the title of the confirmation dialog.
   */
  default String title() {
    return "Open Browser";
  }

  /**
   * The prompt should indicate to the user where the issue report will be opened (eg github).
   *
   * @return The string to display in the confirmation dialog.
   */
  String prompt();

  /**
   * The label should indicate what is going to happen when clicked (eg send feedback).
   *
   * @return The string to display as the label of the yes-button in the confirmation dialog.
   */
  default String label() {
    return "Send feedback";
  }

  /**
   * Set the text of the message to be optionally included in the report.
   *
   * @param message
   */
  default void setMessage(String message) {
  }

  /**
   * Perform the action required to send feedback.
   *
   * @param project the current project
   */
  void sendFeedback(@Nullable Project project);

  /**
   * Display a standard query dialog and return the user's response.
   */
  default boolean showQuery(String message) {
    return
      (MessageDialogBuilder.yesNo(title(), message == null ? prompt() : message + "\n" + prompt())
         .icon(Messages.getQuestionIcon())
         .yesText(label())
         .show() == Messages.YES);
  }
}
