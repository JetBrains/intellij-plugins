// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.annotations.Nls

internal class TfRunTemplateConfigAction(private val configurationName: @Nls String, private val directoryPath: String) : AnAction() {

  init {
    templatePresentation.text = configurationName
  }

  private fun createAndConfigureSettings(runManager: RunManager): RunnerAndConfigurationSettings {
    val configType = tfRunConfigurationType()

    val configurationName = runManager.suggestUniqueName(configurationName, configType)
    val settings = runManager.createConfiguration(configurationName, getConfigurationFactory(configurationName, configType))

    settings.isTemporary = true
    (settings.configuration as? TerraformRunConfiguration)?.let {
      it.workingDirectory = directoryPath
    }

    runManager.addConfiguration(settings)
    return settings
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val runManager: RunManager = RunManager.getInstance(project)

    val existingConfiguration = runManager.findConfigurationByTypeAndName(tfRunConfigurationType(), configurationName)
    val settings = existingConfiguration ?: createAndConfigureSettings(runManager)
    runManager.selectedConfiguration = settings
    ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
  }

  private fun getConfigurationFactory(configurationName: String, configType: TfConfigurationType): ConfigurationFactory =
    when (configurationName.split(' ').firstOrNull()) {
      "Init" -> configType.initFactory
      "Validate" -> configType.validateFactory
      "Plan" -> configType.planFactory
      "Apply" -> configType.applyFactory
      "Destroy" -> configType.destroyFactory
      else -> configType.baseFactory
    }
}

