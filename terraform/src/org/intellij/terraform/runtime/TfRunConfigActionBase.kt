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
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.isTerraformFile
import org.jetbrains.annotations.Nls

internal sealed class TfRunConfigActionBase : AnAction(), DumbAware {
  abstract val command: TfMainCommand

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = CommonDataKeys.PROJECT.getData(e.dataContext)
    val psiFile = CommonDataKeys.PSI_FILE.getData(e.dataContext)

    val hclBlock = psiFile?.children?.firstOrNull { it is HCLBlock } as? HCLBlock
    e.presentation.isEnabledAndVisible = project != null && isTerraformFile(psiFile) && hclBlock != null

    hclBlock?.let {
      e.presentation.text = getConfigurationName(getRootModule(hclBlock))
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = CommonDataKeys.PROJECT.getData(e.dataContext) ?: return
    val psiFile = CommonDataKeys.PSI_FILE.getData(e.dataContext) ?: return

    if (!isTerraformFile(psiFile)) return
    val hclBlock = psiFile.children.firstOrNull { it is HCLBlock } as? HCLBlock ?: return

    val rootModule = getRootModule(hclBlock)
    val configurationName = getConfigurationName(rootModule)

    val runManager: RunManager = RunManager.getInstance(project)
    val existingConfiguration = runManager.findConfigurationByTypeAndName(tfRunConfigurationType(), configurationName)
    val settings = existingConfiguration ?: createAndConfigureSettings(runManager, configurationName, rootModule.path)

    runManager.selectedConfiguration = settings
    ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
  }

  private fun createAndConfigureSettings(runManager: RunManager, suggestName: String, modulePath: String): RunnerAndConfigurationSettings {
    val configurationName = runManager.suggestUniqueName(suggestName, tfRunConfigurationType())
    val settings = runManager.createConfiguration(configurationName, getConfigurationFactory())

    settings.isTemporary = true
    (settings.configuration as? TerraformRunConfiguration)?.let {
      it.workingDirectory = modulePath
    }

    runManager.addConfiguration(settings)
    return settings
  }

  private fun getConfigurationName(rootModule: RootModulePath): @Nls String = "${command.title} ${rootModule.name}"

  private fun getConfigurationFactory(): ConfigurationFactory {
    val configurationType = tfRunConfigurationType()
    return when (command) {
      TfMainCommand.INIT -> configurationType.initFactory
      TfMainCommand.VALIDATE -> configurationType.validateFactory
      TfMainCommand.PLAN -> configurationType.planFactory
      TfMainCommand.APPLY -> configurationType.applyFactory
      TfMainCommand.DESTROY -> configurationType.destroyFactory
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

internal class InitAction : TfRunConfigActionBase() {
  override val command: TfMainCommand = TfMainCommand.INIT
}

internal class ValidateAction : TfRunConfigActionBase() {
  override val command: TfMainCommand = TfMainCommand.VALIDATE
}

internal class PlanAction : TfRunConfigActionBase() {
  override val command: TfMainCommand = TfMainCommand.PLAN
}

internal class ApplyAction : TfRunConfigActionBase() {
  override val command: TfMainCommand = TfMainCommand.APPLY
}

internal class DestroyAction : TfRunConfigActionBase() {
  override val command: TfMainCommand = TfMainCommand.DESTROY
}
