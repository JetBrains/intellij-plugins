package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.externalToolsDetector;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

/**
 * Created by Masahiro Suzuka on 2014/04/25.
 */
public class iOSSDKdetectorThread implements Runnable{

  private Project project;

  public iOSSDKdetectorThread(final Project project) {
    this.project = project;
  }

  @Override
  public void run() {
    final GeneralCommandLine generalCommandLine = new GeneralCommandLine(PhoneGapSettings.IOS_SIM, "--version");
    generalCommandLine.setWorkDirectory(project.getBasePath());
    try {
      OSProcessHandler handler = new OSProcessHandler(generalCommandLine);
      // handler.notifyAll();
      generalCommandLine.createProcess();
    } catch (Exception e) {
      /* ios-sim not run */
      String groupeDisplayId = "PhoneGap notification";
      String notificationTitle = "PhoneGap Plugin";
      String notificationMessage = "ios-sim not detected";
      NotificationType notificationType = NotificationType.ERROR;
      Notification notification = new Notification(groupeDisplayId, notificationTitle, notificationMessage, notificationType);

      Notifications.Bus.notify(notification);
    } finally { }
  }
}
