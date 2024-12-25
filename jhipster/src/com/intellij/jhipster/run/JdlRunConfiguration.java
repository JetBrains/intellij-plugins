// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.intellij.util.PathUtil.toSystemDependentName;

final class JdlRunConfiguration extends RunConfigurationBase<JdlRunConfigurationOptions> {

  JdlRunConfiguration(@NotNull Project project, @Nullable ConfigurationFactory factory, @Nullable String name) {
    super(project, factory, name);
  }

  @Override
  protected @NotNull JdlRunConfigurationOptions getOptions() {
    return (JdlRunConfigurationOptions)super.getOptions();
  }

  @Override
  public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new JdlRunSettingsEditor(getProject());
  }

  @Override
  public @NotNull RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
    return new CommandLineState(executionEnvironment) {
      @Override
      protected @NotNull ProcessHandler startProcess() throws ExecutionException {
        var options = getOptions();

        var jdlFile = new File(options.getJdlLocation());
        var commandLine = new GeneralCommandLine(getJHipsterExecutable(options), "jdl", jdlFile.getAbsolutePath());
        commandLine.setCharset(getConsoleCharset());
        commandLine.setWorkDirectory(getWorkDirectory(jdlFile, options));

        var projectSdk = ProjectRootManager.getInstance(getProject()).getProjectSdk();
        if (projectSdk != null
            && projectSdk.getSdkType() instanceof JavaSdkType
            && projectSdk.getHomePath() != null) {
          var jdkIdePath = projectSdk.getHomePath();
          var jdkHome = toSystemDependentName(jdkIdePath);
          commandLine.withEnvironment("JAVA_HOME", jdkHome);

          // try to upgrade PATH
          var path = System.getenv("PATH");
          var newPath = path + File.pathSeparatorChar + jdkHome + File.separator + "bin";
          commandLine.withEnvironment("PATH", newPath);
        }

        var processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine);
        processHandler.addProcessListener(new ProcessAdapter() {
          @Override
          public void processTerminated(@NotNull ProcessEvent event) {
            // we generated something, let's check
            LocalFileSystem.getInstance().refresh(true);
          }
        });

        ProcessTerminatedListener.attach(processHandler);
        return processHandler;
      }
    };
  }

  private static File getWorkDirectory(File jdlFile, JdlRunConfigurationOptions options) {
    if (StringUtil.isEmptyOrSpaces(options.getOutputLocation())) {
      return jdlFile.getParentFile();
    }
    else {
      var outputDir = new File(options.getOutputLocation());
      if (!outputDir.exists()) {
        //noinspection ResultOfMethodCallIgnored
        outputDir.mkdir();
      }
      return outputDir;
    }
  }

  private static @NotNull String getJHipsterExecutable(JdlRunConfigurationOptions options) {
    if (StringUtil.isEmptyOrSpaces(options.getJHipsterLocation())) {
      if (SystemInfo.isWindows) {
        return "jhipster.cmd";
      }

      return "jhipster";
    }
    else {
      return options.getJHipsterLocation();
    }
  }

  private static @NotNull Charset getConsoleCharset() {
    if (SystemInfo.isWin10OrNewer) {
      // enforce UTF-8
      return StandardCharsets.UTF_8;
    }
    return CharsetToolkit.getDefaultSystemCharset();
  }
}
