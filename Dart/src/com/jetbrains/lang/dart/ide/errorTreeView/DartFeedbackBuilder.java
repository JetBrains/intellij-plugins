package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

import static com.intellij.notification.NotificationDisplayType.STICKY_BALLOON;

/**
 * Plugins may define alternative implementations that cause issues to be opened
 * for projects other than the Dart plugin. For example, the Flutter plugin may
 * send issues to the flutter project on github.
 */
public abstract class DartFeedbackBuilder {
  public static final int MAX_URL_LENGTH = 4000;
  private static final NotificationGroup ourNotificationGroup = new NotificationGroup("Dart Analyzer Error",
                                                                                      STICKY_BALLOON, true);

  private static ExtensionPointName<DartFeedbackBuilder> EP_NAME = ExtensionPointName.create("Dart.feedbackBuilder");

  static {
    Notifications.Bus.register(ourNotificationGroup.getDisplayId(), null);
  }

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
   * @param project      the current project
   * @param errorMessage additional information for the issue, such as a tack trace
   * @param serverLog recent requests made to the analysis server
   */
  public abstract void sendFeedback(@NotNull Project project, @Nullable String errorMessage, @Nullable String serverLog);

  /**
   * Display a standard query dialog and return the user's response.
   *
   * @param message optional, an additional message to display before the prompt
   */
  public boolean showQuery(String message) {
    return
      (MessageDialogBuilder.yesNo(title(), message == null ? prompt() : message + "\n" + prompt())
         .icon(Messages.getQuestionIcon())
         .yesText(label())
         .show() == Messages.YES);
  }

  /**
   * Show a notification that allows the user to submit a feedback issue.
   *
   * @param message      optional, an additional message to display before the prompt
   * @param project      optional, the current project
   * @param errorMessage optional, may be used for stack trace
   */
  public void showNotification(String message, @NotNull Project project, String errorMessage, String debugLog) {
    // TODO(messick) work in progress
    IdeFrame frame = WindowManager.getInstance().getIdeFrame(project);
    String link = "<a href=\"\">" + prompt() + "</a>";
    Notifications.Bus.notify(
      new Notification(
        ourNotificationGroup.getDisplayId(),
        title(),
        message == null ? link : message + "<br>" + link,
        NotificationType.ERROR,
        (notification, event) -> {
          if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            notification.expire();
            sendFeedback(project, errorMessage, debugLog);
          }
        }));
  }
}
