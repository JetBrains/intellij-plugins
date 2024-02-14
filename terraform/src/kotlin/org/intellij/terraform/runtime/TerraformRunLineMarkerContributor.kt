// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import org.intellij.terraform.hcl.psi.HCLBlock
import java.util.function.Function

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

    val actions = ExecutorAction.getActions(0)
    val event = createActionEvent(leaf)
    val tooltipProvider = Function<PsiElement, String?> { actions.mapNotNull { getText(it, event) }.joinToString("\n") }
    return Info(AllIcons.RunConfigurations.TestState.Run, actions, tooltipProvider)
  }
}