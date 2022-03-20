package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.Platform;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer;

import java.io.File;
import java.util.Map;

import static com.intellij.execution.configurations.PathEnvironmentVariableUtil.getPathDirs;
import static com.intellij.openapi.util.io.FileUtil.pathsEqual;
import static com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces;
import static com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable.getPioLocation;

public class PlatformioLocalTerminalCustomizer extends LocalTerminalCustomizer {

  private static final String ENV_PATH = "PATH";

  @Override
  public String[] customizeCommandAndEnvironment(
          final @NotNull Project project,
          final @NotNull String[] command,
          final @NotNull Map<String, String> envs) {
    final var pioLocation = getPioLocation();
    if (!isEmptyOrSpaces(pioLocation)) {
      final var platformioLocation = new File(pioLocation);
      if (platformioLocation.isFile() && platformioLocation.canExecute()) {
        final var parentPath = platformioLocation.getParent();
        final var path = envs.getOrDefault(ENV_PATH, "");
        if (getPathDirs(path).stream().noneMatch(s -> pathsEqual(s, parentPath))) {
          envs.replace(ENV_PATH, path + Platform.current().pathSeparator + parentPath);
        }
      }
    }
    return super.customizeCommandAndEnvironment(project, command, envs);
  }
}
