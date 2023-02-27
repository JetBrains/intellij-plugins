// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.NullableFunction
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TerraformPatterns

class TFDuplicatedOutputInspection : TFDuplicatedInspectionBase() {

  override fun createVisitor(holder: ProblemsHolder): PsiElementVisitor {
    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      val duplicates = getDuplicates(block) ?: return
      val name = block.getNameElementUnquoted(1) ?: return
      holder.registerProblem(block, HCLBundle.message("duplicated.output.inspection.output.declared.multiple.times.error.message", name),
                             ProblemHighlightType.GENERIC_ERROR, *getFixes(block, duplicates))
    }
  }

  private fun getDuplicates(block: HCLBlock): List<HCLBlock>? {
    if (!TerraformPatterns.OutputRootBlock.accepts(block)) return null
    val inOverride = TerraformPatterns.ConfigOverrideFile.accepts(block.containingFile)
    if (inOverride) return null

    val module = block.getTerraformModule()

    val name = block.getNameElementUnquoted(1) ?: return null

    val same = module.getDefinedOutputs().filter { name == it.getNameElementUnquoted(1) && !TerraformPatterns.ConfigOverrideFile.accepts(it.containingFile) }
    if (same.isEmpty()) return null
    if (same.size == 1) {
      if (same.first() == block) {
        return null
      }
    }
    return same
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

    fixes.add(DeleteOutputFix)
    fixes.add(RenameOutputFix)
    return fixes.toTypedArray()
  }

  private object DeleteOutputFix : LocalQuickFix, LowPriorityAction {
    override fun getFamilyName(): String {
      return HCLBundle.message("duplicated.output.inspection.delete.output.quick.fix.name")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val block = descriptor.psiElement as? HCLBlock ?: return
      IntentionPreviewUtils.write<Throwable> { block.delete() }
    }
  }

  private object RenameOutputFix : Companion.RenameQuickFix() {
    override fun getFamilyName(): String {
      return HCLBundle.message("duplicated.output.inspection.rename.output.quick.fix")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val block = descriptor.psiElement as? HCLBlock ?: return
      invokeRenameRefactoring(project, block)
    }
  }
}

