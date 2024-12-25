// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.webdev;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.actions.DartPubActionBase;
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter;
import com.jetbrains.lang.dart.pubServer.DartWebdev;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class DartWebdevRunningState extends CommandLineState {

  protected @NotNull DartWebdevParameters myDartWebdevParameters;

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

    try {
      builder.addFilter(new DartRelativePathsConsoleFilter(project, myDartWebdevParameters.getWorkingDirectory(project).getPath()));
    }
    catch (RuntimeConfigurationError ignore) {/* can't happen because checked in myDartWebdevParameters.check(project) */}

    DartWebdevConsoleView.install(env.getProject(), this);
  }

  @Override
  protected @NotNull ProcessHandler startProcess() throws ExecutionException {
    if (!DartWebdev.INSTANCE.getActivated()) {
      DartWebdev.INSTANCE.ensureWebdevActivated(getEnvironment().getProject());
    }

    final GeneralCommandLine commandLine = createCommandLine();

    final OSProcessHandler processHandler = new ColoredProcessHandler(commandLine);

    ProcessTerminatedListener.attach(processHandler, getEnvironment().getProject());
    return processHandler;
  }

  private GeneralCommandLine createCommandLine() throws ExecutionException {
    final Project project = getEnvironment().getProject();
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) {
      throw new ExecutionException(DartBundle.message("dart.sdk.is.not.configured"));
    }

    VirtualFile workingDir;
    try {
      workingDir = myDartWebdevParameters.getWorkingDirectory(project);
    }
    catch (RuntimeConfigurationError error) {
      throw new ExecutionException(DartBundle.message("html.file.not.within.dart.project"));
    }

    final GeneralCommandLine commandLine = new GeneralCommandLine().withWorkDirectory(workingDir.getPath());
    commandLine.setCharset(StandardCharsets.UTF_8);
    DartPubActionBase.setupPubExePath(commandLine, sdk);
    commandLine.addParameters("global", "run", "webdev", "daemon");

    // Compute the relative html file path and first directory name in the relative html file path
    final String htmlFilePath = myDartWebdevParameters.getHtmlFilePath();

    // workingDir  =  "/.../webdev/example"
    // htmlFilePath = "/.../webdev/example/web/index.html"
    final String htmlFilePathRelativeFromWorkingDir =
      htmlFilePath.startsWith(workingDir.getPath() + "/") ? htmlFilePath.substring(workingDir.getPath().length() + 1) : null;

    // htmlFilePathRelativeFromWorkingDir = "web/index.html"
    final int firstSlashIndex = htmlFilePathRelativeFromWorkingDir == null ? -1 : htmlFilePathRelativeFromWorkingDir.indexOf('/');
    final String firstDirName = firstSlashIndex == -1 ? null : htmlFilePathRelativeFromWorkingDir.substring(0, firstSlashIndex);

    // firstDirName = "web"
    // Append the "web:<port>" and "--launch-app=web/index.html"
    if (myDartWebdevParameters.getWebdevPort() != -1 && StringUtil.isNotEmpty(firstDirName)) {
      commandLine.addParameter(firstDirName + ":" + myDartWebdevParameters.getWebdevPort());
    }
    if (htmlFilePathRelativeFromWorkingDir != null) {
      commandLine.addParameter("--launch-app=" + htmlFilePathRelativeFromWorkingDir);
    }

    return commandLine;
  }
}
