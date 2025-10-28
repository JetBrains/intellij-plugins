// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

class TfDuplicatedVariableInspection : TfDuplicatedInspectionBase() {
  override fun createVisitor(holder: ProblemsHolder): PsiElementVisitor {
    return VariableBlockVisitor(holder)
  }

  inner class VariableBlockVisitor(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      val duplicates = getDuplicates(block) ?: return
      val name = block.getNameElementUnquoted(1) ?: return
      holder.registerProblem(
        block,
        HCLBundle.message("duplicated.variable.inspection.multiple.declaration.error.message", name),
        ProblemHighlightType.GENERIC_ERROR,
        RenameBlockQuickFix(), DeleteBlockQuickFix(), *getDefaultFixes(block, duplicates)
      )
    }
  }

  private fun getDuplicates(block: HCLBlock): List<HCLBlock>? {
    if (!TfPsiPatterns.VariableRootBlock.accepts(block)) return null
    if (TfPsiPatterns.ConfigOverrideFile.accepts(block.containingFile)) return null

    val module = block.getTerraformModule()
    val name = block.getNameElementUnquoted(1) ?: return null

    val sameVariables = module.findVariables(name).filter { !TfPsiPatterns.ConfigOverrideFile.accepts(it.declaration.containingFile) }
    if (sameVariables.isEmpty()) return null
    if (sameVariables.size == 1 && sameVariables.first().declaration == block) {
      return null
    }
    return sameVariables.map { it.declaration }
  }
}

