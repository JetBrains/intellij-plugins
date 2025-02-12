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
import org.intellij.terraform.config.actions.AddProviderAction
import org.intellij.terraform.config.actions.TfInitAction
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.TypeModel
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.isTerraformCompatiblePsiFile
import kotlin.collections.listOfNotNull

internal class TfUnknownResourceInspection : LocalInspectionTool() {

  private val allowedIdentifiers = listOf(HCL_RESOURCE_IDENTIFIER, HCL_DATASOURCE_IDENTIFIER, HCL_PROVIDER_IDENTIFIER)

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerraformCompatiblePsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return TfBlockVisitor(holder)
  }

  inner class TfBlockVisitor(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      ProgressIndicatorProvider.checkCanceled()
      val blockTypeString = getBlockTypeString(block, allowedIdentifiers) ?: return
      val identifier = block.getNameElementUnquoted(1) ?: return

      val model = TypeModelProvider.getModel(block)
      val provider = model.getProviderType(identifier, block)
      if (provider == null) {
        holder.registerProblem(block,
                               HCLBundle.message("unknown.resource.identifier.inspection.error.message", blockTypeString, identifier),
                               *listOfNotNull(
                                 TfInitAction.createQuickFixNotInitialized(block),
                                 AddProviderAction(block)
                               ).toArray(LocalQuickFix.EMPTY_ARRAY)
        )
      }
      else if (getTypeForBlock(blockTypeString, identifier, block, model) == null) {
        holder.registerProblem(block,
                               HCLBundle.message("unknown.resource.identifier.for.known.provider", blockTypeString, identifier, provider.fullName),
                               *listOfNotNull(
                                 TfInitAction.createQuickFixNotInitialized(block)
                               ).toArray(LocalQuickFix.EMPTY_ARRAY)
        )
      }
    }
  }

  private fun getTypeForBlock(blockType: String, identifier: String, block: HCLBlock, model: TypeModel): BlockType? = when (blockType) {
    HCL_RESOURCE_IDENTIFIER -> model.getResourceType(identifier, block)
    HCL_DATASOURCE_IDENTIFIER -> model.getDataSourceType(identifier, block)
    HCL_PROVIDER_IDENTIFIER -> model.getProviderType(identifier, block)
    else -> null
  }


  private fun getBlockTypeString(block: HCLBlock, allowedIdentifiers: List<String>): String? =
    block.getNameElementUnquoted(0)?.lowercase()?.takeIf { it in allowedIdentifiers }

}