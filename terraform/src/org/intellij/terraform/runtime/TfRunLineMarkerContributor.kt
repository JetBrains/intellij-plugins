// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.RunManager
import com.intellij.execution.impl.EditConfigurationsDialog
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.ui.IconManager
import org.intellij.terraform.config.actions.TFInitAction
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.isTerraformFile
import org.jetbrains.annotations.Nls
import java.util.function.Function
import javax.swing.Icon

class TfRunLineMarkerContributor : RunLineMarkerContributor(), DumbAware {
  override fun getInfo(leaf: PsiElement): Info? {
    val psiFile = leaf.containingFile
    if (!isTerraformFile(psiFile)) {
      return null
    }

    val identifier = leaf.parent ?: return null
    val block = identifier.parent as? HCLBlock ?: return null

    val firstHCLBlock = psiFile.children.firstOrNull { it is HCLBlock } ?: return null
    if (block !== firstHCLBlock) {
      return null
    }
    if (block.nameIdentifier !== identifier) {
      return null
    }

    val icon: Icon
    val tooltipProvider: Function<PsiElement, String>
    if (TFInitAction.isInitRequired(leaf.project, leaf.containingFile.virtualFile)) {
      icon = IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark)
      tooltipProvider = Function<PsiElement, String> { HCLBundle.message("not.initialized.inspection.error.message") }
    }
    else {
      icon = AllIcons.RunConfigurations.TestState.Run
      tooltipProvider = Function<PsiElement, String> { HCLBundle.message("terraform.run.text") }
    }

    return Info(icon, computeActions(block), tooltipProvider)
  }

  private fun computeActions(block: HCLBlock): Array<AnAction> {
    val project = block.project
    val rootModule = getRootModule(block)
    val templateConfigNames = getTemplateConfigNames(rootModule.name)

    val actions: MutableList<AnAction> = mutableListOf()
    actions.addAll(templateConfigNames.map { TfRunTemplateConfigAction(it, rootModule.path) })

    val runManager = RunManager.getInstance(project)
    val existingConfigs = runManager.allSettings.filter {
      val configuration = it.configuration as? TerraformRunConfiguration
      configuration != null && configuration.workingDirectory == rootModule.path && configuration.name !in templateConfigNames
    }
    actions.addAll(existingConfigs.map { TfRunExistingConfigAction(it) })

    actions.add(Separator())
    actions.add(getEditConfigurationAction(project))

    return actions.toTypedArray()
  }

  private fun getTemplateConfigNames(moduleName: @NlsSafe String): List<@Nls String> = TfMainCommand.entries
    .filter { it != TfMainCommand.CUSTOM }
    .map { "${it.title} $moduleName".trim() }

  private fun getRootModule(block: HCLBlock): RootModulePath {
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

  private fun getEditConfigurationAction(project: Project): AnAction = object : AnAction() {
    init {
      templatePresentation.text = HCLBundle.message("terraform.edit.configurations.action.text")
    }

    override fun actionPerformed(e: AnActionEvent) {
      EditConfigurationsDialog(project, tfRunConfigurationType().baseFactory).show()
    }
  }
}

private data class RootModulePath(val path: String, @NlsSafe val name: String)