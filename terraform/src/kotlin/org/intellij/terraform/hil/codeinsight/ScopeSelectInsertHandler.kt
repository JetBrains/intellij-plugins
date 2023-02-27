// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.codeinsight

import com.intellij.codeInsight.completion.BasicInsertHandler
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.intellij.terraform.hil.HILElementTypes
import org.intellij.terraform.hil.psi.ILSelectExpression
import org.intellij.terraform.hil.psi.ILVariable

object ScopeSelectInsertHandler : BasicInsertHandler<LookupElement>() {
  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val editor = context.editor
    val file = context.file

    val project = editor.project
    if (project == null || project.isDisposed) return

    val e = file.findElementAt(context.startOffset) ?: return

    val element: PsiElement?
    if (e.node?.elementType == HILElementTypes.ID) {
      element = e.parent
    } else {
      element = e
    }
    if (element !is ILVariable) return

    val parent = element.parent
    if (parent is ILSelectExpression) {
      // Already select expression
      return
    }

    if (context.completionChar in " .") {
      context.setAddCompletionChar(false)
    }
    EditorModificationUtil.insertStringAtCaret(editor, ".")
    scheduleBasicCompletion(context)
    PsiDocumentManager.getInstance(project).commitDocument(editor.document)
  }

  private fun scheduleBasicCompletion(context: InsertionContext) {
    context.laterRunnable = Runnable {
      if (context.project.isDisposed || context.editor.isDisposed) return@Runnable
      CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(context.project, context.editor)
    }
  }

}
