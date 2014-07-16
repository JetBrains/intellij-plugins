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

    private static void noPhoneGap() {
        String groupDisplayId = "PhoneGap notification";
        String notificationTitle = "PhoneGap Plugin";
        String notificationMessage = "PhoneGap/Cordova has incorrect executable path";
        NotificationType notificationType = NotificationType.ERROR;
        Notification notification = new Notification(groupDisplayId, notificationTitle, notificationMessage, notificationType);

        Notifications.Bus.notify(notification);
    }
}
