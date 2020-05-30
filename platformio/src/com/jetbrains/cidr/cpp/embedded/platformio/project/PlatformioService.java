package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

@Service
public final class PlatformioService {
  public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.balloonGroup("PlatformIO plugin");
  private State myState = State.NONE;

  public PlatformioService() {
  }

  public @NotNull State getState() {
    return myState;
  }

  public static void setEnabled(@NotNull Project project, boolean enabled) {
    if (enabled) {
      ServiceManager.getService(project, PlatformioService.class).myState = State.OK;
    }
    else {
      PlatformioService serviceIfCreated = project.getServiceIfCreated(PlatformioService.class);
      if (serviceIfCreated != null) serviceIfCreated.myState = State.NONE;
    }
  }

  public static void openInstallGuide() {
    BrowserUtil.browse("https://docs.platformio.org/en/latest/core/installation.html");
  }

  public static State getState(@Nullable Project project) {
    if (project == null || project.isDefault()) return State.NONE;
    PlatformioService platformioService = project.getServiceIfCreated(PlatformioService.class);
    return platformioService == null ? State.NONE : platformioService.getState();
  }

  public static void notifyPlatformioNotFound(@Nullable Project project) {
    Notification notification = NOTIFICATION_GROUP.createNotification(
      ClionEmbeddedPlatformioBundle.message("platformio.utility.is.not.found"),
      null,
      ClionEmbeddedPlatformioBundle.message("please.check.system.path"),
      NotificationType.ERROR);
    notification.addAction(new AnAction(ClionEmbeddedPlatformioBundle.message("install.guide")) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        openInstallGuide();
      }
    });

    Notifications.Bus.notify(notification, project);
  }

  public static State updateStateForProject(@Nullable Project project) {
    if (project == null) return State.NONE;
    boolean enabled =
      Stream.of(ProjectRootManager.getInstance(project).getContentRoots())
        .anyMatch(root -> root.findChild(PlatformioFileType.FILE_NAME) != null);
    setEnabled(project, enabled);
    return enabled ? State.OK : State.NONE;
  }

  public enum State {
    NONE,
    OK
  }
}
