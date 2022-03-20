package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.ui.content.Content;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.cpp.cmake.CMakeException;
import com.jetbrains.cidr.cpp.cmake.CMakeSettings;
import com.jetbrains.cidr.cpp.cmake.model.CMakeModel;
import com.jetbrains.cidr.cpp.cmake.model.CMakeModelConfigurationData;
import com.jetbrains.cidr.cpp.cmake.model.CMakeVariable;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeProfileInfo;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.embedded.EmbeddedBundle;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase;
import com.jetbrains.cidr.cpp.execution.CLionLauncher;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget;
import com.jetbrains.cidr.cpp.execution.debugger.backend.CLionGDBDriverConfiguration;
import com.jetbrains.cidr.cpp.execution.debugger.embedded.svd.SvdPanel;
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.execution.TrivialRunParameters;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.backend.gdb.GDBDriver;
import icons.CLionEmbeddedIcons;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;
import static com.intellij.openapi.vfs.VfsUtil.findFileByIoFile;
import static com.intellij.openapi.vfs.VfsUtilCore.findRelativeFile;
import static com.intellij.util.containers.ContainerUtil.find;
import static com.jetbrains.cidr.ArchitectureType.UNKNOWN;
import static com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType.FILE_NAME;
import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.fusLog;
import static com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration.getSelectedBuildProfile;
import static com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget.getProfileName;
import static com.jetbrains.cidr.cpp.execution.debugger.embedded.svd.SvdPanel.registerPeripheralTab;
import static icons.CLionEmbeddedIcons.ResetMcu;
import static java.util.Objects.requireNonNull;

public class PlatformioLauncher extends CLionLauncher {
  private final String[] cliParameters;
  private final PlatformioActionBase.FUS_COMMAND command;


  public PlatformioLauncher(final @NotNull ExecutionEnvironment executionEnvironment,
                            final @NotNull PlatformioBaseConfiguration configuration,
                            final @Nullable String[] cliParameters,
                            final @NotNull PlatformioActionBase.FUS_COMMAND command) {
    super(executionEnvironment, configuration);
    this.cliParameters = cliParameters;
    this.command = command;
  }

  @NotNull
  @Override
  protected Pair<File, CPPEnvironment> getRunFileAndEnvironment() {
    final var configuration = (PlatformioBaseConfiguration) getConfiguration();
    final var toolchain = configuration.getToolchain();
    return new Pair<>(toolchain.getDebugger().getGdbExecutable(), new CPPEnvironment(toolchain));
  }

  @NotNull
  @Override
  protected GeneralCommandLine createCommandLine(final @NotNull CommandLineState state,
                                                 final @NotNull File runFile,
                                                 final @NotNull CPPEnvironment environment,
                                                 final boolean usePty) throws ExecutionException {
    final var commandLine = super.createCommandLine(state, runFile, environment, usePty);
    commandLine.setWorkDirectory(getProjectBaseDir());
    if (cliParameters != null) {
      commandLine.addParameters(cliParameters);
    }
    final var cmakeBuildType = getCmakeBuildType(state);
    cmakeBuildType.ifPresent(s -> commandLine.addParameters("-e", s));
    return commandLine;
  }

  @NotNull
  @Override
  public ProcessHandler createProcess(final @NotNull CommandLineState state) throws ExecutionException {
    fusLog(getProject(), command);
    return super.createProcess(state);
  }

  @Override
  @NotNull
  public CidrDebugProcess createDebugProcess(
          final @NotNull CommandLineState commandLineState,
          final @NotNull XDebugSession xDebugSession)
    throws ExecutionException {
    fusLog(getProject(), command);
    final var cmakeBuildType = getCmakeBuildType(commandLineState);
    final var debuggerDriverConfiguration = new CLionGDBDriverConfiguration(getConfiguration().getProject(),
            ((PlatformioBaseConfiguration) getConfiguration()).getToolchain()) {
      @Override
      public @NotNull
      GeneralCommandLine createDriverCommandLine(final @NotNull DebuggerDriver driver, final @NotNull ArchitectureType architectureType)
        throws ExecutionException {
        final var driverCommandLine = super.createDriverCommandLine(driver, architectureType)
          .withWorkDirectory(getProject().getBasePath())
          .withRedirectErrorStream(true);
        final var parametersList = driverCommandLine.getParametersList();
        parametersList.clearAll();
        parametersList.addAll("debug", "--interface=gdb", "--interpreter=mi2", "-x", ".pioinit", "--iex", "set mi-async on");
        cmakeBuildType.ifPresent(s -> parametersList.addAll("-e", s));
        return driverCommandLine;
      }
    };
    @SystemIndependent final String projectPath = getProject().getBasePath();
    final var vfs = LocalFileSystem.getInstance();
    if (projectPath == null || requireNonNull(vfs.findFileByPath(projectPath)).findChild(FILE_NAME) == null) {
      throw new ExecutionException(ClionEmbeddedPlatformioBundle.message("file.is.not.found", FILE_NAME));
    }
    final var commandLine = new GeneralCommandLine("").withWorkDirectory(projectPath);
    final var parameters = new TrivialRunParameters(debuggerDriverConfiguration, commandLine, UNKNOWN);

    final ConsoleFilterProvider consoleCopyFilter = project -> new Filter[]{(s, i) -> {
      xDebugSession.getConsoleView().print(s, NORMAL_OUTPUT);
      return null;
    }};

    final var defaultSvdLocation = findSvdFile(getProject());
    return new CidrDebugProcess(parameters, xDebugSession, commandLineState.getConsoleBuilder(), consoleCopyFilter) {
      @Override
      public @NotNull
      XDebugTabLayouter createTabLayouter() {
        CidrDebugProcess gdbDebugProcess = this;
        final var innerLayouter = super.createTabLayouter();
        return new XDebugTabLayouter() {
          @NotNull
          @Override
          public Content registerConsoleContent(final @NotNull RunnerLayoutUi ui, final @NotNull ExecutionConsole console) {
            return innerLayouter.registerConsoleContent(ui, console);
          }

          @Override
          public void registerAdditionalContent(final @NotNull RunnerLayoutUi ui) {
            innerLayouter.registerAdditionalContent(ui);
            final var panel = registerPeripheralTab(gdbDebugProcess, ui);
            panel.setSvdDefaultLocation(defaultSvdLocation);
          }
        };
      }

      @Override
      protected @NotNull
      DebuggerDriver.Inferior doLoadTarget(final @NotNull DebuggerDriver driver) throws ExecutionException {
        final var tempInferior = driver.loadForRemote("", null, null, List.of());
        return driver.new Inferior(tempInferior.getId()) {
          @SuppressWarnings("RedundantThrows")
          @Override
          protected long startImpl() throws ExecutionException {
            return 0;
          }

          @Override
          protected void detachImpl() throws ExecutionException {
            tempInferior.detach();
          }

          @Override
          protected boolean destroyImpl() throws ExecutionException {
            return tempInferior.destroy();
          }
        };
      }
    };
  }

  private Optional<String> getCmakeBuildType(final @NotNull CommandLineState commandLineState) throws ExecutionException {
    final var workspace = CMakeWorkspace.getInstance(getProject());
    if (!workspace.isInitialized()) {
      throw new ExecutionException(ClionEmbeddedPlatformioBundle.message("cmake.workspace.is.not.initialized"));
    }
    final var buildProfileId = getProfileName(commandLineState.getExecutionTarget());
    return workspace
      .getProfileInfos()
      .stream()
      .map(CMakeProfileInfo::getProfile)
      .filter(cMakeProfile -> cMakeProfile.getName().equals(buildProfileId))
      .findAny()
      .map(CMakeSettings.Profile::getBuildType);
  }

  @SuppressWarnings("RedundantThrows")
  @Override
  protected void collectAdditionalActions(final @NotNull CommandLineState state,
                                          final @NotNull ProcessHandler processHandler,
                                          final @NotNull ExecutionConsole console,
                                          final @NotNull List<? super AnAction> actions) throws ExecutionException {

    actions.add(
      new AnAction(() -> EmbeddedBundle.message("mcu.reset.action.title"), () -> EmbeddedBundle.message("mcu.reset.action.description"), ResetMcu) {
        @Override
        public void actionPerformed(final @NotNull AnActionEvent anActionEvent) {
          final var session = XDebuggerManager.getInstance(getProject()).getDebugSession(console);
          if (session != null) {
            ((CidrDebugProcess) session.getDebugProcess()).postCommand(
              drv -> ((GDBDriver)drv).interruptAndExecuteConsole("pio_reset_halt_target")
            );
          }
        }
      });
  }

  @Nullable
  public static VirtualFile findSvdFile(final @NotNull Project project) {
    final var workspace = CMakeWorkspace.getInstance(project);
    if (!workspace.isInitialized()) {
      return null;
    }

    final var model = workspace.getModel();
    final var selectedBuildProfile = getSelectedBuildProfile(project);
    final var projectDir = workspace.getProjectPath().toFile();
    if (model == null || selectedBuildProfile == null) {
      return null;
    }

    final var profileName = selectedBuildProfile.getProfileName();
    final var configurationData = find(
      model.getConfigurationData(), confData -> profileName.equals(confData.getConfigName()));

    if (configurationData == null) return null;
    try {
      final var variable = configurationData.getCacheConfigurator().findVariable("CLION_SVD_FILE_PATH");
      if (variable == null || variable.getValue() == null) return null;
      return findRelativeFile(variable.getValue(), findFileByIoFile(projectDir, false));
    }
    catch (final CMakeException ignored) {
      return null;
    }
  }
}
