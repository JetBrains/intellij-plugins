// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.actions.createQuickFixNotInitialized
import org.intellij.terraform.config.actions.isInitializedDir
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.isTerraformCompatiblePsiFile

class TfUnknownPropertyInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerraformCompatiblePsiFile(file) && isInitializedDir(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return TfPropertyVisitor(holder)
  }

  class TfPropertyVisitor(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitProperty(property: HCLProperty) {
      super.visitProperty(property)

      val hclBlock = getBlockOfProperty(property) ?: return
      val properties = TfModelHelper.getBlockProperties(hclBlock).ifEmpty { return }

      val propertyName = property.name
      if (!properties.containsKey(propertyName)) {
        holder.problem(
          property,
          HCLBundle.message("unknown.property.in.block.inspection.error.message", propertyName))
          .maybeFix(createQuickFixNotInitialized(property))
          .fix(RemovePropertyQuickFix(property))
          .register()
      }
    }

    private fun getBlockOfProperty(element: HCLElement): HCLBlock? {
      val hclObject = element.parent as? HCLObject
      val hclBlock = hclObject?.parent as? HCLBlock
      return if (TfPsiPatterns.RootBlock.accepts(hclBlock)) hclBlock else null
    }
  }
}

internal abstract class RemovePsiElementQuickFix(element: HCLElement) : PsiUpdateModCommandAction<HCLElement>(element) {
  override fun invoke(context: ActionContext, element: HCLElement, updater: ModPsiUpdater) {
    element.delete()
  }
}

private class RemovePropertyQuickFix(element: HCLElement) : RemovePsiElementQuickFix(element) {
  override fun getFamilyName(): @IntentionFamilyName String = HCLBundle.message("unknown.property.in.block.inspection.quick.fix.name")
}