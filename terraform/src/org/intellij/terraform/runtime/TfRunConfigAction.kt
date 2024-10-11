// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.NlsSafe
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.hcl.psi.HCLBlock
import org.jetbrains.annotations.Nls

internal class TfRunConfigAction(private val block: HCLBlock, private val tfCommand: TfMainCommand) : AnAction() {
  private val project = block.project

  private val rootModule: RootModulePath by lazy {
    val moduleRoot = block.getTerraformModule().moduleRoot

    if (moduleRoot.isDirectory) {
      val virtualFile = moduleRoot.virtualFile
      RootModulePath(virtualFile.path, virtualFile.name)
    }
    else {
      RootModulePath(project.basePath ?: "", project.name)
    }
  }

  private val configurationName: @Nls String = "${tfCommand.title} ${rootModule.name}"

  init {
    templatePresentation.text = configurationName
  }

  override fun actionPerformed(e: AnActionEvent) {
    val runManager = RunManager.getInstance(project)
    val configType = tfRunConfigurationType()

    val existingConfiguration = runManager.findConfigurationByTypeAndName(configType, configurationName)
    val settings = existingConfiguration ?: runManager.createConfiguration(configurationName, configType.createFactory(tfCommand))
    configureSettings(settings)

    runManager.addConfiguration(settings)
    runManager.selectedConfiguration = settings
    ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
  }

  private fun configureSettings(settings: RunnerAndConfigurationSettings) {
    settings.isTemporary = true
    (settings.configuration as? TerraformRunConfiguration)?.let {
      it.workingDirectory = rootModule.path
    }
  }
}

private data class RootModulePath(val path: String, @NlsSafe val name: String)