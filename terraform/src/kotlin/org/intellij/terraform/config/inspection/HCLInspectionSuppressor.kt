/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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