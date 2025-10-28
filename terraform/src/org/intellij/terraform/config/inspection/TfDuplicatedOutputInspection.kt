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

class TfDuplicatedOutputInspection : TfDuplicatedInspectionBase() {

  override fun createVisitor(holder: ProblemsHolder): PsiElementVisitor {
    return OutputBlockVisitor(holder)
  }

  inner class OutputBlockVisitor(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      val duplicates = getDuplicates(block) ?: return
      val name = block.getNameElementUnquoted(1) ?: return
      holder.registerProblem(
        block,
        HCLBundle.message("duplicated.output.inspection.output.declared.multiple.times.error.message", name),
        ProblemHighlightType.GENERIC_ERROR,
        RenameBlockQuickFix(), DeleteBlockQuickFix(), *getDefaultFixes(block, duplicates)
      )
    }
  }

  private fun getDuplicates(block: HCLBlock): List<HCLBlock>? {
    if (!TfPsiPatterns.OutputRootBlock.accepts(block)) return null
    val inOverride = TfPsiPatterns.ConfigOverrideFile.accepts(block.containingFile)
    if (inOverride) return null

    val module = block.getTerraformModule()
    val name = block.getNameElementUnquoted(1) ?: return null

    val sameOutputs = module.getDefinedOutputs().filter { name == it.getNameElementUnquoted(1) && !TfPsiPatterns.ConfigOverrideFile.accepts(it.containingFile) }
    if (sameOutputs.isEmpty()) return null
    if (sameOutputs.size == 1 && sameOutputs.first() == block) {
      return null
    }
    return sameOutputs
  }
}

