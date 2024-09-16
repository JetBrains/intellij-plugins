// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.actions.TFInitAction
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*

class TfUnknownPropertyInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    if (holder.file.fileType != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return TfPropertyVisitor(holder)
  }

  inner class TfPropertyVisitor(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitProperty(property: HCLProperty) {
      super.visitProperty(property)

      val hclBlock = getBlockOfProperty(property) ?: return
      val properties = TfModelHelper.getBlockProperties(hclBlock).ifEmpty { return }

      val propertyName = property.name
      if (!properties.containsKey(propertyName)) {
        holder.registerProblem(
          property,
          HCLBundle.message("unknown.property.in.block.inspection.error.message", propertyName),
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
          *listOfNotNull(TFInitAction.createQuickFixNotInitialized(property), RemovePropertyQuickFix(property)).toTypedArray()
        )
      }
    }

    private fun getBlockOfProperty(element: HCLElement): HCLBlock? {
      val hclObject = element.parent as? HCLObject
      val hclBlock = hclObject?.parent as? HCLBlock
      return if (TerraformPatterns.RootBlock.accepts(hclBlock)) hclBlock else null
    }
  }
}

internal abstract class RemovePsiElementQuickFix(element: HCLElement) : LocalQuickFixOnPsiElement(element) {
  override fun getFamilyName(): String = text
  override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
    startElement.delete()
  }
}

private class RemovePropertyQuickFix(element: HCLElement) : RemovePsiElementQuickFix(element) {
  override fun getText(): String = HCLBundle.message("unknown.property.in.block.inspection.quick.fix.name")
}