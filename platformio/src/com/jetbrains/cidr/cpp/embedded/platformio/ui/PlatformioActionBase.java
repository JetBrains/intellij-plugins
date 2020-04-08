package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.tools.Tool;
import com.jetbrains.cidr.cpp.cmake.CMakeSettings;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeProfileInfo;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.embedded.platformio.CustomTool;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioBaseConfiguration;
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class PlatformioActionBase extends DumbAwareAction {
  private final long myExecutionId;
  @NotNull
  private final Supplier<String> myDynamicText;

  public PlatformioActionBase(@NotNull Supplier<String> dynamicText, @NotNull Supplier<String> dynamicDescription) {
    super(dynamicText, dynamicDescription, null);
    this.myExecutionId = ExecutionEnvironment.getNextUnusedExecutionId();
    this.myDynamicText = dynamicText;
  }

  protected void actionPerformed(@NotNull AnActionEvent e,
                                 @NotNull String arguments,
                                 boolean updateCmake,
                                 boolean appendEnvironmentKey) {
    Project project = e.getProject();
    Tool tool = createPlatformioTool(project, appendEnvironmentKey, arguments, myDynamicText.get());
    if (tool == null) return;
    ProcessAdapter processListener = null;
    if (updateCmake && project != null) {
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
  protected static Tool createPlatformioTool(@Nullable Project project,
                                             boolean appendEnvironmentKey,
                                             @NotNull String argumentsToPass, @NotNull String text) {
    String platformioPath = PlatformioBaseConfiguration.findPlatformio();
    if (platformioPath == null || !(new File(platformioPath).canExecute())) {
      PlatformioService.notifyPlatformioNotFound(project);
      return null;
    }
    //todo try default profile
    StringBuilder tabTitle = new StringBuilder("PlatformIO ").append(text);
    if (appendEnvironmentKey && project != null) {
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

    Tool tool = new CustomTool(tabTitle);
    tool.setProgram(platformioPath);
    tool.setParameters(argumentsToPass);
    return tool;
  }
}
