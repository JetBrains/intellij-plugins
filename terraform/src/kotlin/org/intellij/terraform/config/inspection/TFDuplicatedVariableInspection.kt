/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

class TFDuplicatedVariableInspection : TFDuplicatedInspectionBase() {
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
    if (!TerraformPatterns.VariableRootBlock.accepts(block)) return null
    if (TerraformPatterns.ConfigOverrideFile.accepts(block.containingFile)) return null

    val module = block.getTerraformModule()

    val name = block.getNameElementUnquoted(1) ?: return null

    val same = module.findVariables(name).filter { !TerraformPatterns.ConfigOverrideFile.accepts(it.declaration.containingFile) }
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

