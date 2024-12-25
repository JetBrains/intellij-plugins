// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.COMMAND_EMULATE;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.COMMAND_RUN;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.PLATFORM_ANDROID;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.PLATFORM_IOS;

public abstract class PhoneGapTargets {
  private static final Logger LOGGER = Logger.getInstance(PhoneGapTargets.class);

  public static @Nullable PhoneGapTargets createTargetsList(@NotNull Project project, @Nullable String platform) {
    if (PLATFORM_ANDROID.equals(platform)) {
      return new PhoneGapAndroidTargets(project);
    }
    if (PLATFORM_IOS.equals(platform)) {
      return new PhoneGapIosTargets(project);
    }

    return null;
  }

  public static @NotNull List<String> listTargets(@Nullable PhoneGapTargets phoneGapTargets, @Nullable String command) {
    if (phoneGapTargets == null) return ContainerUtil.emptyList();

    if (COMMAND_EMULATE.equals(command)) {
      return phoneGapTargets.listVirtualDevices();
    }

    if (COMMAND_RUN.equals(command)) {
      return phoneGapTargets.listDevices();
    }

    return ContainerUtil.emptyList();
  }


  protected final @NotNull Project myProject;

  private final NotNullLazyValue<List<String>> myVirtualDevices = NotNullLazyValue.lazy(() -> {
    return listVirtualDevicesNonCached();
  });

  private final NotNullLazyValue<List<String>> myDevices = NotNullLazyValue.lazy(() -> {
    return listDevicesNonCached();
  });

  public PhoneGapTargets(@NotNull Project project) {
    myProject = project;
  }

  public @NotNull List<String> listDevices() {
    return myDevices.getValue();
  }

  public @NotNull List<String> listVirtualDevices() {
    return myVirtualDevices.getValue();
  }

  protected abstract @NotNull List<String> listDevicesNonCached();

  protected abstract @NotNull List<String> listVirtualDevicesNonCached();

  protected List<String> list(String executableName,
                              Function<String, String> parser,
                              boolean errorOut,
                              String... params) {
    List<String> result = new ArrayList<>();

    File deployExecutable = PathEnvironmentVariableUtil.findInPath(executableName);
    if (deployExecutable == null) return result;

    try {
      GeneralCommandLine line = new GeneralCommandLine(deployExecutable.getAbsolutePath());
      line.addParameters(params);
      ProcessOutput output = ExecUtil.execAndGetOutput(line);
      List<String> lines = null;
      if (errorOut) {
        if (!StringUtil.isEmpty(output.getStderr())) {
          lines = output.getStderrLines();
        }
      }

      if (lines == null) {
        lines = output.getStdoutLines();
      }

      if (output.getExitCode() != 0) return result;

      for (String value : lines) {
        ContainerUtil.addIfNotNull(result, parser.fun(value));
      }
    }
    catch (ExecutionException e) {
      LOGGER.debug(e.getMessage(), e);
    }

    return result;
  }

  public abstract @NotNull String platform();
}
