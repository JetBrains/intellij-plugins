// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.codeInsight.editorActions.SelectWordUtil
import com.intellij.lexer.StringLiteralLexer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hil.HILElementTypes

private class HCLHILStringLiteralSelectionHandler : ExtendWordSelectionHandlerBase() {
  override fun canSelect(e: PsiElement): Boolean {
    val type = e.node.elementType
    return type === HCLElementTypes.SINGLE_QUOTED_STRING
           || type === HCLElementTypes.DOUBLE_QUOTED_STRING
           || type === HILElementTypes.DOUBLE_QUOTED_STRING
  }

  override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange> {
    val type = e.node.elementType
    // Should be the same as in `canSelect`
    val quoteChar = if (type === HCLElementTypes.SINGLE_QUOTED_STRING) '\''
    else if (type === HCLElementTypes.DOUBLE_QUOTED_STRING) '\"'
    else if (type === HILElementTypes.DOUBLE_QUOTED_STRING) '\"'
    else return emptyList()

    val lexer = StringLiteralLexer(quoteChar, type, false, "/", false, false)
    val result: MutableList<TextRange> = ArrayList()
    SelectWordUtil.addWordHonoringEscapeSequences(editorText, e.textRange, cursorOffset, lexer, result)

    val parent = e.parent
    result.add(ElementManipulators.getValueTextRange(parent).shiftRight(parent.textOffset))
    return result
  }
}
