// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

object Angular2BlockKeywordInsertHandler : InsertHandler<LookupElement> {
  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val document = context.document
    val insertOffset = context.tailOffset
    if (document.textLength == insertOffset || document.charsSequence[insertOffset] != ' ') {
      document.insertString(insertOffset, " ")
    }
    context.editor.caretModel.moveToOffset(insertOffset + 1)
    AutoPopupController.getInstance(context.project).scheduleAutoPopup(context.editor)
  }
}