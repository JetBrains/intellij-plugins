// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInsight.intention.FileModifier.SafeFieldForPreview
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.*
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.toArray
import org.intellij.terraform.config.actions.TfInitAction
import org.intellij.terraform.config.codeinsight.ResourcePropertyInsertHandler
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TfPsiPatterns.ConfigOverrideFile
import org.intellij.terraform.config.patterns.TfPsiPatterns.DynamicBlock
import org.intellij.terraform.config.patterns.TfPsiPatterns.ModuleWithEmptySource
import org.intellij.terraform.config.psi.TfElementGenerator
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.isTerraformCompatiblePsiFile

class HCLBlockMissingPropertyInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerraformCompatiblePsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return MissingPropertyVisitor.create(holder, false)
  }

  override fun getID(): String {
    return "MissingProperty"
  }

  override fun getBatchSuppressActions(element: PsiElement?): Array<SuppressQuickFix> {
    return super.getBatchSuppressActions(PsiTreeUtil.getParentOfType(element, HCLBlock::class.java, false))
  }

}

internal class AddResourcePropertiesFix(@SafeFieldForPreview val add: Collection<PropertyOrBlockType>) : LocalQuickFix {
  override fun getName(): String {
    return HCLBundle.message("missing.resource.property.inspection.add.properties.quick.fix.name")
  }

  override fun getFamilyName(): String = name

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement.parent as? HCLBlock ?: return
    val obj = element.`object` ?: return
    IntentionPreviewUtils.write<Throwable> {
      val generator = TfElementGenerator(project)
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
        }
        else generator.createBlock(it.name)
      }
      for (it in elements) {
        obj.addBefore(it, obj.lastChild)
        obj.node.addLeaf(TokenType.WHITE_SPACE, "\n", obj.node.lastChildNode)
      }
      // TODO: Investigate why reformat fails: IJPL-159260
      // CodeStyleManager.getInstance(project).reformat(block, true)
      // TODO: Navigate cursor to added.last() or added.first()
    }
  }
}

internal class MissingPropertyVisitor(val holder: ProblemsHolder, val recursive: Boolean) : HCLElementVisitor() {

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
    val properties = TfModelHelper.getBlockProperties(block)
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
      }
      else {
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
    val nameOfBlock = HCLPsiUtil.getIdentifierPsi(block) ?: return
    val size = required.size
    holder.registerProblem(
      nameOfBlock,
      HCLBundle.message(
        "missing.resource.property.inspection.required.properties.error.message",
        required.joinToString(", ", postfix = if (size > DisplayLimit) " [$size]" else "", limit = DisplayLimit) { it.name }
      ),
      ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
      *listOfNotNull(
        AddResourcePropertiesFix(required),
        TfInitAction.createQuickFixNotInitialized(nameOfBlock),
        createDisableDeepVariableSearchQuickFix()
      ).toArray(LocalQuickFix.EMPTY_ARRAY)
    )
  }

  companion object {
    fun create(holder: ProblemsHolder, recursive: Boolean): PsiElementVisitor {
      if (!isTerraformCompatiblePsiFile(holder.file)) {
        return EMPTY_VISITOR
      }
      return MissingPropertyVisitor(holder, recursive)
    }
  }
}

private const val DisplayLimit: Int = 5