// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.util.containers.toArray
import org.intellij.terraform.config.actions.TfInitAction
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.isTerraformCompatiblePsiFile

class HCLDeprecatedElementInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerraformCompatiblePsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      ProgressIndicatorProvider.checkCanceled()
      block.getNameElementUnquoted(0) ?: return
      block.`object` ?: return
      val properties = TfModelHelper.getBlockProperties(block)
      doCheck(block, holder, properties)
    }
  }

  private fun doCheck(block: HCLBlock, holder: ProblemsHolder, properties: Map<String, PropertyOrBlockType>) {
    if (properties.isEmpty()) return
    val obj = block.`object` ?: return
    ProgressIndicatorProvider.checkCanceled()

    val candidates = properties.filterValues { it.deprecated != null }
    if (candidates.isEmpty()) return

    ProgressIndicatorProvider.checkCanceled()
    if (candidates.values.any { it is PropertyType }) for (hclProperty in obj.propertyList) {
      val name = hclProperty.name
      val reason = candidates[name]?.deprecated
      if (reason != null) {
        holder.registerProblem(
          hclProperty,
          HCLBundle.message(
            "deprecated.element.inspection.deprecated.property.error.message", name, reason,
            if (reason.isNotEmpty()) 0 else 1),
          ProblemHighlightType.LIKE_DEPRECATED,
          *listOfNotNull(TfInitAction.createQuickFixNotInitialized(block)).toArray(LocalQuickFix.EMPTY_ARRAY)
        )
      }
    }

    ProgressIndicatorProvider.checkCanceled()
    if (candidates.values.any { it is BlockType }) for (hclBlock in obj.blockList) {
      val name = hclBlock.name
      val reason = candidates[name]?.deprecated
      if (reason != null) {
        holder.registerProblem(
          hclBlock,
          HCLBundle.message(
            "deprecated.element.inspection.deprecated.block.error.message", name, reason,
            if (reason.isNotEmpty()) 0 else 1),
          ProblemHighlightType.LIKE_DEPRECATED
        )
      }
    }
  }

}
