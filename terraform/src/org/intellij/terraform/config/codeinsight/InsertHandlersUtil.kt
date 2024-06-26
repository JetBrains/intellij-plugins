// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_REQUIRED_PROVIDERS
import org.intellij.terraform.config.inspection.AddResourcePropertiesFix
import org.intellij.terraform.config.inspection.HCLBlockMissingPropertyInspection
import org.intellij.terraform.config.model.ProviderType
import org.intellij.terraform.config.model.TypeModel
import org.intellij.terraform.config.patterns.TerraformPatterns.RequiredProvidersBlock
import org.intellij.terraform.config.psi.TerraformElementGenerator
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLPsiUtil.getNextSiblingNonWhiteSpace
import org.intellij.terraform.hcl.psi.HCLStringLiteral

object InsertHandlersUtil {
  internal fun isNextNameOnTheSameLine(element: PsiElement, document: Document): Boolean {
    val right: PsiElement?
    if (element is HCLIdentifier || element is HCLStringLiteral) {
      right = element.getNextSiblingNonWhiteSpace()
    } else if (HCLTokenTypes.IDENTIFYING_LITERALS.contains(element.node?.elementType)) {
      if (element.parent is HCLIdentifier) {
        right = element.parent.getNextSiblingNonWhiteSpace()
      } else return true
    } else return true
    if (right == null) return true
    val range = right.node.textRange
    return document.getLineNumber(range.startOffset) == document.getLineNumber(element.textRange.endOffset)
  }

  internal fun scheduleBasicCompletion(context: InsertionContext) {
    context.laterRunnable = Runnable {
      CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(context.project, context.editor)
    }
  }

  internal fun addHCLBlockRequiredProperties(file: PsiFile, editor: Editor, project: Project) {
    val block = PsiTreeUtil.getParentOfType(file.findElementAt(editor.caretModel.offset), HCLBlock::class.java)
    if (block != null) {
      addHCLBlockRequiredProperties(file, project, block)
    }
  }

  fun addHCLBlockRequiredProperties(file: PsiFile, project: Project, block: HCLBlock) {
    val inspection = HCLBlockMissingPropertyInspection()
    var changed: Boolean
    do {
      changed = false
      val holder = ProblemsHolder(InspectionManager.getInstance(project), file, true)
      val visitor = inspection.createVisitor(holder, true)
      if (visitor is HCLElementVisitor) {
        visitor.visitBlock(block)
      }
      for (result in holder.results) {
        val fixes = result.fixes
        if (!fixes.isNullOrEmpty()) {
          changed = true
          fixes.filterIsInstance<AddResourcePropertiesFix>().forEach { it.applyFix(project, result) }
        }
      }
    } while (changed)
  }

  internal fun addArguments(count: Int, editor: Editor) {
    EditorModificationUtil.insertStringAtCaret(editor, StringUtil.repeat(" \"\"", count))
  }

  internal fun addBraces(editor: Editor) {
    EditorModificationUtil.insertStringAtCaret(editor, " {}")
    editor.caretModel.moveToOffset(editor.caretModel.offset - 1)
  }

  internal fun addRequiredProvidersBlock(provider: ProviderType, file: PsiFile) {
    val project = file.project
    val elementGenerator = TerraformElementGenerator(project)
    val terraformBlock = (TypeModel.getTerraformBlock(file)
                          ?: file.addBefore(elementGenerator.createBlock(HCL_TERRAFORM_IDENTIFIER), file.firstChild)) as HCLBlock
    val requiredProvidersBlock = (PsiTreeUtil.findChildrenOfType<HCLBlock>(terraformBlock, HCLBlock::class.java).firstOrNull { RequiredProvidersBlock.accepts(it) }
                                  ?: terraformBlock.`object`?.addBefore(elementGenerator.createBlock(HCL_TERRAFORM_REQUIRED_PROVIDERS), terraformBlock.`object`?.lastChild)) as HCLBlock

    val providerObject = elementGenerator.createObject(mapOf("source" to "\"${provider.fullName}\"", "version" to "\"${provider.version}\""))
    val providerProperty = elementGenerator.createObjectProperty(provider.type, providerObject.text)

    requiredProvidersBlock.`object`?.addBefore(providerProperty, requiredProvidersBlock.`object`?.lastChild)
    CodeStyleManager.getInstance(project).reformatText(file, listOf(terraformBlock.textRange), true)
  }


}