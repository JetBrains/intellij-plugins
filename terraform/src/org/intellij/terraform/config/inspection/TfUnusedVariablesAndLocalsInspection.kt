// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.searches.ReferencesSearch
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*

class TfUnusedVariablesAndLocalsInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    if (holder.file.fileType != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return BlockVisitor(holder)
  }

  inner class BlockVisitor(private val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      if (TerraformPatterns.VariableRootBlock.accepts(block) && isElementUnused(block)) {
        holder.registerProblem(
          block,
          HCLBundle.message("unused.variable.inspection.error.message", block.getNameElementUnquoted(1)),
          ProblemHighlightType.LIKE_UNUSED_SYMBOL,
          RemoveVariableQuickFix(block)
        )
      }

      if (TerraformPatterns.LocalsRootBlock.accepts(block)) {
        val localObject = block.`object`
        localObject?.propertyList?.forEach { local ->
          if (isElementUnused(local)) {
            holder.registerProblem(
              local,
              HCLBundle.message("unused.local.inspection.error.message", local.name),
              ProblemHighlightType.LIKE_UNUSED_SYMBOL,
              RemoveLocalQuickFix(local)
            )
          }
        }
      }
    }

    private fun isElementUnused(element: HCLElement): Boolean {
      val module = element.getTerraformModule()
      val searchScope = module.getTerraformModuleScope()
      return ReferencesSearch.search(element, searchScope, false).findFirst() == null
    }
  }
}

internal class RemoveVariableQuickFix(element: HCLBlock) : RemovePsiElementQuickFix(element) {
  override fun getText(): String = HCLBundle.message("unused.variable.inspection.quick.fix.name")
}

internal class RemoveLocalQuickFix(element: HCLProperty) : RemovePsiElementQuickFix(element) {
  override fun getText(): String = HCLBundle.message("unused.local.inspection.quick.fix.name")
}