package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.notification.*;
import com.intellij.notification.impl.NotificationsManagerImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.BalloonLayout;
import com.intellij.ui.BalloonLayoutData;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;

/**
 * Plugins may define alternative implementations that cause issues to be opened
 * for projects other than the Dart plugin. For example, the Flutter plugin may
 * send issues to the flutter project on github.
 */
public abstract class DartFeedbackBuilder {
  public static final int MAX_URL_LENGTH = 4000;

  // NOTIFICATION_GROUP is used to add an error to Event Log tool window. Red balloon is shown separately, like for IDE fatal errors.
  // We do not show standard balloon using this NOTIFICATION_GROUP because it is not red enough.
  private static final NotificationGroup NOTIFICATION_GROUP =
    new NotificationGroup("Dart Analyzer Error", NotificationDisplayType.NONE, true);

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
   * @param project      the current project
   * @param errorMessage additional information for the issue, such as a tack trace
   * @param serverLog    recent requests made to the analysis server
   */
  public abstract void sendFeedback(@NotNull Project project, @Nullable String errorMessage, @Nullable String serverLog);

  /**
   * Display a standard query dialog and return the user's response.
   *
   * @param message optional, an additional message to display before the prompt
   */
  public boolean showQuery(@Nullable String message) {
    return (MessageDialogBuilder.yesNo(title(), message == null ? prompt() : message + "\n" + prompt())
              .icon(Messages.getQuestionIcon())
              .yesText(label())
              .show() == Messages.YES);
  }

  /**
   * Show a notification that allows the user to submit a feedback issue.
   *
   * @param message      an additional message to display before the prompt
   * @param project      the current project
   * @param errorMessage optional, may be used for stack trace
   * @param debugLog     optional, server traffic log for debugging
   */
  public void showNotification(@NotNull String message,
                               @NotNull Project project,
                               @Nullable String errorMessage,
                               @Nullable String debugLog) {
    String content = message + "<br><a href=\"\">" + prompt() + "</a>";

    NotificationListener listener = new NotificationListener.Adapter() {
      @Override
      protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
        notification.expire();
        sendFeedback(project, errorMessage, debugLog);
      }
    };

    Notification notification =
      NOTIFICATION_GROUP.createNotification(NOTIFICATION_GROUP.getDisplayId(), content, NotificationType.ERROR, listener);

    // this writes to Event Log tool window, but doesn't show balloon (standard balloon is not red enough,
    // we want to show a different balloon similar to the IDE fatal error)
    notification.notify(project);

    // this shows red balloon line in case of an IDE fatal error
    ApplicationManager.getApplication()
      .invokeLater(() -> showErrorNotification(notification, project), ModalityState.NON_MODAL, project.getDisposed());
  }

  private static void showErrorNotification(@NotNull Notification notification, @NotNull Project project) {
    // Adapted from IdeMessagePanel.showErrorNotification()
    IdeFrame myFrame = WindowManager.getInstance().getIdeFrame(project);
    BalloonLayout layout = myFrame.getBalloonLayout();
    assert layout != null;

    BalloonLayoutData layoutData = BalloonLayoutData.createEmpty();
    layoutData.fadeoutTime = 5000;
    layoutData.fillColor = new JBColor(0XF5E6E7, 0X593D41);
    layoutData.borderColor = new JBColor(0XE0A8A9, 0X73454B);

    Balloon balloon = NotificationsManagerImpl.createBalloon(myFrame, notification, false, false, new Ref<>(layoutData), project);
    layout.add(balloon);
  }
}
