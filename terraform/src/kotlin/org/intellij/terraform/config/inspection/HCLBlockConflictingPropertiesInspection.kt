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

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.model.PropertyOrBlockType

class HCLBlockConflictingPropertiesInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val ft = holder.file.fileType
    if (ft != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return MyEV(holder)
  }

  override fun getID(): String {
    return "ConflictingProperties"
  }

  override fun getBatchSuppressActions(element: PsiElement?): Array<SuppressQuickFix> {
    return super.getBatchSuppressActions(PsiTreeUtil.getParentOfType(element, HCLBlock::class.java, false))
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitProperty(property: HCLProperty) {
      ProgressIndicatorProvider.checkCanceled()
      val obj = property.parent as? HCLObject ?: return
      val block = obj.parent as? HCLBlock ?: return
      val properties = ModelHelper.getBlockProperties(block)
      if (property.value is HCLNullLiteral) return
      doCheck(holder, property, property.name, obj, properties)
    }

    override fun visitBlock(inner: HCLBlock) {
      ProgressIndicatorProvider.checkCanceled()
      val obj = inner.parent as? HCLObject ?: return
      val block = obj.parent as? HCLBlock ?: return
      val properties = ModelHelper.getBlockProperties(block)
      doCheck(holder, inner, inner.name, obj, properties)
    }
  }

  private fun doCheck(holder: ProblemsHolder, element: PsiElement, name: String, obj: HCLObject, properties: Map<String, PropertyOrBlockType>) {
    ProgressIndicatorProvider.checkCanceled()
    val pobt = properties[name] ?: return
    val conflictsWith = (pobt.conflictsWith ?: return) - name
    if (conflictsWith.isEmpty()) return


    ProgressIndicatorProvider.checkCanceled()
    val conflicts = ArrayList<String>()
    (obj.propertyList).filter { conflictsWith.contains(it.name) }.filter { it.value !is HCLNullLiteral }.mapTo(conflicts) { it.name }

    ProgressIndicatorProvider.checkCanceled()
    // TODO: Better block name selection?
    (obj.blockList).filter { conflictsWith.contains(it.name) }.mapTo(conflicts) { it.name }

    if (conflicts.isEmpty()) return

    // TODO: Add 'navigate to' and 'remove current element' quick fixes
    // TODO: Reuse quick fixes from TFDuplicatedInspectionBase
    holder.registerProblem(
      element,
      HCLBundle.message("conflicting.resource.property.inspection.error.message", conflicts.joinToString(", ")),
      ProblemHighlightType.GENERIC_ERROR_OR_WARNING
    )
  }

}
