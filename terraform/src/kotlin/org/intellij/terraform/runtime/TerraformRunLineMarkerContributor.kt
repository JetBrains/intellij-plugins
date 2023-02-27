// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.config.patterns.TerraformPatterns

class TerraformRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(leaf: PsiElement): Info? {
    if (!HCLTokenTypes.IDENTIFYING_LITERALS.contains(leaf.node?.elementType)) return null

    val identifier = leaf.parent ?: return null

    val block = identifier.parent as? HCLBlock ?: return null

    if (block.nameIdentifier !== identifier) return null

    if (!TerraformPatterns.ResourceRootBlock.accepts(block)) return null

    TerraformResourceConfigurationProducer.getResourceTarget(block) ?: return null

    val actions = ExecutorAction.getActions(0)
    val tooltipProvider: Function<PsiElement, String> = Function { psiElement ->
      @Suppress("UselessCallOnCollection")
      actions.filterNotNull().mapNotNull { getText(it, psiElement) }.joinToString("\n")
    }
    return Info(AllIcons.RunConfigurations.TestState.Run, tooltipProvider, *actions)
  }
}