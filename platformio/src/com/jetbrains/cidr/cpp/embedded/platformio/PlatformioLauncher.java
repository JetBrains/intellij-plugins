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
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.ui.content.Content;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.cpp.cmake.CMakeSettings;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeProfileInfo;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.embedded.EmbeddedBundle;
import com.jetbrains.cidr.cpp.execution.CLionLauncher;
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

public class PlatformioLauncher extends CLionLauncher {
  private final String[] cliParameters;


  public PlatformioLauncher(@NotNull ExecutionEnvironment executionEnvironment,
                            @NotNull PlatformioBaseConfiguration configuration,
                            String @Nullable [] cliParameters) {
    super(executionEnvironment, configuration);
    this.cliParameters = cliParameters;
  }

  @NotNull
  @Override
  protected Pair<File, CPPEnvironment> getRunFileAndEnvironment() {
    PlatformioBaseConfiguration configuration = (PlatformioBaseConfiguration)getConfiguration();
    CPPToolchains.Toolchain toolchain = configuration.getToolchain();
    return new Pair<>(toolchain.getDebugger().getGdbExecutable(), new CPPEnvironment(toolchain));
  }

  @NotNull
  @Override
  protected GeneralCommandLine createCommandLine(@NotNull CommandLineState state,
                                                 @NotNull File runFile,
                                                 @NotNull CPPEnvironment environment,
                                                 boolean usePty) throws ExecutionException {
    GeneralCommandLine commandLine = super.createCommandLine(state, runFile, environment, usePty);
    commandLine.setWorkDirectory(getProjectBaseDir());
    if (cliParameters != null) {
      commandLine.addParameters(cliParameters);
    }
    Optional<String> cmakeBuildType = getCmakeBuildType(state);
    cmakeBuildType.ifPresent(s -> commandLine.addParameters("-e", s));
    return commandLine;
  }

  @Override
  @NotNull
  public CidrDebugProcess createDebugProcess(@NotNull CommandLineState commandLineState, @NotNull XDebugSession xDebugSession)
    throws ExecutionException {

    Optional<String> cmakeBuildType = getCmakeBuildType(commandLineState);
    DebuggerDriverConfiguration debuggerDriverConfiguration = new CLionGDBDriverConfiguration(getConfiguration().getProject(),
                                                                                              ((PlatformioBaseConfiguration)getConfiguration())
                                                                                                .getToolchain(),
                                                                                              getConfiguration().isElevated()) {
      @Override
      public @NotNull
      GeneralCommandLine createDriverCommandLine(@NotNull DebuggerDriver driver, @NotNull ArchitectureType architectureType)
        throws ExecutionException {
        GeneralCommandLine driverCommandLine = super.createDriverCommandLine(driver, architectureType)
          .withWorkDirectory(getProject().getBasePath())
          .withRedirectErrorStream(true);
        ParametersList parametersList = driverCommandLine.getParametersList();
        parametersList.clearAll();
        parametersList.addAll("debug", "--interface=gdb", "--interpreter=mi2", "-x", ".pioinit", "--iex", "set mi-async on");
        cmakeBuildType.ifPresent(s -> parametersList.addAll("-e", s));
        return driverCommandLine;
      }
    };
    @SystemIndependent final String projectPath = getProject().getBasePath();
    final VirtualFileSystem vfs = LocalFileSystem.getInstance();
    if (projectPath == null ||
        Objects.requireNonNull(vfs.findFileByPath(projectPath)).findChild(PlatformioFileType.FILE_NAME) == null) {
      throw new ExecutionException(ClionEmbeddedPlatformioBundle.message("file.is.not.found", PlatformioFileType.FILE_NAME));
    }
    GeneralCommandLine commandLine = new GeneralCommandLine("").withWorkDirectory(projectPath);
    TrivialRunParameters parameters = new TrivialRunParameters(debuggerDriverConfiguration, commandLine, ArchitectureType.UNKNOWN);

    final ConsoleFilterProvider consoleCopyFilter = project -> new Filter[]{(s, i) -> {
      xDebugSession.getConsoleView().print(s, ConsoleViewContentType.NORMAL_OUTPUT);
      return null;
    }};

    return new CidrDebugProcess(parameters, xDebugSession, commandLineState.getConsoleBuilder(),
                                consoleCopyFilter) {

      @Override
      public @NotNull
      XDebugTabLayouter createTabLayouter() {
        CidrDebugProcess gdbDebugProcess = this;
        XDebugTabLayouter innerLayouter = super.createTabLayouter();
        return new XDebugTabLayouter() {
          @NotNull
          @Override
          public Content registerConsoleContent(@NotNull RunnerLayoutUi ui, @NotNull ExecutionConsole console) {
            return innerLayouter.registerConsoleContent(ui, console);
          }

          @Override
          public void registerAdditionalContent(@NotNull RunnerLayoutUi ui) {
            innerLayouter.registerAdditionalContent(ui);
            SvdPanel.registerPeripheralTab(gdbDebugProcess, ui);
          }
        };
      }

      @Override
      protected @NotNull
      DebuggerDriver.Inferior doLoadTarget(@NotNull DebuggerDriver driver) throws ExecutionException {

        DebuggerDriver.Inferior tempInferior = ((GDBDriver)driver).loadForRemote("", null, null, Collections.emptyList());
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

  private Optional<String> getCmakeBuildType(@NotNull CommandLineState commandLineState) throws ExecutionException {
    CMakeWorkspace workspace = CMakeWorkspace.getInstance(getProject());
    if (!workspace.isInitialized()) {
      throw new ExecutionException(ClionEmbeddedPlatformioBundle.message("cmake.workspace.is.not.initialized"));
    }
    String buildProfileId = CMakeBuildProfileExecutionTarget.getProfileName(commandLineState.getExecutionTarget());
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
  protected void collectAdditionalActions(@NotNull CommandLineState state,
                                          @NotNull ProcessHandler processHandler,
                                          @NotNull ExecutionConsole console,
                                          @NotNull List<? super AnAction> actions) throws ExecutionException {

    actions.add(new AnAction(() -> "Reset", () -> EmbeddedBundle.message("mcu.reset.action.description"), CLionEmbeddedIcons.ResetMcu) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        XDebugSession session = XDebuggerManager.getInstance(getProject()).getDebugSession(console);
        if (session != null) {
          ((CidrDebugProcess)session.getDebugProcess()).postCommand(
            drv -> ((GDBDriver)drv).interruptAndExecuteConsole("pio_reset_halt_target")
          );
        }
      }
    });
  }
}
