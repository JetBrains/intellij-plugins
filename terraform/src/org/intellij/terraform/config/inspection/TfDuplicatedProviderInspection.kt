// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.NullableFunction
import org.intellij.terraform.config.model.getProviderFQName
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.jetbrains.annotations.NonNls

class TfDuplicatedProviderInspection : TfDuplicatedInspectionBase() {

  override fun createVisitor(holder: ProblemsHolder): PsiElementVisitor {
    return MyEV(holder)
  }


  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      val duplicates = getDuplicates(block) ?: return
      @NonNls val fqn = block.getProviderFQName() ?: return
      holder.registerProblem(block,
                             HCLBundle.message("duplicated.provider.inspection.display.name.multiple.declaration.error.message", fqn),
                             ProblemHighlightType.GENERIC_ERROR, *getFixes(fqn != block.getNameElementUnquoted(1), block, duplicates))
    }
  }

  private fun getDuplicates(block: HCLBlock): List<HCLBlock>? {
    if (!TfPsiPatterns.ProviderRootBlock.accepts(block)) return null
    if (TfPsiPatterns.ConfigOverrideFile.accepts(block.containingFile)) return null

    val module = block.getTerraformModule()

    val fqn = block.getProviderFQName() ?: return null

    val same = module.getDefinedProviders().filter { it.second == fqn && !TfPsiPatterns.ConfigOverrideFile.accepts(it.first.containingFile) }
    if (same.isEmpty()) return null
    if (same.size == 1) {
      if (same.first().first == block) {
        return null
      }
    }
    return same.map { it.first }
  }

  private fun getFixes(aliased: Boolean, block: HCLBlock, duplicates: List<HCLBlock>): Array<LocalQuickFix> {
    val fixes = ArrayList<LocalQuickFix>()

    val first = duplicates.firstOrNull { it != block }
    first?.containingFile?.virtualFile?.let { createNavigateToDupeFix(first, duplicates.size <= 2).let { fixes.add(it) } }
    block.containingFile?.virtualFile?.let { createShowOtherDupesFix(block, NullableFunction { param -> getDuplicates(param as HCLBlock) }).let { fixes.add(it) } }

    if (false) {
      // TODO: Implement fixes
      if (aliased) {
        fixes.add(ChangeAliasFix)
      }
      else {
        fixes.add(AddAliasFix)
      }
    }

    return fixes.toTypedArray()
  }

  private object AddAliasFix : LocalQuickFix {
    override fun getFamilyName(): String {
      return HCLBundle.message("duplicated.provider.inspection.display.name.add.provider.alias.quick.fix.name")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      TODO("Implement")
    }
  }

  private object ChangeAliasFix : LocalQuickFix {
    override fun getFamilyName(): String {
      return HCLBundle.message("duplicated.provider.inspection.display.name.change.provider.alias.quick.fix.name")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      TODO("Implement")
    }
  }
}

