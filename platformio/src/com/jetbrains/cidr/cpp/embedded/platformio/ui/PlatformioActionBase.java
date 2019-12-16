package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.tools.Tool;
import com.jetbrains.cidr.cpp.cmake.CMakeSettings;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeProfileInfo;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioBaseConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public class PlatformioActionBase extends DumbAwareAction {
  private final long myExecutionId;
  @NotNull
  private final String myArguments;
  private final boolean myAppendEnvironmentKey;
  private final boolean myUpdateCmake;
  @NotNull
  private final String myText;

  public PlatformioActionBase(long executionId, @NotNull String text, @Nullable String description, @NotNull String arguments,
                              boolean updateCmake, boolean appendEnvironmentKey) {
    super(text, description, null);
    this.myExecutionId = executionId;
    this.myArguments = arguments;
    this.myAppendEnvironmentKey = appendEnvironmentKey;
    this.myUpdateCmake = updateCmake;
    this.myText = text;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    Tool tool = createPlatformioTool(project);
    if (tool == null) return;
    ProcessAdapter processListener = null;
    if (myUpdateCmake && project != null) {
      processListener = new ProcessAdapter() {

        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
          if (project.isInitialized()) {
            final CMakeWorkspace cMakeWorkspace = CMakeWorkspace.getInstance(project);
            cMakeWorkspace.scheduleClearGeneratedFilesAndReload();
          }
        }
      };
    }
    tool.executeIfPossible(e, e.getDataContext(), myExecutionId, processListener);
  }

  @Nullable
  public Tool createPlatformioTool(@Nullable Project project) {
    String platformioPath = PlatformioBaseConfiguration.findPlatformio();
    if (platformioPath == null || !(new File(platformioPath).canExecute())) {
      Notifications.Bus.notify(
        new Notification("org.platformio", "PlatfotmIO utility is not found", "Please check system path", NotificationType.ERROR),
        project);
      return null;
    }
    String argumentsToPass = myArguments;
    StringBuilder tabTitle = new StringBuilder("PlatformIO ").append(myText);
    if (myAppendEnvironmentKey && project != null) {
      final CMakeWorkspace cMakeWorkspace = CMakeWorkspace.getInstance(project);
      CMakeBuildProfileExecutionTarget selectedBuildProfile = CMakeAppRunConfiguration.getSelectedBuildProfile(project);
      if (selectedBuildProfile != null) {
        String profileName = selectedBuildProfile.getProfileName();
        Optional<CMakeSettings.Profile> profile =
          cMakeWorkspace.getProfileInfos()
            .stream()
            .map(CMakeProfileInfo::getProfile)
            .filter(p -> Objects.equals(profileName, p.getName()))
            .findAny();
        if (profile.isPresent()) {
          argumentsToPass += " -e " + profile.get().getBuildType();
          tabTitle.append(" (").append(profileName).append(")");
        }
      }
    }

    Tool tool = new MyTool(tabTitle);
    tool.setProgram(platformioPath);
    tool.setParameters(argumentsToPass);
    return tool;
  }

  private static class MyTool extends Tool {
    private final StringBuilder tabTitle;

    MyTool(StringBuilder tabTitle) {
      this.tabTitle = tabTitle;
    }

    @Override
    public boolean isUseConsole() {
      return true;
    }

    @Override
    public boolean isShowConsoleOnStdOut() {
      return true;
    }

    @Override
    public boolean isShowConsoleOnStdErr() {
      return true;
    }

    @Override
    public String getName() {
      return tabTitle.toString();
    }
  }
}
