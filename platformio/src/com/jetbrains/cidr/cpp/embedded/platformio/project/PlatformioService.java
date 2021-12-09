package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable;
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
      project.getService(PlatformioService.class).myState = State.OK;
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
    NOTIFICATION_GROUP
      .createNotification(ClionEmbeddedPlatformioBundle.message("platformio.utility.is.not.found"), ClionEmbeddedPlatformioBundle.message("please.check.system.path"), NotificationType.ERROR)
      .addAction(NotificationAction.createSimple(ClionEmbeddedPlatformioBundle.message("install.guide"), () -> openInstallGuide()))
      .addAction(NotificationAction.createSimpleExpiring(ClionEmbeddedPlatformioBundle.message("open.settings.link"), () -> openSettings(project)))
      .notify(project);
  }

  public static State updateStateForProject(@Nullable Project project) {
    if (project == null) return State.NONE;
    boolean enabled =
      Stream.of(ProjectRootManager.getInstance(project).getContentRoots())
        .anyMatch(root -> root.findChild(PlatformioFileType.FILE_NAME) != null);
    setEnabled(project, enabled);
    return enabled ? State.OK : State.NONE;
  }

  public static void openSettings(@Nullable Project project) {
    ShowSettingsUtil.getInstance().showSettingsDialog(project, PlatformioConfigurable.class);
  }

  public enum State {
    NONE,
    OK
  }
}
