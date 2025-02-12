// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.NullableFunction
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

class TfDuplicatedVariableInspection : TfDuplicatedInspectionBase() {
  override fun createVisitor(holder: ProblemsHolder): PsiElementVisitor {
    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      val duplicates = getDuplicates(block) ?: return
      val name = block.getNameElementUnquoted(1) ?: return
      holder.registerProblem(block, HCLBundle.message("duplicated.variable.inspection.multiple.declaration.error.message", name),
                             ProblemHighlightType.GENERIC_ERROR, *getFixes(block, duplicates))
    }
  }

  private fun getDuplicates(block: HCLBlock): List<HCLBlock>? {
    if (!TfPsiPatterns.VariableRootBlock.accepts(block)) return null
    if (TfPsiPatterns.ConfigOverrideFile.accepts(block.containingFile)) return null

    val module = block.getTerraformModule()

    val name = block.getNameElementUnquoted(1) ?: return null

    val same = module.findVariables(name).filter { !TfPsiPatterns.ConfigOverrideFile.accepts(it.declaration.containingFile) }
    if (same.isEmpty()) return null
    if (same.size == 1) {
      if (same.first().declaration == block) {
        return null
      }
    }
    return same.map { it.declaration }
  }

  private fun getFixes(block: HCLBlock, duplicates: List<HCLBlock>): Array<LocalQuickFix> {
    val fixes = ArrayList<LocalQuickFix>()

    val first = duplicates.first { it != block }
    first.containingFile?.virtualFile?.let { createNavigateToDupeFix(first, duplicates.size <= 2).let { fixes.add(it) } }
    block.containingFile?.virtualFile?.let {
      createShowOtherDupesFix(block, NullableFunction { param ->
        getDuplicates(param as HCLBlock)
      }).let { fixes.add(it) }
    }

    fixes.add(DeleteVariableFix)
    fixes.add(RenameVariableFix)
    return fixes.toTypedArray()
  }


  private object DeleteVariableFix : LocalQuickFix, LowPriorityAction {
    override fun getFamilyName(): String = HCLBundle.message("duplicated.variable.inspection.delete.variable.quick.fix.name")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val block = descriptor.psiElement as? HCLBlock ?: return
      IntentionPreviewUtils.write<Throwable> { block.delete() }
    }
  }

  private object RenameVariableFix : Companion.RenameQuickFix() {
    override fun getFamilyName(): String = HCLBundle.message("duplicated.variable.inspection.rename.variable.quick.fix.name")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val block = descriptor.psiElement as? HCLBlock ?: return
      invokeRenameRefactoring(project, block)
    }
  }
}

