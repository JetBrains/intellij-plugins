package com.intellij.lang.javascript.flex.actions.airmobile;

import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.util.List;

public class PackageMobileAirApplicationAction extends DumbAwareAction {
  public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.balloonGroup("AIR Packaging");

  public void actionPerformed(final AnActionEvent actionEvent) {
    final Project project = getEventProject(actionEvent);
    final PackageMobileAirApplicationDialog dialog = new PackageMobileAirApplicationDialog(project);
    dialog.show();
    if (dialog.isOK()) {
      FileDocumentManager.getInstance().saveAllDocuments();

      final MobileAirPackageParameters parameters = dialog.getPackageParameters();

      final Runnable onSuccess = new Runnable() {
        public void run() {
          final String href = "<a href=''>" + parameters.INSTALLER_FILE_NAME + "</a>";
          final String message = FlexBundle.message("application.created", parameters.MOBILE_PLATFORM, href);

          final NotificationListener listener = new NotificationListener() {
            public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
              notification.expire();
              ShowFilePathAction.open(new File(parameters.INSTALLER_FILE_LOCATION),
                                      new File(parameters.INSTALLER_FILE_LOCATION, parameters.INSTALLER_FILE_NAME));
            }
          };

          NOTIFICATION_GROUP.createNotification(PackageMobileAirApplicationDialog.TITLE, message, NotificationType.INFORMATION, listener)
            .notify(project);
        }
      };

      final Consumer<List<String>> onFailure = new Consumer<List<String>>() {
        public void consume(final List<String> messages) {
          final String message = messages.isEmpty() ? FlexBundle.message("unexpected.empty.adt.output") : StringUtil.join(messages, "<br>");
          final String html =
            message + "<br><a href='http://TryAgain'>Try again</a>"; // + "&nbsp;&nbsp;&nbsp;<a href='http://Dismiss'>Dismiss</a>"

          final NotificationListener listener = new NotificationListener() {
            public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
              notification.expire();
              if ("TryAgain".equals(event.getURL().getHost())) {
                actionPerformed(actionEvent);
              }
            }
          };

          NOTIFICATION_GROUP.createNotification(PackageMobileAirApplicationDialog.TITLE, html, NotificationType.ERROR, listener)
            .notify(project);
        }
      };

      ExternalTask.runInBackground(MobileAirUtil.createMobileAirPackageTask(project, parameters),
                                   FlexBundle.message("packaging.application", parameters.MOBILE_PLATFORM),
                                   onSuccess, onFailure);
    }
  }

  public void update(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    e.getPresentation().setEnabled(project != null && ModuleManager.getInstance(project).getModules().length > 0);
  }
}
