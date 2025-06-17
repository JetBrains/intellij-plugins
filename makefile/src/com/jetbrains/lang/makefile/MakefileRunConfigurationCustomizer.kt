package com.jetbrains.lang.makefile

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.util.concurrency.annotations.RequiresEdt

interface MakefileRunConfigurationCustomizer {
  companion object {
    val EP_NAME: ExtensionPointName<MakefileRunConfigurationCustomizer> = ExtensionPointName<MakefileRunConfigurationCustomizer>("com.intellij.makefile.runConfigurationCustomizer")
  }

  @RequiresEdt
  fun customizeCommandAndEnvironment(
    project: Project, command: Array<@NlsSafe String>,
    environment: MutableMap<@NlsSafe String, @NlsSafe String>,
  ): Array<@NlsSafe String>
}