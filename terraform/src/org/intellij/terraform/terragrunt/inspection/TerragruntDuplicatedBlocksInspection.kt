// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.inspection.DeleteBlockQuickFix
import org.intellij.terraform.config.inspection.RenameBlockQuickFix
import org.intellij.terraform.config.inspection.TfDuplicatedInspectionBase
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.terragrunt.*
import org.intellij.terraform.terragrunt.codeinsight.TerragruntUnitHelper.collectMatchingBlocks
import org.intellij.terraform.terragrunt.patterns.TerragruntPsiPatterns

internal class TerragruntDuplicatedBlocksInspection : TfDuplicatedInspectionBase() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerragruntPsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    if (!TerragruntPsiPatterns.TerragruntFile.accepts(file)) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return createVisitor(holder)
  }

  override fun createVisitor(holder: ProblemsHolder): PsiElementVisitor = RootBlocksVisitor(holder)

  inner class RootBlocksVisitor(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      if (block.parent !is PsiFile) return

      val name = block.getNameElementUnquoted(1) ?: return
      val duplicates = getDuplicates(block, name) ?: return
      holder.registerProblem(
        block,
        HCLBundle.message("terragrunt.duplicated.blocks.inspection.error.message", name),
        ProblemHighlightType.WARNING,
        RenameBlockQuickFix(), DeleteBlockQuickFix(), *getDefaultFixes(block, duplicates)
      )
    }
  }

  private fun getDuplicates(block: HCLBlock, name: String): List<HCLBlock>? {
    val blockType = when {
                      TerragruntPsiPatterns.StackBlockPattern.accepts(block) -> TERRAGRUNT_STACK
                      TerragruntPsiPatterns.UnitBlockPattern.accepts(block) -> TERRAGRUNT_UNIT
                      TerragruntPsiPatterns.IncludeBlockPattern.accepts(block) -> TERRAGRUNT_INCLUDE
                      TerragruntPsiPatterns.DependencyBlockPattern.accepts(block) -> TERRAGRUNT_DEPENDENCY
                      TerragruntPsiPatterns.GenerateBlockPattern.accepts(block) -> TERRAGRUNT_GENERATE
                      TerragruntPsiPatterns.FeatureBlockPattern.accepts(block) -> TERRAGRUNT_FEATURE
                      else -> null
                    } ?: return null

    val matchedBlocks = collectMatchingBlocks(block, blockType, name)
    if (matchedBlocks.isEmpty()) return null
    if (matchedBlocks.size == 1 && matchedBlocks.first() == block) {
      return null
    }

    return matchedBlocks
  }
}