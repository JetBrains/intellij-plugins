package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.Platform
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable.Companion.pioBinFolder
import com.jetbrains.cidr.cpp.embedded.platformio.project.ENV_PATH
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer
import java.nio.file.Path

class PlatformioLocalTerminalCustomizer : LocalTerminalCustomizer() {
  override fun customizeCommandAndEnvironment(project: Project,
                                              workingDirectory: String?,
                                              command: Array<String>,
                                              envs: MutableMap<String, String>): Array<String> {
    val pioLocation = pioBinFolder()
    if (pioLocation != null) {
      val path = envs.getOrDefault(ENV_PATH, "")
      if (PathEnvironmentVariableUtil.getPathDirs(path).none { pioLocation == Path.of(it) }) {
        envs.replace(ENV_PATH, path + Platform.current().pathSeparator + pioLocation.toAbsolutePath())
      }
    }
    return super.customizeCommandAndEnvironment(project, workingDirectory, command, envs)
  }
}
