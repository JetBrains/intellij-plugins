// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.NlsSafe
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.isTerraformCompatiblePsiFile
import org.jetbrains.annotations.Nls

internal sealed class TfRunBaseConfigAction : AnAction(), DumbAware {

  abstract val command: TfCommand

  private var toolType: TfToolType = TfToolType.TERRAFORM

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = CommonDataKeys.PROJECT.getData(e.dataContext)
    val psiFile = CommonDataKeys.PSI_FILE.getData(e.dataContext)

    val hclBlock = psiFile?.children?.firstOrNull { it is HCLBlock } as? HCLBlock
    e.presentation.isEnabledAndVisible = project != null && isTerraformCompatiblePsiFile(psiFile) && hclBlock != null

    hclBlock?.let {
      e.presentation.setText(getConfigurationName(getRootModule(hclBlock)), false)
      toolType = getApplicableToolType(psiFile.virtualFile)
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = CommonDataKeys.PROJECT.getData(e.dataContext) ?: return
    val psiFile = CommonDataKeys.PSI_FILE.getData(e.dataContext) ?: return

    if (!isTerraformCompatiblePsiFile(psiFile)) return
    val hclBlock = psiFile.children.firstOrNull { it is HCLBlock } as? HCLBlock ?: return

    val rootModule = getRootModule(hclBlock)
    val configurationName = getConfigurationName(rootModule)

    val runManager: RunManager = RunManager.getInstance(project)
    val existingConfiguration = runManager.findConfigurationByTypeAndName(tfRunConfigurationType(toolType), configurationName)
    val settings = existingConfiguration ?: createAndConfigureSettings(runManager, configurationName, rootModule.path, toolType)

    runManager.selectedConfiguration = settings
    ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
  }

  private fun createAndConfigureSettings(runManager: RunManager, suggestName: String, modulePath: String, toolType: TfToolType): RunnerAndConfigurationSettings {
    val configurationName = runManager.suggestUniqueName(suggestName, tfRunConfigurationType(toolType))
    val settings = runManager.createConfiguration(configurationName, getConfigurationFactory(toolType))

    settings.isTemporary = true
    (settings.configuration as? TfToolsRunConfigurationBase)?.let {
      it.workingDirectory = modulePath
    }

    runManager.addConfiguration(settings)
    return settings
  }

  internal fun getConfigurationName(rootModule: RootModulePath): @Nls String {
    return "${HCLBundle.message("terraform.run.configuration."+command.command+".name.suffix")} ${rootModule.name}"
  }

  private fun getConfigurationFactory(toolType: TfToolType): ConfigurationFactory {
    val configurationType = tfRunConfigurationType(toolType)
    return when (command) {
      TfCommand.INIT -> configurationType.initFactory
      TfCommand.VALIDATE -> configurationType.validateFactory
      TfCommand.PLAN -> configurationType.planFactory
      TfCommand.APPLY -> configurationType.applyFactory
      TfCommand.DESTROY -> configurationType.destroyFactory
      else -> configurationType.baseFactory
    }
  }

  companion object {
    fun getRootModule(block: HCLBlock): RootModulePath {
      val moduleRoot = block.getTerraformModule().moduleRoot

      return if (moduleRoot.isDirectory) {
        val virtualFile = moduleRoot.virtualFile
        RootModulePath(virtualFile.path, virtualFile.name)
      }
      else {
        val project = block.project
        RootModulePath(project.basePath ?: "", project.name)
      }
    }
  }
}

internal data class RootModulePath(val path: String, @NlsSafe val name: String)

internal class InitAction : TfRunBaseConfigAction() {
  override val command = TfCommand.INIT
}

internal class ValidateAction : TfRunBaseConfigAction() {
  override val command = TfCommand.VALIDATE
}

internal class PlanAction : TfRunBaseConfigAction() {
  override val command = TfCommand.PLAN
}

internal class ApplyAction : TfRunBaseConfigAction() {
  override val command = TfCommand.APPLY
}

internal class DestroyAction : TfRunBaseConfigAction() {
  override val command = TfCommand.DESTROY
}
