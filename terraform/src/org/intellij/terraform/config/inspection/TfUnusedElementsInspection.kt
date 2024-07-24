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
      val unusedElement: HclUnusedElement? = when {
        TerraformPatterns.VariableRootBlock.accepts(block) -> object : HclUnusedElement(holder, block) {
          override val inspectionMessage: String = HCLBundle.message("unused.variable.inspection.error.message", name)
          override val quickFix: LocalQuickFix = RemoveVariableQuickFix(block)
        }
        TerraformPatterns.DataSourceRootBlock.accepts(block) -> object : HclUnusedElement(holder, block) {
          override val inspectionMessage: String = HCLBundle.message("unused.data.source.inspection.error.message", name)
          override val quickFix: LocalQuickFix = RemoveDataSourceQuickFix(block)
        }
        else -> null
      }

      unusedElement?.checkElement()
    }

    override fun visitProperty(property: HCLProperty) {
      if (TerraformPatterns.LocalProperty.accepts(property)) {
        object : HclUnusedElement(holder, property) {
          override val inspectionMessage: String = HCLBundle.message("unused.local.inspection.error.message", name)
          override val quickFix: LocalQuickFix = RemoveLocalQuickFix(property)
        }.checkElement()
      }
    }
  }
}

internal abstract class HclUnusedElement(private val holder: ProblemsHolder, private val element: HCLElement) {
  abstract val inspectionMessage: String
  abstract val quickFix: LocalQuickFix

  val name: String? by lazy { element.getElementName() }

  private fun isElementUnused(): Boolean {
    val elementName = name ?: return false
    val module = element.getTerraformModule()
    val searchScope = module.getTerraformModuleScope()

    val costSearch = PsiSearchHelper.getInstance(element.project).isCheapEnoughToSearch(elementName, searchScope, element.containingFile)
    if (costSearch != PsiSearchHelper.SearchCostResult.ZERO_OCCURRENCES) {
      return false
    }

    return ReferencesSearch.search(element, searchScope, false).findFirst() == null
  }

  fun checkElement() {
    if (isElementUnused()) {
      val highlightedElement = HCLPsiUtil.getPsiIdentifierName(element) ?: return
      holder.registerProblem(highlightedElement, inspectionMessage, ProblemHighlightType.LIKE_UNUSED_SYMBOL, quickFix)
    }
  }
}

private class RemoveVariableQuickFix(element: HCLBlock) : RemovePsiElementQuickFix(element) {
  override fun getText(): String = HCLBundle.message("unused.variable.inspection.quick.fix.name")
}

private class RemoveLocalQuickFix(element: HCLProperty) : RemovePsiElementQuickFix(element) {
  override fun getText(): String = HCLBundle.message("unused.local.inspection.quick.fix.name")
}

private class RemoveDataSourceQuickFix(element: HCLBlock) : RemovePsiElementQuickFix(element) {
  override fun getText(): String = HCLBundle.message("unused.data.source.inspection.quick.fix.name")
}