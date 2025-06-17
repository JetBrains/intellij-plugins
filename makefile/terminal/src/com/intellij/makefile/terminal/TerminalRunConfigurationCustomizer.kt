package com.intellij.makefile.terminal

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.jetbrains.lang.makefile.MakefileRunConfigurationCustomizer
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer

class TerminalRunConfigurationCustomizer : MakefileRunConfigurationCustomizer {
  @RequiresEdt
  override fun customizeCommandAndEnvironment(project: Project, command: Array<@NlsSafe String>, environment: MutableMap<@NlsSafe String, @NlsSafe String>): Array<@NlsSafe String> {
    /*
     * The result of last successful invocation, needed for the fail-safe scenario.
     */
    var lastCommand = command

    return try {
      LocalTerminalCustomizer.EP_NAME.extensionList.fold(command) { acc, customizer ->
        try {
          customizer.customizeCommandAndEnvironment(project, null, acc, environment)
        }
        catch (_: Throwable) {
          acc
        }.also {
          /*
           * Remember the result of last successful invocation.
           */
          lastCommand = it
        }
      }
    }
    catch (_: Throwable) {
      // optional dependency
      lastCommand
    }
  }
}