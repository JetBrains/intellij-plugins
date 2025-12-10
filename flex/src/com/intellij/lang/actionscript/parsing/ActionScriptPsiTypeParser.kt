// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptParserBundle
import com.intellij.lang.javascript.parsing.AdvancesLexer
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.Ref
import com.intellij.psi.tree.IElementType

internal class ActionScriptPsiTypeParser(parser: ActionScriptParser) : ActionScriptParserBase(parser) {

  @AdvancesLexer(onlyIfReturnsTrue = true)
  fun parseQualifiedTypeName(): Boolean {
    return parseQualifiedTypeName(false)
  }

  /**
   * @return true if the following code can be parsed as a type (can include nested errors), false otherwise.
   * @apiNote without a type separator
   */
  fun parseType(): Boolean {
    val tokenType = builder.tokenType
    if (JSKeywordSets.PRIMITIVE_TYPES.contains(tokenType)) {
      builder.advanceLexer()
    }
    else if (!parseQualifiedTypeName()) {
      builder.error(getTypeNameExpectedMessage())
    }
    return true
  }

  /**
   * @return true if the following code was parsed as a type (can include nested errors), false otherwise.
   * @apiNote a type separator is expected
   */
  fun tryParseType(): Boolean {
    if (builder.tokenType === JSTokenTypes.COLON) {
      builder.advanceLexer()

      val b = parseType()
      if (b) {
        val tokenType = builder.tokenType
        if (tokenType === JSTokenTypes.QUEST || tokenType === JSTokenTypes.EXCL) {
          builder.advanceLexer()
        }
      }
      return b
    }

    return false
  }

  fun tryParseFunctionReturnType(): Boolean {
    return tryParseType()
  }

  fun tryParseTypeParameterList(): Boolean {
    return true
  }

  @AdvancesLexer(onlyIfReturnsTrue = true)
  fun parseQualifiedTypeName(allowStar: Boolean): Boolean {
    val expr = parseIdentifierPart()
    if (expr == null) return false

    return parseQualifiedTypeNameRest(allowStar, expr)
  }

  private fun parseIdentifierPart(): PsiBuilder.Marker? {
    if (!isIdentifierToken(builder.tokenType)) return null
    val expr = builder.mark()
    parser.buildTokenElement(JSElementTypes.REFERENCE_EXPRESSION)
    return expr
  }

  @AdvancesLexer(onlyIfReturnsTrue = true)
  fun parseQualifiedTypeNameRest(
    allowStar: Boolean,
    expr: PsiBuilder.Marker,
  ): Boolean {
    var expr = expr
    while (isAcceptableQualifierSeparator()) {
      builder.advanceLexer()
      val ref = Ref.create(expr)
      val stop = parseAfterDotInQualifiedTypeNameRest(allowStar, ref)
      expr = ref.get()

      if (stop) break
    }

    return parseQualifiedTypeNameTail(expr)
  }

  private fun isAcceptableQualifierSeparator(): Boolean =
    builder.tokenType === JSTokenTypes.DOT

  private fun parseAfterDotInQualifiedTypeNameRest(
    allowStar: Boolean,
    marker: Ref<PsiBuilder.Marker>,
  ): Boolean {
    val tokenType = builder.tokenType
    val expr = marker.get()

    val stop = parseAfterDotInQualifiedTypeNameRest(allowStar, tokenType)
    expr.done(JSElementTypes.REFERENCE_EXPRESSION)
    marker.set(expr.precede())

    return stop
  }

  private fun parseAfterDotInQualifiedTypeNameRest(
    allowStar: Boolean,
    tokenType: IElementType?,
  ): Boolean {
    if (tokenType === JSTokenTypes.ANY_IDENTIFIER && allowStar) {
      builder.advanceLexer()
      return true
    }
    else if (tokenType !== JSTokenTypes.IDENTIFIER && parser.isIdentifierName(tokenType)) {
      builder.advanceLexer()
    }
    else {
      checkMatches(builder, JSTokenTypes.IDENTIFIER, "javascript.parser.message.expected.name")
    }
    return false
  }

  private fun parseQualifiedTypeNameTail(
    expr: PsiBuilder.Marker,
  ): Boolean {
    if (builder.tokenType === JSTokenTypes.LT) {
      builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.dot"))
      parseECMA4GenericSignature()
      expr.done(JSElementTypes.REFERENCE_EXPRESSION)
    }
    else {
      expr.drop()
    }
    return true
  }

  fun parseECMA4GenericSignature() {
    assert(builder.tokenType === JSTokenTypes.LT || builder.tokenType === JSTokenTypes.GENERIC_SIGNATURE_START)
    val genericTypeSignature = builder.mark()
    builder.advanceLexer()
    parseType()
    checkMatches(builder, JSTokenTypes.GT, "javascript.parser.message.expected.gt")
    genericTypeSignature.done(JSElementTypes.GENERIC_SIGNATURE)
  }

  @NlsContexts.ParsingError
  private fun getTypeNameExpectedMessage(): String =
    JavaScriptParserBundle.message("javascript.parser.message.expected.typename.or.*")
}
