package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.externalToolsDetector;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

/**
 * PhoneGapDetectThread.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/16.
 */
public class PhoneGapDetectThread implements Runnable/*, ProcessListener*/ {

    private final Project myProject;

    public PhoneGapDetectThread(final Project project) {
        this.myProject = project;
    }

    @Override
    public void run() {
        PhoneGapSettings instance = PhoneGapSettings.getInstance();
        if (!instance.isPhoneGapAvailable()) {
            noPhoneGap();
            return;
        }

        String phoneGapExecutablePath = instance.getPhoneGapExecutablePath();
        final GeneralCommandLine generalCommandLine = new GeneralCommandLine(phoneGapExecutablePath, "--version");
        generalCommandLine.setWorkDirectory(myProject.getBasePath());
        try {
            final OSProcessHandler handler = new OSProcessHandler(generalCommandLine);
            //handler.addProcessListener(this);
            handler.startNotify();
            generalCommandLine.createProcess();
        } catch (Exception e) {
            noPhoneGap();

        }
    }

    private void noPhoneGap() {
        String groupDisplayId = "PhoneGap notification";
        String notificationTitle = "PhoneGap Plugin";
        String notificationMessage = "PhoneGap can't run";
        NotificationType notificationType = NotificationType.ERROR;
        Notification notification = new Notification(groupDisplayId, notificationTitle, notificationMessage, notificationType);

        Notifications.Bus.notify(notification);
    }

  /*
  @Override
  public void startNotified(ProcessEvent processEvent) {

  }

  @Override
  public void processTerminated(ProcessEvent processEvent) {
    int exitCode = processEvent.getExitCode();
    if (exitCode != 0) {
      String groupeDisplayId = "PhoneGap notification";
      String notificationTitle = "PhoneGap";
      String notificationMessage = "PhoneGap not detected";
      NotificationType notificationType = NotificationType.ERROR;
      Notification notification = new Notification(groupeDisplayId, notificationTitle, notificationMessage, notificationType);

      Notifications.Bus.notify(notification);
    }
  }

  @Override
  public void processWillTerminate(ProcessEvent processEvent, boolean b) {

  }

  @Override
  public void onTextAvailable(ProcessEvent processEvent, Key key) {

  }
  */
}
