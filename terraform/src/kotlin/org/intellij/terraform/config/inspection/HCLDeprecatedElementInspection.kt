/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElementVisitor
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.config.model.PropertyType

class HCLDeprecatedElementInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val ft = holder.file.fileType
    if (ft != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      ProgressIndicatorProvider.checkCanceled()
      block.getNameElementUnquoted(0) ?: return
      block.`object` ?: return
      val properties = ModelHelper.getBlockProperties(block)
      doCheck(block, holder, properties)
    }
  }

  private fun doCheck(block: HCLBlock, holder: ProblemsHolder, properties: Map<String, PropertyOrBlockType>) {
    if (properties.isEmpty()) return
    val obj = block.`object` ?: return
    ProgressIndicatorProvider.checkCanceled()

    val candidates = properties.filterValues { it.deprecated != null }
    if (candidates.isEmpty()) return

    ProgressIndicatorProvider.checkCanceled()
    if (candidates.values.any { it is PropertyType }) for (hclProperty in obj.propertyList) {
      val name = hclProperty.name
      val reason = candidates[name]?.deprecated
      if (reason != null) {
        holder.registerProblem(
          hclProperty,
          HCLBundle.message(
            "deprecated.element.inspection.deprecated.property.error.message", name, reason,
            if (reason.isNotEmpty()) 0 else 1),
          ProblemHighlightType.LIKE_DEPRECATED
        )
      }
    }

    ProgressIndicatorProvider.checkCanceled()
    if (candidates.values.any { it is BlockType }) for (hclBlock in obj.blockList) {
      val name = hclBlock.name
      val reason = candidates[name]?.deprecated
      if (reason != null) {
        holder.registerProblem(
          hclBlock,
          HCLBundle.message(
            "deprecated.element.inspection.deprecated.block.error.message", name, reason,
            if (reason.isNotEmpty()) 0 else 1),
          ProblemHighlightType.LIKE_DEPRECATED
        )
      }
    }
  }

}
