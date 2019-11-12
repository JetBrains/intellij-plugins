// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Plugins may define alternative implementations that cause issues to be opened
 * for projects other than the Dart plugin. For example, the Flutter plugin may
 * send issues to the flutter project on github.
 *
 * @deprecated This extension point and class are no longer supported.
 */
@Deprecated
public abstract class DartFeedbackBuilder {
  /**
   * The title should indicate what sort of action will occur (eg open a browser).
   *
   * @return The string to display in the title of the confirmation dialog.
   * @deprecated This extension point and class are no longer supported.
   */
  @Deprecated
  public String title() {
    return "Open Browser";
  }

  /**
   * The prompt should indicate to the user where the issue report will be opened (eg github).
   *
   * @return The string to display in the confirmation dialog.
   * @deprecated This extension point and class are no longer supported.
   */
  @Deprecated
  public abstract String prompt();

  /**
   * The label should indicate what is going to happen when clicked (eg send feedback).
   *
   * @return The string to display as the label of the yes-button in the confirmation dialog.
   * @deprecated This extension point and class are no longer supported.
   */
  @Deprecated
  public String label() {
    return "Send feedback";
  }

  /**
   * Perform the action required to send feedback.
   *
   * @param project      the current project
   * @param errorMessage additional information for the issue, such as a tack trace
   * @param serverLog    recent requests made to the analysis server
   * @deprecated This extension point and class are no longer supported.
   */
  @Deprecated
  public abstract void sendFeedback(@NotNull Project project, @Nullable String errorMessage, @Nullable String serverLog);
}
