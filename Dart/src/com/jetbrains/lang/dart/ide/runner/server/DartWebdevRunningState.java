// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.net.NetUtils;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.coverage.DartCoverageProgramRunner;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsView;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.DartExecutionHelper;
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.webdev.DartWebdevConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.webdev.DartWebdevParameters;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import sun.tools.jar.CommandLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DartWebdevRunningState extends CommandLineState {
  @NotNull protected final DartWebdevParameters myDartWebdevParameters;
  private int myObservatoryPort = -1;
  private final Collection<Consumer<String>> myObservatoryUrlConsumers = new ArrayList<>();

  public DartWebdevRunningState(final @NotNull ExecutionEnvironment env) throws ExecutionException {
    super(env);
    myDartWebdevParameters = ((DartWebdevConfiguration)env.getRunProfile()).getParameters().clone();

    final Project project = env.getProject();
    try {
      myDartWebdevParameters.check(project);
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e);
    }

    final TextConsoleBuilder builder = getConsoleBuilder();
    if (builder instanceof TextConsoleBuilderImpl) {
      ((TextConsoleBuilderImpl)builder).setUsePredefinedMessageFilter(false);
    }

    // TODO(jwren) Do we want to add filters to the builder, see DartCommandLineRunningState
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
    return actions.toArray(AnAction.EMPTY_ARRAY);
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
    final GeneralCommandLine commandLine = createCommandLine();

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

    // TODO(jwren) Convert connecting to some observatory port to the new protocol output by the VM and the daemon protocol.
    // TODO(jwren) Parse the JSON passed from the daemon process to display in the Run panel.

    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(@NotNull final ProcessEvent event, @NotNull final Key outputType) {
        final String prefix = "Serving `web` on http://";
        final String text = event.getText().trim();
        if (text.startsWith(prefix)) {
          processHandler.removeProcessListener(this);
          final String url = "http://" + text.substring(prefix.length());
          for (Consumer<String> consumer : myObservatoryUrlConsumers) {
            consumer.consume(url);
          }
          myObservatoryPort = -1;
        }
      }
    });


    // TODO(jwren) Check for and display any analysis errors when we launch a Dart webdev app.
    // see DartCommandLineRunningState

    ProcessTerminatedListener.attach(processHandler, getEnvironment().getProject());
    return processHandler;
  }

  private GeneralCommandLine createCommandLine() throws ExecutionException {
    final DartSdk sdk = DartSdk.getDartSdk(getEnvironment().getProject());
    if (sdk == null) {
      throw new ExecutionException(DartBundle.message("dart.sdk.is.not.configured"));
    }

    // Call webdev:
    // <dart_sdk_path>/pub global run webdev daemon

    final GeneralCommandLine commandLine = new GeneralCommandLine()
      .withWorkDirectory(myDartWebdevParameters.computeProcessWorkingDirectory(getEnvironment().getProject()));
    commandLine.setCharset(StandardCharsets.UTF_8);
    commandLine.setExePath(FileUtil.toSystemDependentName(DartSdkUtil.getPubPath(sdk)));
    commandLine.addParameters("global", "run", "webdev", "daemon");
    return commandLine;
  }

  public int getObservatoryPort() {
    return myObservatoryPort;
  }
}
