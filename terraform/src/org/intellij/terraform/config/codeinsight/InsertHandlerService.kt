// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.readAndWriteAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_REQUIRED_PROVIDERS
import org.intellij.terraform.config.inspection.AddResourcePropertiesFix
import org.intellij.terraform.config.inspection.HCLBlockMissingPropertyInspection
import org.intellij.terraform.config.model.ProviderType
import org.intellij.terraform.config.model.TypeModel
import org.intellij.terraform.config.patterns.TerraformPatterns.RequiredProvidersBlock
import org.intellij.terraform.config.psi.TerraformElementGenerator
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLPsiUtil.getNextSiblingNonWhiteSpace
import org.intellij.terraform.hcl.psi.HCLStringLiteral

@Service(Service.Level.PROJECT)
class InsertHandlerService(val project: Project, val coroutineScope: CoroutineScope) {

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
      CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(project, context.editor)
    }
  }

  internal fun addHCLBlockRequiredPropertiesAsync(file: PsiFile, editor: Editor, project: Project): Deferred<Unit?> {
    return coroutineScope.async(Dispatchers.Default) {
      readAction {
        PsiTreeUtil.getParentOfType<HCLBlock>(file.findElementAt(editor.caretModel.offset), HCLBlock::class.java)?.createSmartPointer()
      }?.let { addHCLBlockRequiredProperties(file, project, it) }
      PsiDocumentManager.getInstance(project).commitDocument(editor.document)
    }
  }

  private suspend fun addHCLBlockRequiredProperties(file: PsiFile, project: Project, pointer: SmartPsiElementPointer<HCLBlock>) {
    val inspection = HCLBlockMissingPropertyInspection()
    var hasChanges: Boolean = false
    do {
      val holder = ProblemsHolder(InspectionManager.getInstance(project), file, true)
      val visitor = inspection.createVisitor(holder, true) as HCLElementVisitor
      readAndWriteAction {
        pointer.element?.let { visitor.visitBlock(it) }
        val fixPairs = holder.results.flatMap { inspectionResult ->
          inspectionResult.fixes?.filterIsInstance<AddResourcePropertiesFix>()?.map { fix -> inspectionResult to fix } ?: emptyList()
        }
        hasChanges = fixPairs.isNotEmpty()
        writeCommandAction(project, HCLBundle.message("terraform.add.required.properties.command.name")) {
          fixPairs.forEach {
            it.second.applyFix(project, it.first)
          }
        }
      }
    }
    while (hasChanges)
  }

  internal fun addArguments(count: Int, editor: Editor) {
    EditorModificationUtil.insertStringAtCaret(editor, StringUtil.repeat(" \"\"", count))
  }

  internal fun addBraces(editor: Editor) {
    EditorModificationUtil.insertStringAtCaret(editor, " {}")
    editor.caretModel.moveToOffset(editor.caretModel.offset - 1)
  }

  @RequiresWriteLock
  internal fun addRequiredProvidersBlock(provider: ProviderType, file: PsiFile) {
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