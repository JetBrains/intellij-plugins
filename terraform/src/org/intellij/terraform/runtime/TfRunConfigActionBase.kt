// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.RunDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.jetbrains.annotations.Nls

internal abstract class TfRunConfigActionBase(private val block: HCLBlock, private val tfCommand: TfMainCommand) : AnAction() {
  protected val project: Project = block.project

  protected val runManager: RunManager
    get() = RunManager.getInstance(project)

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

  protected val configurationName: @Nls String
    get() = "${tfCommand.title} ${rootModule.name}".trim()

  protected fun createAndConfigureSettings(): RunnerAndConfigurationSettings {
    val configType = tfRunConfigurationType()

    val configurationName = runManager.suggestUniqueName(configurationName, configType)
    val settings = runManager.createConfiguration(configurationName, configType.createFactory(tfCommand))

    settings.isTemporary = true
    (settings.configuration as? TerraformRunConfiguration)?.let {
      it.workingDirectory = rootModule.path
    }
    return settings
  }
}

internal class TfRunConfigurationAction(block: HCLBlock, tfCommand: TfMainCommand) : TfRunConfigActionBase(block, tfCommand) {
  init {
    templatePresentation.text = configurationName
  }

  override fun actionPerformed(e: AnActionEvent) {
    val existingConfiguration = runManager.findConfigurationByTypeAndName(tfRunConfigurationType(), configurationName)
    val settings = existingConfiguration ?: createAndConfigureSettings()

    runManager.addConfiguration(settings)
    runManager.selectedConfiguration = settings
    ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
  }
}

internal class TfEditConfigurationsAction(block: HCLBlock) : TfRunConfigActionBase(block, TfMainCommand.NONE) {
  init {
    templatePresentation.text = HCLBundle.message("terraform.edit.configurations.action.text")
  }

  override fun actionPerformed(e: AnActionEvent) {
    val selectedConfiguration = runManager.selectedConfiguration

    val settings = if (selectedConfiguration?.configuration is TerraformRunConfiguration)
      selectedConfiguration
    else createAndConfigureSettings()

    RunDialog.editConfiguration(project, settings, HCLBundle.message("terraform.edit.configuration.dialog.title", settings.name))
  }
}

private data class RootModulePath(val path: String, @NlsSafe val name: String)