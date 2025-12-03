// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.modcommand.ModCommandAction
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.searches.ReferencesSearch
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.isTfOrTofuPsiFile

internal class TfUnusedElementsInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTfOrTofuPsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return TfVisitor(holder)
  }

  inner class TfVisitor(private val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      checkElement(holder, block)
    }

    override fun visitProperty(property: HCLProperty) {
      checkElement(holder, property)
    }
  }

  private fun checkElement(holder: ProblemsHolder, element: HCLElement) {
    ProgressIndicatorProvider.checkCanceled()
    val name = element.getElementName() ?: return
    // Need to know is that a suitable hclElement before reference search (isElementUnused method)
    val unused = getHclUnusedElement(element, name) ?: return

    if (isElementUnused(element, name)) {
      val highlighted = HCLPsiUtil.getIdentifierPsi(element) ?: return
      holder.problem(highlighted, unused.inspectionMessage)
        .highlight(ProblemHighlightType.LIKE_UNUSED_SYMBOL)
        .fix(unused.quickFix)
        .register()
    }
  }

  private fun isElementUnused(element: HCLElement, name: String): Boolean {
    ProgressIndicatorProvider.checkCanceled()
    val module = element.getTerraformModule()
    val searchScope = module.getTerraformModuleScope()

    val costSearch = PsiSearchHelper.getInstance(element.project).isCheapEnoughToSearch(name, searchScope, element.containingFile)
    if (costSearch != PsiSearchHelper.SearchCostResult.ZERO_OCCURRENCES) {
      return false
    }

    return ReferencesSearch.search(element, searchScope, false).findFirst() == null
  }

  private fun getHclUnusedElement(element: HCLElement, name: String): HclUnusedElement? = when {
    TfPsiPatterns.LocalProperty.accepts(element) -> object : HclUnusedElement {
      override val inspectionMessage: String = HCLBundle.message("unused.local.inspection.error.message", name)
      override val quickFix: ModCommandAction = RemoveLocalQuickFix(element)
    }

    TfPsiPatterns.VariableRootBlock.accepts(element) -> object : HclUnusedElement {
      override val inspectionMessage: String = HCLBundle.message("unused.variable.inspection.error.message", name)
      override val quickFix: ModCommandAction = RemoveVariableQuickFix(element)
    }

    TfPsiPatterns.DataSourceRootBlock.accepts(element) -> object : HclUnusedElement {
      override val inspectionMessage: String = HCLBundle.message("unused.data.source.inspection.error.message", name)
      override val quickFix: ModCommandAction = RemoveDataSourceQuickFix(element)
    }
    else -> null
  }
}

private interface HclUnusedElement {
  val inspectionMessage: String
  val quickFix: ModCommandAction
}

private class RemoveVariableQuickFix(element: HCLElement) : RemovePsiElementQuickFix(element) {
  override fun getFamilyName(): @IntentionFamilyName String = HCLBundle.message("unused.variable.inspection.quick.fix.name")
}

private class RemoveLocalQuickFix(element: HCLElement) : RemovePsiElementQuickFix(element) {
  override fun getFamilyName(): @IntentionFamilyName String = HCLBundle.message("unused.local.inspection.quick.fix.name")
}

private class RemoveDataSourceQuickFix(element: HCLElement) : RemovePsiElementQuickFix(element) {
  override fun getFamilyName(): @IntentionFamilyName String = HCLBundle.message("unused.data.source.inspection.quick.fix.name")
}