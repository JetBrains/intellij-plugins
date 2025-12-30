// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import icons.AngularIcons
import org.angular2.lang.expr.psi.Angular2BlockParameter

object Angular2HtmlBlockReferenceExpressionCompletionProvider {
  fun addCompletions(result: CompletionResultSet, ref: JSReferenceExpressionImpl): Boolean {
    // the "of" keyword in @for primary expression
    if (addOfKeyword(ref)) {
      result.addElement(LookupElementBuilder.create("of")
                          .withIcon(AngularIcons.Angular2)
                          .withInsertHandler(Angular2BlockKeywordInsertHandler))
      result.stopHere()
      return true
    }
    // the "=" in @for "let" parameter
    else if (addEq(ref)) {
      result.addElement(LookupElementBuilder.create("=")
                          .withIcon(AngularIcons.Angular2)
                          .withInsertHandler(Angular2BlockKeywordInsertHandler))
      result.stopHere()
      return true
    }
    return false
  }

  fun canAddCompletions(ref: JSReferenceExpression): Boolean =
    addOfKeyword(ref) || addEq(ref)

  private fun addOfKeyword(ref: JSReferenceExpression) =
    ref.parent.let { it is Angular2BlockParameter && it.isPrimaryExpression && it.block?.getName() == BLOCK_FOR }
    && ref.siblings(false, false)
      .filter { it.elementType != TokenType.WHITE_SPACE && it !is PsiErrorElement }.firstOrNull() is JSVarStatement

  private fun addEq(ref: JSReferenceExpression) =
    isJSReferenceInForBlockLetParameterAssignment(ref)
    && ref.siblings(false, false)
      .filter { it.elementType != TokenType.WHITE_SPACE && it !is PsiErrorElement }
      .firstOrNull()
      ?.elementType == JSTokenTypes.IDENTIFIER
}