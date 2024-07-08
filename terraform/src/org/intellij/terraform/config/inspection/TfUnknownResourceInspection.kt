// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.util.containers.toArray
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.actions.AddProviderAction
import org.intellij.terraform.config.actions.TFInitAction
import org.intellij.terraform.config.codeinsight.TfModelHelper.getTypeForBlock
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import kotlin.collections.listOfNotNull

internal class TfUnknownResourceInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return file.fileType == TerraformFileType
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return TfBlockVisitor(holder)
  }

  inner class TfBlockVisitor(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      ProgressIndicatorProvider.checkCanceled()
      if (block.nameElements.size < 2) return
      val blockType = getTypeForBlock(block)
      val allowedBlockTypeString = getAllowedBlockTypeString(block)
      if (blockType == null && allowedBlockTypeString.isNotEmpty()) {
        val blockName = block.getNameElementUnquoted(1) ?: ""
        holder.registerProblem(block,
                               HCLBundle.message("unknown.resource.identifier.inspection.error.message", allowedBlockTypeString, blockName),
                               *listOfNotNull(
                                 TFInitAction.createQuickFixNotInitialized(block),
                                 AddProviderAction(block)
                               ).toArray(LocalQuickFix.EMPTY_ARRAY)
        )
      }
    }
  }

  private fun getAllowedBlockTypeString(block: HCLBlock): String {
    val blockType = block.getNameElementUnquoted(0)?.lowercase() ?: ""
    return when (blockType) {
      HCL_RESOURCE_IDENTIFIER -> "resource"
      HCL_DATASOURCE_IDENTIFIER -> "datasource"
      HCL_PROVIDER_IDENTIFIER -> "provider"
      else -> ""
    }
  }

}