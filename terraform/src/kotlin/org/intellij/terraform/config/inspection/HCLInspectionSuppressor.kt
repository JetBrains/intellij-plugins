// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.SuppressionUtil
import com.intellij.psi.PsiElement
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

class HCLInspectionSuppressor : InspectionSuppressor {
  override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
    // TODO: Support suppression for file, module
    return SuppressionUtil.isSuppressedInStatement(element, toolId, HCLBlock::class.java)
        || SuppressionUtil.isSuppressedInStatement(element, toolId, HCLProperty::class.java)
  }

  override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
    if (element == null) return SuppressQuickFix.EMPTY_ARRAY
    return listOfNotNull(
      HCLSuppressInspectionFix(toolId, HCLBlock::class.java) {
        HCLBundle.message("inspection.suppressor.suppress.for.element.action.name",
                          it.getContainer(element)?.getNameElementUnquoted(0) ?: "block")
      }.takeIf { it.getContainer(element) != null },
      HCLSuppressInspectionFix(toolId, HCLBundle.message("inspection.suppressor.suppress.for.property.action.name"),
                               HCLProperty::class.java).takeIf { it.getContainer(element) != null }
    ).toTypedArray()
  }
}