package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Plugins may define alternative implementations that cause issues to be opened
 * for projects other than the Dart plugin. For example, the Flutter plugin may
 * send issues to the flutter project on github.
 */
public abstract class DartFeedbackBuilder {
  public static final int MAX_URL_LENGTH = 4000;
  private static boolean ShowPrompt = true;

  private static ExtensionPointName<DartFeedbackBuilder> EP_NAME = ExtensionPointName.create("Dart.feedbackBuilder");

  @NotNull
  public static DartFeedbackBuilder getFeedbackBuilder() {
    final DartFeedbackBuilder[] builders = EP_NAME.getExtensions();
    assert builders.length > 0;
    return builders[0];
  }

  /**
   * The title should indicate what sort of action will occur (eg open a browser).
   *
   * @return The string to display in the title of the confirmation dialog.
   */
  public String title() {
    return "Open Browser";
  }

  /**
   * The prompt should indicate to the user where the issue report will be opened (eg github).
   *
   * @return The string to display in the confirmation dialog.
   */
  public abstract String prompt();

  /**
   * The label should indicate what is going to happen when clicked (eg send feedback).
   *
   * @return The string to display as the label of the yes-button in the confirmation dialog.
   */
  public String label() {
    return "Send feedback";
  }

  /**
   * Perform the action required to send feedback.
   *
   * @param project the current project
   * @param errorMessage additional information for the issue, such as a tack trace
   * @param serverLog recent requests made to the analysis server
   */
  public abstract void sendFeedback(@NotNull Project project, @Nullable String errorMessage, @Nullable String serverLog);

  /**
   * Display a standard query dialog and return the user's response.
   */
  public boolean showQuery(String message) {
    if (ShowPrompt) {
      return
        (MessageDialogBuilder.yesNo(title(), message == null ? prompt() : message + "\n" + prompt())
           .icon(Messages.getQuestionIcon())
           .doNotAsk(getDoNotAskOption())
           .yesText(label())
           .show() == Messages.YES);
    } else {
      return false;
    }
  }

  /**
   * Return a DoNotAskOption option for the MessageDialogBuilder.
   */
  public DialogWrapper.DoNotAskOption getDoNotAskOption() {
    return getDefaultDoNotAskOption();
  }

  static DialogWrapper.DoNotAskOption getDefaultDoNotAskOption() {
    return new DialogWrapper.DoNotAskOption.Adapter() {
      @Override
      public void rememberChoice(boolean isSelected, int exitCode) {
        //noinspection AssignmentToStaticFieldFromInstanceMethod
        ShowPrompt = !isSelected;
      }

      @NotNull
      @Override
      public String getDoNotShowMessage() {
        return DartBundle.message("dart.report.options.do.not.ask");
      }
    };
  }
}
