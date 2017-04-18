package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.filters.UrlFilter;
import com.intellij.execution.process.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.net.NetUtils;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.coverage.DartCoverageProgramRunner;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.DartExecutionHelper;
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class DartCommandLineRunningState extends CommandLineState {
  protected final @NotNull DartCommandLineRunnerParameters myRunnerParameters;
  private int myObservatoryPort = -1;
  private Collection<Consumer<String>> myObservatoryUrlConsumers = new ArrayList<>();

  public DartCommandLineRunningState(final @NotNull ExecutionEnvironment env) throws ExecutionException {
    super(env);
    myRunnerParameters = ((DartRunConfiguration)env.getRunProfile()).getRunnerParameters().clone();

    final Project project = env.getProject();
    try {
      myRunnerParameters.check(project);
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e);
    }

    final TextConsoleBuilder builder = getConsoleBuilder();
    if (builder instanceof TextConsoleBuilderImpl) {
      ((TextConsoleBuilderImpl)builder).setUsePredefinedMessageFilter(false);
    }

    try {
      builder.addFilter(new DartConsoleFilter(project, myRunnerParameters.getDartFileOrDirectory()));
      builder.addFilter(new DartRelativePathsConsoleFilter(project, myRunnerParameters.computeProcessWorkingDirectory(project)));
      builder.addFilter(new UrlFilter());
    }
    catch (RuntimeConfigurationError e) { /* can't happen because already checked */}
  }

  public void addObservatoryUrlConsumer(@NotNull final Consumer<String> consumer) {
    myObservatoryUrlConsumers.add(consumer);
  }

  @NotNull
  @Override
  protected AnAction[] createActions(final ConsoleView console, final ProcessHandler processHandler, final Executor executor) {
    // These actions are effectively added only to the Run tool window. For Debug see DartCommandLineDebugProcess.registerAdditionalActions()
    final List<AnAction> actions = new ArrayList<>(Arrays.asList(super.createActions(console, processHandler, executor)));
    addObservatoryActions(actions, processHandler);
    return actions.toArray(new AnAction[actions.size()]);
  }

  protected void addObservatoryActions(List<AnAction> actions, final ProcessHandler processHandler) {
    actions.add(new Separator());

    final OpenDartObservatoryUrlAction openObservatoryAction =
      new OpenDartObservatoryUrlAction(null, () -> !processHandler.isProcessTerminated());
    addObservatoryUrlConsumer(openObservatoryAction::setUrl);

    actions.add(openObservatoryAction);
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    return doStartProcess(null);
  }

  protected ProcessHandler doStartProcess(final @Nullable String overriddenMainFilePath) throws ExecutionException {
    final GeneralCommandLine commandLine = createCommandLine(overriddenMainFilePath);

    // Workaround for "Observatory listening on ..." message that is concatenated (without line break) with the message following it
    final OSProcessHandler processHandler = new ColoredProcessHandler(commandLine) {
      @Override
      public void coloredTextAvailable(@NotNull String text, @NotNull Key attributes) {
        if (text.startsWith(DartConsoleFilter.OBSERVATORY_LISTENING_ON)) {
          text += "\n";
        }
        super.coloredTextAvailable(text, attributes);
      }
    };

    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(final ProcessEvent event, final Key outputType) {
        final String prefix = DartConsoleFilter.OBSERVATORY_LISTENING_ON + "http://";
        final String text = event.getText().trim();
        if (text.startsWith(prefix)) {
          processHandler.removeProcessListener(this);
          final String url = "http://" + text.substring(prefix.length());
          for (Consumer<String> consumer : myObservatoryUrlConsumers) {
            consumer.consume(url);
          }
        }
      }
    });

    // Check for and display any analysis errors when we launch a Dart app.
    final Project project = getEnvironment().getProject();
    try {
      final DartRunConfiguration dartRunConfiguration = (DartRunConfiguration)getEnvironment().getRunProfile();
      final VirtualFile launchFile = dartRunConfiguration.getRunnerParameters().getDartFileOrDirectory();

      if (getEnvironment().getRunnerAndConfigurationSettings() == null) {
        DartExecutionHelper.getInstance(project).displayIssues(launchFile);
      }
      else {
        String launchTitle = "Error launching " + getEnvironment().getRunnerAndConfigurationSettings().getName();
        Icon icon = getEnvironment().getRunnerAndConfigurationSettings().getConfiguration().getIcon();
        DartExecutionHelper.getInstance(project).displayIssues(launchFile, launchTitle, icon);
      }
    }
    catch (RuntimeConfigurationError error) {
      DartExecutionHelper.getInstance(project).clearIssueNotifications();
    }

    ProcessTerminatedListener.attach(processHandler, getEnvironment().getProject());

    return processHandler;
  }

  private GeneralCommandLine createCommandLine(@Nullable final String overriddenMainFilePath) throws ExecutionException {
    final DartSdk sdk = DartSdk.getDartSdk(getEnvironment().getProject());
    if (sdk == null) {
      throw new ExecutionException(DartBundle.message("dart.sdk.is.not.configured"));
    }

    final GeneralCommandLine commandLine = new GeneralCommandLine()
      .withWorkDirectory(myRunnerParameters.computeProcessWorkingDirectory(getEnvironment().getProject()));
    commandLine.setCharset(CharsetToolkit.UTF8_CHARSET);
    commandLine.setExePath(FileUtil.toSystemDependentName(DartSdkUtil.getDartExePath(sdk)));
    commandLine.getEnvironment().putAll(myRunnerParameters.getEnvs());
    commandLine
      .withParentEnvironmentType(myRunnerParameters.isIncludeParentEnvs() ? ParentEnvironmentType.CONSOLE : ParentEnvironmentType.NONE);
    setupParameters(commandLine, overriddenMainFilePath);

    return commandLine;
  }

  private void setupParameters(@NotNull final GeneralCommandLine commandLine,
                               @Nullable final String overriddenMainFilePath) throws ExecutionException {
    int customObservatoryPort = -1;

    final String vmOptions = myRunnerParameters.getVMOptions();
    if (vmOptions != null) {
      final StringTokenizer vmOptionsTokenizer = new CommandLineTokenizer(vmOptions);
      while (vmOptionsTokenizer.hasMoreTokens()) {
        final String vmOption = vmOptionsTokenizer.nextToken();
        commandLine.addParameter(vmOption);

        try {
          if (vmOption.equals("--enable-vm-service") || vmOption.equals("--observe")) {
            customObservatoryPort = 8181; // default port, see https://www.dartlang.org/tools/dart-vm/
          }
          else if (vmOption.startsWith("--enable-vm-service:")) {
            customObservatoryPort = parseIntBeforeSlash(vmOption.substring("--enable-vm-service:".length()));
          }
          else if (vmOption.startsWith("--observe:")) {
            customObservatoryPort = parseIntBeforeSlash(vmOption.substring("--observe:".length()));
          }
        }
        catch (NumberFormatException ignore) {/**/}
      }
    }

    if (myRunnerParameters.isCheckedMode()) {
      commandLine.addParameter(DartiumUtil.CHECKED_MODE_OPTION);
    }

    final VirtualFile dartFile;
    try {
      dartFile = myRunnerParameters.getDartFileOrDirectory();
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e);
    }

    if (DefaultDebugExecutor.EXECUTOR_ID.equals(getEnvironment().getExecutor().getId())) {
      commandLine.addParameter("--pause_isolates_on_start");
    }

    if (customObservatoryPort > 0) {
      myObservatoryPort = customObservatoryPort;
    }
    else {
      try {
        myObservatoryPort = NetUtils.findAvailableSocketPort();
      }
      catch (IOException e) {
        throw new ExecutionException(e);
      }

      commandLine.addParameter("--enable-vm-service:" + myObservatoryPort);

      if (getEnvironment().getRunner() instanceof DartCoverageProgramRunner) {
        commandLine.addParameter("--pause-isolates-on-exit");
      }
    }

    commandLine.addParameter(FileUtil.toSystemDependentName(overriddenMainFilePath == null ? dartFile.getPath() : overriddenMainFilePath));

    final String arguments = myRunnerParameters.getArguments();
    if (arguments != null) {
      StringTokenizer argumentsTokenizer = new CommandLineTokenizer(arguments);
      while (argumentsTokenizer.hasMoreTokens()) {
        commandLine.addParameter(argumentsTokenizer.nextToken());
      }
    }
  }

  private static int parseIntBeforeSlash(@NotNull final String s) throws NumberFormatException {
    // "5858" or "5858/0.0.0.0"
    final int index = s.indexOf('/');
    return Integer.parseInt(index > 0 ? s.substring(0, index) : s);
  }

  public int getObservatoryPort() {
    return myObservatoryPort;
  }
}
