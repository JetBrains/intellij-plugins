package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.clion.embedded.execution.custom.McuResetAction;
import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.ui.content.Content;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioCliBuilder;
import com.jetbrains.cidr.cpp.execution.CLionLauncher;
import com.jetbrains.cidr.cpp.execution.debugger.backend.CLionGDBDriverConfiguration;
import com.jetbrains.cidr.cpp.execution.debugger.peripheralview.SvdPanel;
import com.jetbrains.cidr.cpp.toolchains.CPPDebugger;
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.cpp.toolchains.TrivialNativeToolchain;
import com.jetbrains.cidr.execution.CidrCoroutineHelper;
import com.jetbrains.cidr.execution.CidrPathConsoleFilter;
import com.jetbrains.cidr.execution.TrivialRunParameters;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.toolchains.OSType;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBaseKt.notifyUploadUnavailable;

public class PlatformioLauncher extends CLionLauncher {

  public PlatformioLauncher(@NotNull ExecutionEnvironment executionEnvironment,
                            @NotNull PlatformioDebugConfiguration configuration) {
    super(executionEnvironment, configuration);
  }

  @Override
  protected boolean emulateTerminal(@NotNull CPPEnvironment environment, boolean isDebugMode) {
    return false;
  }

  @Override
  public @NotNull Pair<File, CPPEnvironment> getRunFileAndEnvironment() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected @NotNull GeneralCommandLine createCommandLine(@NotNull CommandLineState state,
                                                          @NotNull File runFile,
                                                          @NotNull CPPEnvironment environment,
                                                          boolean usePty,
                                                          boolean emulateTerminal) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull ProcessHandler createProcess(@NotNull CommandLineState state) throws ExecutionException {
    return CidrCoroutineHelper.runOnEDT(() -> {
      ActionManager actionManager = ActionManager.getInstance();
      AnAction uploadAction = actionManager.getAction("target-platformio-upload");
      if (uploadAction != null) {
        actionManager.tryToExecute(uploadAction, null, null, null, true);
      }
      else {
        notifyUploadUnavailable(getProject());
      }
      throw new CantRunException.CustomProcessedCantRunException();
    });
  }

  @Override
  public @NotNull CidrDebugProcess createDebugProcess(@NotNull CommandLineState commandLineState, @NotNull XDebugSession xDebugSession)
    throws ExecutionException {
    PlatformioUsagesCollector.DEBUG_START_EVENT_ID.log(getProject());

    CPPToolchains.Toolchain toolchain = TrivialNativeToolchain.Companion.forDebugger(CPPDebugger.customGdb("pio"), OSType.getCurrent());
    DebuggerDriverConfiguration debuggerDriverConfiguration =
      new CLionGDBDriverConfiguration(getProject(), toolchain) {
        @Override
        public @NotNull
        GeneralCommandLine createDriverCommandLine(@NotNull DebuggerDriver driver, @NotNull ArchitectureType architectureType)
          throws ExecutionException {
          return new PlatformioCliBuilder(false, getProject(), true, true)
            .withParams("debug", "--interface=gdb", "--interpreter=mi2", "-x", ".pioinit", "--iex", "set mi-async on")
            .withGdbHomeCompatibility()
            .withRedirectErrorStream(true)
            .build();
        }
      };
    final @SystemIndependent String projectPath = getProject().getBasePath();
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

    String defaultSvdLocation = getProject().getService(PlatformioService.class).getSvdPath();
    return CidrCoroutineHelper.runOnEDT(
      () -> new CidrDebugProcess(parameters, xDebugSession, commandLineState.getConsoleBuilder(),
                                 consoleCopyFilter) {

        @Override
        public @NotNull
        XDebugTabLayouter createTabLayouter() {
          CidrDebugProcess gdbDebugProcess = this;
          XDebugTabLayouter innerLayouter = super.createTabLayouter();
          return new XDebugTabLayouter() {
            @Override
            public @NotNull Content registerConsoleContent(@NotNull RunnerLayoutUi ui, @NotNull ExecutionConsole console) {
              return innerLayouter.registerConsoleContent(ui, console);
            }

            @Override
            public void registerAdditionalContent(@NotNull RunnerLayoutUi ui) {
              innerLayouter.registerAdditionalContent(ui);
              SvdPanel.registerPeripheralTab(gdbDebugProcess, ui, defaultSvdLocation);
            }
          };
        }

        @Override
        public @NotNull ConsoleView createConsole() {
          ConsoleView console = super.createConsole();
          console.addMessageFilter(new CidrPathConsoleFilter(getProject(), null, Path.of(projectPath)));
          return console;
        }

        @Override
        protected @NotNull
        DebuggerDriver.Inferior doLoadTarget(@NotNull DebuggerDriver driver) throws ExecutionException {

          DebuggerDriver.Inferior tempInferior = driver.loadForRemote("", null, null, Collections.emptyList());
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
      });
  }

  @SuppressWarnings("RedundantThrows")
  @Override
  protected void collectAdditionalActions(@NotNull CommandLineState state,
                                          @NotNull ProcessHandler processHandler,
                                          @NotNull ExecutionConsole console,
                                          @NotNull List<? super AnAction> actions) throws ExecutionException {
    super.collectAdditionalActions(state, processHandler, console, actions);
    McuResetAction.addResetMcuAction(actions, processHandler, "pio_reset_halt_target");
  }
}
