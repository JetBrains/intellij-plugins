package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.externalToolsDetector;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

/**
 * NodeJSDetectorThread.java
 *
 * Created by Masahiro Suzuka on 2014/04/22.
 */
public class NodeJSDetectorThread implements Runnable {

  private Project project;

  public NodeJSDetectorThread(Project project) {
    this.project = project;
  }

  @Override
  public void run() {
    final GeneralCommandLine generalCommandLine = new GeneralCommandLine(PhoneGapSettings.NODEJS_PATH, "--version");
    generalCommandLine.setWorkDirectory(project.getBasePath());
    try {
      OSProcessHandler handler = new OSProcessHandler(generalCommandLine);
      handler.startNotify();
      generalCommandLine.createProcess();
    } catch (Exception e) {
      // Node not working
      // Output Notify
      String groupeDisplayId = "PhoneGap notification";
      String notificationTitle = "PhoneGap Plugin";
      String notificationMessage = "NodeJS not detected";
      NotificationType notificationType = NotificationType.ERROR;
      Notification notification = new Notification(groupeDisplayId, notificationTitle, notificationMessage, notificationType);

      Notifications.Bus.notify(notification);
    } finally { }
  }
}
