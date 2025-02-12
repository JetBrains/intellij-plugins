// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.RunManager
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.ui.IconManager
import org.intellij.terraform.config.actions.TfInitAction
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.isTerraformCompatiblePsiFile
import java.util.function.Function
import javax.swing.Icon

class TfRunLineMarkerContributor : RunLineMarkerContributor(), DumbAware {
  override fun getInfo(leaf: PsiElement): Info? {
    val psiFile = leaf.containingFile
    if (!isTerraformCompatiblePsiFile(psiFile)) {
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

    val toolType = getApplicableToolType(psiFile.virtualFile)

    val icon: Icon
    val tooltipProvider: Function<PsiElement, String>
    if (TfInitAction.isInitRequired(leaf.project, leaf.containingFile.virtualFile)) {
      icon = IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark)
      tooltipProvider = Function<PsiElement, String> { HCLBundle.message("not.initialized.inspection.error.message") }
    }
    else {
      icon = AllIcons.RunConfigurations.TestState.Run
      tooltipProvider = Function<PsiElement, String> { HCLBundle.message("terraform.run.text", toolType.displayName) }
    }

    return Info(icon, computeActions(block, toolType), tooltipProvider)
  }

  private fun computeActions(block: HCLBlock, toolType: TfToolType): Array<AnAction> {
    val project = block.project
    val templateActions = getRunTemplateActions(toolType)
    val rootModule = TfRunBaseConfigAction.getRootModule(block)

    val templateConfigNames = templateActions.filterIsInstance<TfRunBaseConfigAction>().map { it.getConfigurationName(rootModule) }
    val runManager = RunManager.getInstance(project)
    val existingConfigs = runManager.allSettings.filter {
      val configuration = it.configuration as? TfToolsRunConfigurationBase
      configuration != null && configuration.workingDirectory == rootModule.path && configuration.name !in templateConfigNames
    }

    val actions = mutableListOf(*templateActions)
    actions.addAll(existingConfigs.map { TfRunExistingConfigAction(it) })

    actions.add(Separator())
    actions.add(ActionManager.getInstance().getAction("editRunConfigurations"))

    return actions.toTypedArray()
  }

  private fun getRunTemplateActions(toolType: TfToolType): Array<AnAction> {
    val actionManager = ActionManager.getInstance()
    val group = actionManager.getAction(tfRunConfigurationType(toolType).actionGroupId)?.let { it as DefaultActionGroup }
    return group?.getChildren(actionManager) ?: emptyArray()
  }

}