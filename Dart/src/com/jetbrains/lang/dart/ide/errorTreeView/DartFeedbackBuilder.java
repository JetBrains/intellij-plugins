package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * Plugins may define alternative implementations that cause issues to be opened
 * for projects other than the Dart plugin. For example, the Flutter plugin may
 * send issues to the flutter project on github.
 */
public interface DartFeedbackBuilder {

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
   * Perform the action required to send feedback.
   *
   * @param project the current project
   */
  void sendFeedback(@Nullable Project project);
}
