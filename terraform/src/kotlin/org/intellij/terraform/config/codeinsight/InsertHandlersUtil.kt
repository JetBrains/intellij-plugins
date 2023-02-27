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
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.inspection.AddResourcePropertiesFix
import org.intellij.terraform.config.inspection.HCLBlockMissingPropertyInspection

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
        if (fixes != null && fixes.isNotEmpty()) {
          changed = true
          fixes.filterIsInstance<AddResourcePropertiesFix>().forEach { it.applyFix(project, result) }
        }
      }
    } while (changed)
  }

  internal fun addSpace(editor: Editor) {
    EditorModificationUtil.insertStringAtCaret(editor, " ")
  }

  internal fun addArguments(count: Int, editor: Editor) {
    EditorModificationUtil.insertStringAtCaret(editor, StringUtil.repeat(" \"\"", count))
  }

  internal fun addBraces(editor: Editor) {
    EditorModificationUtil.insertStringAtCaret(editor, " {}")
    editor.caretModel.moveToOffset(editor.caretModel.offset - 1)
  }
}