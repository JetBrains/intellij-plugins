package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

import static com.intellij.ide.BrowserUtil.browse;
import static com.intellij.notification.NotificationType.ERROR;
import static com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType.FILE_NAME;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService.State.NONE;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService.State.OK;

@Service
public final class PlatformioService {

  public static final NotificationGroup NOTIFICATION_GROUP =
    NotificationGroupManager.getInstance().getNotificationGroup("PlatformIO plugin");
  private State myState = NONE;

  public PlatformioService() { }

  public @NotNull State getState() {
    return myState;
  }

  public static void setEnabled(final @NotNull Project project, final boolean enabled) {
    if (enabled) {
      project.getService(PlatformioService.class).myState = OK;
    }
    else {
      final var serviceIfCreated = project.getServiceIfCreated(PlatformioService.class);
      if (serviceIfCreated != null) serviceIfCreated.myState = NONE;
    }
  }

  public static void openInstallGuide() {
    browse("https://docs.platformio.org/en/latest/core/installation.html");
  }

  public static State getState(final @Nullable Project project) {
    if (project == null || project.isDefault()) return NONE;
    PlatformioService platformioService = project.getServiceIfCreated(PlatformioService.class);
    return platformioService == null ? NONE : platformioService.getState();
  }

  public static void notifyPlatformioNotFound(@Nullable Project project) {
    NOTIFICATION_GROUP
      .createNotification(ClionEmbeddedPlatformioBundle.message("platformio.utility.is.not.found"),
                          ClionEmbeddedPlatformioBundle.message("please.check.system.path"), ERROR)
      .addAction(NotificationAction.createSimple(ClionEmbeddedPlatformioBundle.message("install.guide"), () -> openInstallGuide()))
      .addAction(
        NotificationAction.createSimpleExpiring(ClionEmbeddedPlatformioBundle.message("open.settings.link"), () -> openSettings(project)))
      .notify(project);
  }

  public static State updateStateForProject(@Nullable Project project) {
    if (project == null) return NONE;
    boolean enabled =
      Stream.of(ProjectRootManager.getInstance(project).getContentRoots())
        .anyMatch(root -> root.findChild(FILE_NAME) != null);
    setEnabled(project, enabled);
    return enabled ? OK : NONE;
  }

  public static void openSettings(@Nullable Project project) {
    ShowSettingsUtil.getInstance().showSettingsDialog(project, PlatformioConfigurable.class);
  }

  public enum State {
    NONE,
    OK
  }
}
