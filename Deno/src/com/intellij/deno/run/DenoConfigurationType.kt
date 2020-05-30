package com.intellij.deno.run

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import icons.JavaScriptDebuggerIcons

class DenoConfigurationType :
  SimpleConfigurationType("DenoConfigurationType", "Deno", "Deno", NotNullLazyValue.createConstantValue(
    JavaScriptDebuggerIcons.JavaScript_debug_configuration)), DumbAware {

  override fun createTemplateConfiguration(project: Project): RunConfiguration {
    return DenoRunConfiguration(project, this, "Deno")
  }
}