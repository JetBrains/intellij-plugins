// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.searches.ReferencesSearch
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*

internal class TfUnusedElementsInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    if (holder.file.fileType != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

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
    val name = element.getElementName() ?: return
    // Need to know is that a suitable hclElement before reference search (isElementUnused method)
    val unused = getHclUnusedElement(element, name) ?: return

    if (isElementUnused(element, name)) {
      val highlighted = HCLPsiUtil.getIdentifierPsi(element) ?: return
      holder.registerProblem(highlighted, unused.inspectionMessage, ProblemHighlightType.LIKE_UNUSED_SYMBOL, unused.quickFix)
    }
  }

  private fun isElementUnused(element: HCLElement, name: String): Boolean {
    val module = element.getTerraformModule()
    val searchScope = module.getTerraformModuleScope()

    val costSearch = PsiSearchHelper.getInstance(element.project).isCheapEnoughToSearch(name, searchScope, element.containingFile)
    if (costSearch != PsiSearchHelper.SearchCostResult.ZERO_OCCURRENCES) {
      return false
    }

    return ReferencesSearch.search(element, searchScope, false).findFirst() == null
  }

  private fun getHclUnusedElement(element: HCLElement, name: String): HclUnusedElement? = when {
    TerraformPatterns.LocalProperty.accepts(element) -> object : HclUnusedElement {
      override val inspectionMessage: String = HCLBundle.message("unused.local.inspection.error.message", name)
      override val quickFix: LocalQuickFix = RemoveLocalQuickFix(element)
    }

    TerraformPatterns.VariableRootBlock.accepts(element) -> object : HclUnusedElement {
      override val inspectionMessage: String = HCLBundle.message("unused.variable.inspection.error.message", name)
      override val quickFix: LocalQuickFix = RemoveVariableQuickFix(element)
    }

    TerraformPatterns.DataSourceRootBlock.accepts(element) -> object : HclUnusedElement {
      override val inspectionMessage: String = HCLBundle.message("unused.data.source.inspection.error.message", name)
      override val quickFix: LocalQuickFix = RemoveDataSourceQuickFix(element)
    }
    else -> null
  }
}

private interface HclUnusedElement {
  val inspectionMessage: String
  val quickFix: LocalQuickFix
}

private class RemoveVariableQuickFix(element: HCLElement) : RemovePsiElementQuickFix(element) {
  override fun getText(): String = HCLBundle.message("unused.variable.inspection.quick.fix.name")
}

private class RemoveLocalQuickFix(element: HCLElement) : RemovePsiElementQuickFix(element) {
  override fun getText(): String = HCLBundle.message("unused.local.inspection.quick.fix.name")
}

private class RemoveDataSourceQuickFix(element: HCLElement) : RemovePsiElementQuickFix(element) {
  override fun getText(): String = HCLBundle.message("unused.data.source.inspection.quick.fix.name")
}