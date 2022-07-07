package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.Platform;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer;

import java.io.File;
import java.util.Map;

public class PlatformioLocalTerminalCustomizer extends LocalTerminalCustomizer {

  private static final String ENV_PATH = "PATH";

  @Override
  public String[] customizeCommandAndEnvironment(@NotNull Project project, @Nullable String workingDirectory, String[] command, @NotNull Map<String, String> envs) {
    String pioLocation = PlatformioConfigurable.getPioLocation();
    if (!StringUtil.isEmptyOrSpaces(pioLocation)) {
      File platformioLocation = new File(pioLocation);
      if (platformioLocation.isFile() && platformioLocation.canExecute()) {
        String parentPath = platformioLocation.getParent();
        String path = envs.getOrDefault(ENV_PATH, "");
        if (
          PathEnvironmentVariableUtil.getPathDirs(path)
            .stream().noneMatch(s -> FileUtil.pathsEqual(s, parentPath))) {
          envs.replace(ENV_PATH, path + Platform.current().pathSeparator + parentPath);
        }
      }
    }
    return super.customizeCommandAndEnvironment(project, command, envs);
  }
}
