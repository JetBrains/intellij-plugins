package com.jetbrains.cidr.cpp.embedded.platformio.terminal

import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable.Companion.pioBinFolder
import org.jetbrains.plugins.terminal.startup.MutableShellExecOptions
import org.jetbrains.plugins.terminal.startup.ShellExecOptionsCustomizer

class PlatformioShellExecOptionCustomizer : ShellExecOptionsCustomizer {
  override fun customizeExecOptions(project: Project, shellExecOptions: MutableShellExecOptions) {
    shellExecOptions.appendEntryToPATH(pioBinFolder())
  }
}
