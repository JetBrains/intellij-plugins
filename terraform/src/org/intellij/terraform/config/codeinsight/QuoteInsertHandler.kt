// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.BasicInsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.psi.PsiDocumentManager
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.HCLStringLiteral

object QuoteInsertHandler : BasicInsertHandler<LookupElement>() {
  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val editor = context.editor
    val file = context.file
    val element = file.findElementAt(context.startOffset)
    var c: Char? = null
    if (element is HCLStringLiteral) {
      when (element.node.firstChildNode.elementType) {
        HCLElementTypes.SINGLE_QUOTED_STRING -> c = '\''
        HCLElementTypes.DOUBLE_QUOTED_STRING -> c = '\"'
      }
    } else {
      when (element?.node?.elementType) {
        HCLElementTypes.SINGLE_QUOTED_STRING -> c = '\''
        HCLElementTypes.DOUBLE_QUOTED_STRING -> c = '\"'
      }
    }
    if (c == null) return
    if (context.completionChar == c) return
    val project = editor.project
    if (project == null || project.isDisposed) return

    if (!isCharAtCaret(editor, c)) {
      EditorModificationUtil.insertStringAtCaret(editor, "$c")
      PsiDocumentManager.getInstance(project).commitDocument(editor.document)
    } else {
      editor.caretModel.moveToOffset(editor.caretModel.offset + 1)
    }
  }

  private fun isCharAtCaret(editor: Editor, c: Char): Boolean {
    val startOffset = editor.caretModel.offset
    val document = editor.document
    return document.textLength > startOffset && document.charsSequence[startOffset] == c
  }
}
