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

import com.intellij.codeInsight.intention.FileModifier.SafeFieldForPreview
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.*
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.codeinsight.ResourcePropertyInsertHandler
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TerraformPatterns.ConfigOverrideFile
import org.intellij.terraform.config.patterns.TerraformPatterns.DynamicBlock
import org.intellij.terraform.config.patterns.TerraformPatterns.ModuleWithEmptySource
import org.intellij.terraform.config.psi.TerraformElementGenerator
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

class HCLBlockMissingPropertyInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return createVisitor(holder, false)
  }

  fun createVisitor(holder: ProblemsHolder, recursive: Boolean): PsiElementVisitor {
    val ft = holder.file.fileType
    if (ft != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return MyEV(holder, recursive)
  }

  override fun getID(): String {
    return "MissingProperty"
  }

  override fun getBatchSuppressActions(element: PsiElement?): Array<SuppressQuickFix> {
    return super.getBatchSuppressActions(PsiTreeUtil.getParentOfType(element, HCLBlock::class.java, false))
  }

  private inner class MyEV(val holder: ProblemsHolder, val recursive: Boolean) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      ProgressIndicatorProvider.checkCanceled()
      block.getNameElementUnquoted(0) ?: return
      val obj = block.`object` ?: return
      // TODO: Generify
      if (ModuleWithEmptySource.accepts(block)) {
        // Check 'source' and report missing one
        doCheck(block, holder, TypeModel.Module.properties)
        return
      }
      if (ConfigOverrideFile.accepts(block.containingFile)) return
      val properties = ModelHelper.getBlockProperties(block)
      doCheck(block, holder, properties)
      if (recursive) {
        visitElement(obj)
      }
    }

    override fun visitElement(element: HCLElement) {
      super.visitElement(element)
      if (recursive) {
        element.acceptChildren(this)
      }
    }
  }

  private fun doCheck(block: HCLBlock, holder: ProblemsHolder, properties: Map<String, PropertyOrBlockType>) {
    if (properties.isEmpty()) return
    val obj = block.`object` ?: return
    ProgressIndicatorProvider.checkCanceled()

    val candidates = ArrayList(properties.values.filter { it.required && !(it is PropertyType && it.has_default) })
    if (candidates.isEmpty()) return
    val all = ArrayList<String>()
    all.addAll(obj.propertyList.map { it.name })
    for (it in obj.blockList) {
      if (DynamicBlock.accepts(it)) {
        all.add(it.name)
      } else {
        all.add(it.fullName)
      }
    }

    ProgressIndicatorProvider.checkCanceled()

    var required = candidates.filterNot { it.name in all }

    if (required.isEmpty()) return

    val requiredProps = required.filterIsInstance<PropertyType>()
    val requiredBlocks = required.filterIsInstance<BlockType>()

    required = requiredProps.sortedBy { it.name } + requiredBlocks.sortedBy { it.name }

    ProgressIndicatorProvider.checkCanceled()

    holder.registerProblem(
      block,
      HCLBundle.message(
        "missing.resource.property.inspection.required.properties.error.message",
        required.joinToString(", ") { it.name }
      ),
      ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
      AddResourcePropertiesFix(required)
    )
  }

}

class AddResourcePropertiesFix(@SafeFieldForPreview val add: Collection<PropertyOrBlockType>) : LocalQuickFix {
  override fun getName(): String {
    return HCLBundle.message(
      "missing.resource.property.inspection.add.properties.quick.fix.name",
      add.joinToString(", ") { it.name }
    )
  }

  override fun getFamilyName(): String {
    return HCLBundle.message("missing.resource.property.inspection.add.properties.quick.fix.family.name")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement as? HCLBlock ?: return
    val obj = element.`object` ?: return
    IntentionPreviewUtils.write<Throwable> {
      val generator = TerraformElementGenerator(project)
      val elements = add.map {
        if (it is PropertyType) {
          val type = it.type
          // TODO: Use property 'default' value
          var value: String = ResourcePropertyInsertHandler.getPlaceholderValue(type)?.first ?: when (type) {
            Types.Boolean -> "false"
            Types.Number -> "0"
            Types.Null -> "null"
            else -> "\"\""
          }

          value = ResourcePropertyInsertHandler.getProposedValueFromModelAndHint(it, element.getTerraformModule())?.first ?: value

          generator.createProperty(it.name, value)
        } else generator.createBlock(it.name)
      }
      for (it in elements) {
        obj.addBefore(it, obj.lastChild)
        obj.node.addLeaf(TokenType.WHITE_SPACE, "\n", obj.node.lastChildNode)
      }
      // TODO: Investigate why reformat fails
      // CodeStyleManager.getInstance(project).reformat(block, true)
      // TODO: Navigate cursor to added.last() or added.first()
    }
  }
}
