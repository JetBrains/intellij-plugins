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

import static com.intellij.execution.runners.ExecutionEnvironment.getNextUnusedExecutionId;
import static com.jetbrains.cidr.cpp.embedded.platformio.PlatformioBaseConfiguration.findPlatformio;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService.notifyPlatformioNotFound;
import static com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration.getSelectedBuildProfile;
import static java.util.stream.Collectors.toUnmodifiableList;

public abstract class PlatformioActionBase extends DumbAwareAction {

  public static final EventLogGroup EVENT_LOG_GROUP = new EventLogGroup("cidr.embedded.platformio", 1);
  private static final EventId1<String> COMMAND_EVENT_ID = EVENT_LOG_GROUP.registerEvent(
    "command", EventFields.String("name", FUS_COMMAND.valuesList())
  );

  public static void fusLog(final @NotNull Project project, final @NotNull FUS_COMMAND fusCommand) {
    COMMAND_EVENT_ID.log(project, fusCommand.name());
  }

  private final long myExecutionId;
  @NotNull
  private final Supplier<String> myDynamicText;

  public PlatformioActionBase(final @NotNull Supplier<String> dynamicText, final @NotNull Supplier<String> dynamicDescription) {
    super(dynamicText, dynamicDescription, null);
    this.myExecutionId = getNextUnusedExecutionId();
    this.myDynamicText = dynamicText;
  }

  public abstract @NotNull FUS_COMMAND getFusCommand();

  protected void actionPerformed(final @NotNull AnActionEvent e,
                                 final @NotNull String arguments,
                                 final boolean updateCmake,
                                 final boolean appendEnvironmentKey) {
    final var project = e.getProject();
    final var tool = createPlatformioTool(project, appendEnvironmentKey, arguments, myDynamicText.get());
    if (tool == null) return;
    ProcessAdapter processListener = null;
    if (updateCmake && project != null) {
      processListener = new ProcessAdapter() {

        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
          if (project.isInitialized()) {
            final var cMakeWorkspace = CMakeWorkspace.getInstance(project);
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
      return Stream.of(values()).map(Enum::name).collect(toUnmodifiableList());
    }
  }

  @Nullable
  protected static Tool createPlatformioTool(final @Nullable Project project,
                                             final boolean appendEnvironmentKey,
                                             @NotNull String argumentsToPass,
                                             final @NotNull String text) {
    final var platformioPath = findPlatformio();
    if (platformioPath == null || !(new File(platformioPath).canExecute())) {
      notifyPlatformioNotFound(project);
      return null;
    }
    //todo try default profile
    final var tabTitle = new StringBuilder("PlatformIO ").append(text);
    if (appendEnvironmentKey && project != null) {
      final var cMakeWorkspace = CMakeWorkspace.getInstance(project);
      final var selectedBuildProfile = getSelectedBuildProfile(project);
      if (selectedBuildProfile != null) {
        final var profileName = selectedBuildProfile.getProfileName();
        final Optional<CMakeSettings.Profile> profile =
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

    final var tool = new CustomTool(tabTitle);
    tool.setProgram(platformioPath);
    tool.setParameters(argumentsToPass);
    return tool;
  }
}
