package com.intellij.openRewrite.run

import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configuration.RunConfigurationExtensionsManager
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service

@Service
internal class OpenRewriteRunConfigurationExtensionManager
  : RunConfigurationExtensionsManager<RunConfigurationBase<*>, RunConfigurationExtension>(RunConfigurationExtension.EP_NAME) {

  companion object {
    fun getInstance(): OpenRewriteRunConfigurationExtensionManager = service()
  }
}