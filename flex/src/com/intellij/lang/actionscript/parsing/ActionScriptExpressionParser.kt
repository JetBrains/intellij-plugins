// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.WhitespacesBinders
import com.intellij.lang.actionscript.ActionScriptInternalElementTypes
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptParserBundle.message
import com.intellij.lang.javascript.parsing.ExpressionParser
import com.intellij.psi.tree.IElementType

/**
 * @author Konstantin.Ulitin
 */
class ActionScriptExpressionParser internal constructor(parser: ActionScriptParser) : ExpressionParser<ActionScriptParser>(parser) {
  override fun isPropertyStart(elementType: IElementType?): Boolean {
    return JSKeywordSets.AS_IDENTIFIER_TOKENS_SET.contains(elementType) || elementType === JSTokenTypes.STRING_LITERAL || elementType === JSTokenTypes.NUMERIC_LITERAL || elementType === JSTokenTypes.LPAR
  }

  override fun isPropertyNameStart(elementType: IElementType?): Boolean {
    return JSKeywordSets.PROPERTY_NAMES.contains(elementType)
  }

  override fun isFunctionPropertyStart(builder: PsiBuilder): Boolean {
    return JSKeywordSets.PROPERTY_NAMES.contains(builder.getTokenType()) && builder.lookAhead(1) === JSTokenTypes.LPAR
  }

  override fun parsePropertyNoMarker(property: PsiBuilder.Marker): Boolean {
    if (builder.getTokenType() === JSTokenTypes.LPAR) {
      parseParenthesizedExpression()
      parsePropertyInitializer(false)
      property.done(JSElementTypes.PROPERTY)
      property.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
      return true
    }

    return super.parsePropertyNoMarker(property)
  }

  override fun isReferenceQualifierSeparator(tokenType: IElementType?): Boolean {
    return tokenType === JSTokenTypes.DOT || tokenType === JSTokenTypes.COLON_COLON || tokenType === JSTokenTypes.DOT_DOT
  }

  override fun parsePrimaryExpression(): Boolean {
    if (builder.getTokenType() === JSTokenTypes.AT) {
      val attrReferenceStartMarker = builder.mark()

      builder.advanceLexer()
      var possibleNamespaceStartMarker: PsiBuilder.Marker? = builder.mark()

      if (!builder.eof()) {
        val tokenType = builder.getTokenType()
        if (tokenType === JSTokenTypes.ANY_IDENTIFIER ||
            isIdentifierToken(tokenType)
        ) {
          builder.advanceLexer()

          if (builder.getTokenType() === JSTokenTypes.COLON_COLON) {
            possibleNamespaceStartMarker!!.done(JSElementTypes.REFERENCE_EXPRESSION)
            possibleNamespaceStartMarker = possibleNamespaceStartMarker.precede()
            proceedWithNamespaceReference(possibleNamespaceStartMarker, true)

            possibleNamespaceStartMarker = null
          }
        }
        else if (tokenType === JSTokenTypes.LBRACKET) {
          builder.advanceLexer()
          parseExpression()
          checkMatches(builder, JSTokenTypes.RBRACKET, "javascript.parser.message.expected.rbracket")
        }
        else {
          builder.error(message("javascript.parser.message.expected.identifier"))
        }
      }

      possibleNamespaceStartMarker?.drop()
      attrReferenceStartMarker.done(JSElementTypes.REFERENCE_EXPRESSION)

      return true
    }

    return super.parsePrimaryExpression()
  }

  override fun getCurrentBinarySignPriority(allowIn: Boolean, advance: Boolean): Int {
    val tokenType = builder.getTokenType()
    if (tokenType === JSTokenTypes.IS_KEYWORD || tokenType === JSTokenTypes.AS_KEYWORD) {
      if (advance) builder.advanceLexer()
      return 10
    }
    return super.getCurrentBinarySignPriority(allowIn, advance)
  }

  override fun parseAfterReferenceQualifierSeparator(expr: PsiBuilder.Marker): Boolean {
    if (builder.getTokenType() !== JSTokenTypes.LPAR) return false
    val requestedArgumentListMarker = builder.mark()
    parseArgumentListNoMarker()
    requestedArgumentListMarker.done(ActionScriptInternalElementTypes.E4X_FILTER_QUERY_ARGUMENT_LIST)
    expr.done(JSElementTypes.CALL_EXPRESSION)
    return true
  }
}
