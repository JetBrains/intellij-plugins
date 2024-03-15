// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.ui.IconManager
import org.intellij.terraform.config.actions.TFInitAction
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import java.util.function.Function
import javax.swing.Icon

class TerraformRunLineMarkerContributor : RunLineMarkerContributor(), DumbAware {
  override fun getInfo(leaf: PsiElement): Info? {
    val psiFile = leaf.containingFile
    if (psiFile.fileType.defaultExtension != "tf") {
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

    val actions = mutableListOf<AnAction>()
    val icon: Icon
    val tooltipProvider: Function<PsiElement, String?>

    if (TFInitAction.isInitRequired(leaf.project, leaf.containingFile.virtualFile)) {
      val initAction = ActionManager.getInstance().getAction("TFInitRequiredAction")
      actions.add(initAction)
      icon = IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark)
      tooltipProvider = Function<PsiElement, String?> { HCLBundle.message("not.initialized.inspection.error.message") }
    }
    else {
      icon = AllIcons.RunConfigurations.TestState.Run
      tooltipProvider = Function<PsiElement, String?> { HCLBundle.message("terraform.run.text") }
    }
    actions.addAll(ExecutorAction.getActions(0))

    return Info(icon, actions.toTypedArray(), tooltipProvider)
  }
}