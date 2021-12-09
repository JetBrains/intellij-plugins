package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.internal.statistic.eventLog.EventLogGroup;
import com.intellij.internal.statistic.eventLog.events.EventFields;
import com.intellij.internal.statistic.eventLog.events.EventId1;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PlatformioActionBase extends DumbAwareAction {

  public static final EventLogGroup EVENT_LOG_GROUP = new EventLogGroup("cidr.embedded.platformio", 1);
  private static final EventId1<String> COMMAND_EVENT_ID = EVENT_LOG_GROUP.registerEvent(
    "command", EventFields.String("name", FUS_COMMAND.valuesList())
  );

  public static void fusLog(Project project, FUS_COMMAND fusCommand) {
    COMMAND_EVENT_ID.log(project, fusCommand.name());
  }

  private final long myExecutionId;
  @NotNull
  private final Supplier<String> myDynamicText;

  public PlatformioActionBase(@NotNull Supplier<String> dynamicText, @NotNull Supplier<String> dynamicDescription) {
    super(dynamicText, dynamicDescription, null);
    this.myExecutionId = ExecutionEnvironment.getNextUnusedExecutionId();
    this.myDynamicText = dynamicText;
  }

  public abstract @NotNull FUS_COMMAND getFusCommand();

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
    COMMAND_EVENT_ID.log(project, getFusCommand().name());
    tool.executeIfPossible(e, e.getDataContext(), myExecutionId, processListener);
  }

  public enum FUS_COMMAND {
    BUILD,
    BUILD_PRODUCTION,
    CHECK,
    CLEAN,
    CREATE_PROJECT,
    DEBUG,
    HOME,
    INIT,
    MONITOR,
    PROGRAM,
    TEST,
    UPDATE_ALL,
    UPLOAD,
    UPLOADFS;

    public static List<String> valuesList() {
      return Stream.of(values()).map(Enum::name).collect(Collectors.toUnmodifiableList());
    }
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
