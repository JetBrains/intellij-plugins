package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service
public final class PlatformioService {
  public static final String PLATFORMIO_IS_NOT_FOUND = "PlatformIO utility is not found";
  public static final String INSTALL_GUIDE = "Install Guide...";
  private State myState = State.NONE;

  public PlatformioService() {
  }

  public @NotNull State getState() {
    return myState;
  }

  public void setState(@NotNull State state) {
    myState = state;
  }

  public void enable(boolean b) {
    myState = b ? State.OK : State.NONE;
  }

  public static void openInstallGuide() {
    BrowserUtil.browse("https://docs.platformio.org/en/latest/core/installation.html");
  }

  public static State getState(@Nullable Project project) {
    if (project == null || project.isDefault()) return State.NONE;
    PlatformioService platformioService = ServiceManager.getServiceIfCreated(project, PlatformioService.class);
    return platformioService == null ? State.NONE : platformioService.getState();
  }

  public static void notifyPlatformioNotFound(@Nullable Project project) {
    Notification notification = new Notification("PlatformIO plugin",
                                                 PLATFORMIO_IS_NOT_FOUND,
                                                 "Please check system path",
                                                 NotificationType.ERROR);
    notification.addAction(new AnAction(INSTALL_GUIDE) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        openInstallGuide();
      }
    });

    Notifications.Bus.notify(notification, project);
  }

  public enum State {
    NONE,
    BROKEN,
    OUTDATED,
    OK
  }
}
