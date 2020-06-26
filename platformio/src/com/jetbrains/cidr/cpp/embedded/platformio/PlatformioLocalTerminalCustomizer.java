package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.Platform;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer;

import java.io.File;
import java.util.Map;

public class PlatformioLocalTerminalCustomizer extends LocalTerminalCustomizer {

  private static final String ENV_PATH = "PATH";

  @Override
  public String[] customizeCommandAndEnvironment(Project project, String[] command, Map<String, String> envs) {
    File platformioLocation = new File(PlatformioConfigurable.getPioLocation());
    if (platformioLocation.isFile() && platformioLocation.canExecute()) {
      String parentPath = platformioLocation.getParent();
      String path = envs.getOrDefault(ENV_PATH, "");
      if (
        PathEnvironmentVariableUtil.getPathDirs(path)
          .stream().noneMatch(s -> FileUtil.pathsEqual(s, parentPath))) {
        envs.replace(ENV_PATH, path + Platform.current().pathSeparator + parentPath);
      }
    }
    return super.customizeCommandAndEnvironment(project, command, envs);
  }
}
