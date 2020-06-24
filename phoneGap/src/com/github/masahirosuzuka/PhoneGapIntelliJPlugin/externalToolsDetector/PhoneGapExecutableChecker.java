package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.externalToolsDetector;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

/**
 * PhoneGapDetectThread.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/16.
 */
public class PhoneGapExecutableChecker {


  public static void check(Project project) {
    PhoneGapSettings instance = PhoneGapSettings.getInstance();
    if (StringUtil.isEmpty(instance.getExecutablePath())) {
      noPhoneGap();
      return;
    }

    String phoneGapExecutablePath = instance.getExecutablePath();
    final GeneralCommandLine generalCommandLine = new GeneralCommandLine(phoneGapExecutablePath, "--version");
    generalCommandLine.setWorkDirectory(project.getBasePath());
    try {
      final OSProcessHandler handler = new OSProcessHandler(generalCommandLine);
      handler.startNotify();
      generalCommandLine.createProcess();
    }
    catch (Exception e) {
      noPhoneGap();
    }
  }

  private static void noPhoneGap() {
    String groupDisplayId = "PhoneGap notification";
    String notificationTitle = PhoneGapBundle.message("notification.title.cordova.plugin");
    String notificationMessage = PhoneGapBundle.message("notification.content.cordova.has.incorrect.executable.path");
    NotificationType notificationType = NotificationType.ERROR;
    Notification notification = new Notification(groupDisplayId, notificationTitle, notificationMessage, notificationType);

    Notifications.Bus.notify(notification);
  }
}
