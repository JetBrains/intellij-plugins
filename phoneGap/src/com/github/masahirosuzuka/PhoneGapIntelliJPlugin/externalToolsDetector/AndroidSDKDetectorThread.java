package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.externalToolsDetector;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

/**
 * Created by Masahiro Suzuka on 2014/04/23.
 */
public class AndroidSDKDetectorThread implements Runnable {

  private Project project;

  public AndroidSDKDetectorThread(Project project) {
    this.project = project;
  }

  @Override
  public void run() {
    final GeneralCommandLine generalCommandLine = new GeneralCommandLine(PhoneGapSettings.ANDROID_SDK, "list");
    generalCommandLine.setWorkDirectory(project.getBasePath());
    try {
      OSProcessHandler handler = new OSProcessHandler(generalCommandLine);
      handler.startNotify();
      generalCommandLine.createProcess();
    } catch (Exception e) {
      // AndroidSDK not working
      // Output Notify
      String groupeDisplayId = "PhoneGap notification";
      String notificationTitle = "PhoneGap Plugin";
      String notificationMessage = "AndroidSDK not detected";
      NotificationType notificationType = NotificationType.ERROR;
      Notification notification = new Notification(groupeDisplayId, notificationTitle, notificationMessage, notificationType);

      Notifications.Bus.notify(notification);
    } finally { }
  }
}
