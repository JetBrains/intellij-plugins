// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.codeinsight

import com.intellij.codeInsight.completion.BasicInsertHandler
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.MethodCallExpression
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.hil.HILElementTypes

object FunctionInsertHandler : BasicInsertHandler<LookupElement>() {
  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val editor = context.editor
    val file = context.file

    val project = editor.project
    if (project == null || project.isDisposed) return

    val e = file.findElementAt(context.startOffset) ?: return

    val element = (if (TokenSet.create(HILElementTypes.ID, HCLElementTypes.ID).contains(e.node?.elementType)) {
      e.parent
    } else {
      e
    }) as? Identifier ?: return

    val function = TypeModelProvider.getModel(project).getFunction(item.lookupString) ?: return


    var offset: Int? = null
    val current: Int
    val expected = function.arguments.size
    var addBraces = false
    var place: Int = 0


    // Probably first element in interpolation OR under ILSelectExpression
    val parent = element.parent
    if (parent is SelectExpression<*>) {
      // Prohibited!
      return
    }
    if (parent is MethodCallExpression<*>) {
      // Looks like function name modified
      current = parent.parameterList.elements.size
      if (current != 0) {
        place = parent.parameterList.elements.last().textOffset
      }
    } else {
      current = 0
      addBraces = true
    }

    // TODO check context.completionChar before adding arguments or braces

    if (context.completionChar in " (") {
      context.setAddCompletionChar(false)
    }

    if (addBraces) {
      addBraces(editor, expected)
      editor.caretModel.moveToOffset(editor.caretModel.offset + 1)
      scheduleBasicCompletion(context)
    } else if (current < expected) {
      // TODO: Add some arguments
      //      offset = editor.caretModel.offset + 2
      //      addArguments(expected, editor, place)
      //      scheduleBasicCompletion(context)
    }
    PsiDocumentManager.getInstance(project).commitDocument(editor.document)
    if (offset != null) {
      editor.caretModel.moveToOffset(offset)
    }
  }


  private fun scheduleBasicCompletion(context: InsertionContext) {
    context.laterRunnable = object : Runnable {
      override fun run() {
        if (context.project.isDisposed || context.editor.isDisposed) return
        CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(context.project, context.editor)
      }
    }
  }

  private fun addArguments(count: Int, editor: Editor, place: Int) {
    val offset = editor.caretModel.offset
    editor.caretModel.moveToOffset(place)
    EditorModificationUtil.insertStringAtCaret(editor, "(${StringUtil.repeat(", ", count)})")
    editor.caretModel.moveToOffset(offset)
  }

  private fun addBraces(editor: Editor, expected: Int) {
    EditorModificationUtil.insertStringAtCaret(editor, "(${StringUtil.join((1..expected).map { "" }, ", ")})", false, false)
    //    EditorModificationUtil.insertStringAtCaret(editor, " {}")
    //    editor.caretModel.moveToOffset(editor.caretModel.offset - 1)
  }

}
