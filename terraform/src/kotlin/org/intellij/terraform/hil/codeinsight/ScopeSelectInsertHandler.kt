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
