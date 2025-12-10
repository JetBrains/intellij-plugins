// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderUtil
import com.intellij.lang.WhitespacesAndCommentsBinder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptParserBundle
import com.intellij.lang.javascript.JavaScriptParserBundle.BUNDLE
import com.intellij.lang.javascript.parsing.LineTerminators
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.PropertyKey

open class ActionScriptParserBase(val parser: ActionScriptParser) {

  protected val builder: PsiBuilder
    get() = parser.builder

  protected fun isIdentifierToken(tokenType: IElementType?): Boolean {
    if (tokenType === JSTokenTypes.IDENTIFIER || tokenType === JSTokenTypes.PRIVATE_IDENTIFIER)
      return true // fast path

    return parser.isIdentifierToken(tokenType)
  }

  protected fun getTokenCharSequence(): CharSequence? =
    if (builder.eof()) null else PsiBuilderUtil.rawTokenText(builder, 0)

  private class JSDocBinder(
    private val allowExtraLineBreak: Boolean,
  ) : WhitespacesAndCommentsBinder {

    override fun getEdgePosition(
      tokens: List<IElementType>,
      atStreamEdge: Boolean,
      getter: WhitespacesAndCommentsBinder.TokenTextGetter,
    ): Int {
      var i = tokens.size - 1

      var type = if (i >= 0) tokens[i] else null
      while (type === JSTokenTypes.WHITE_SPACE || type === JSTokenTypes.END_OF_LINE_COMMENT) {
        if (!allowExtraLineBreak && type === JSTokenTypes.WHITE_SPACE) {
          val tokenText = getter[i]
          if (tokenText.count { it == '\n' } > 1) {
            break // if there're two newlines, the further comments shouldn't be attached
          }
        }
        --i
        type = if (i >= 0) tokens[i] else null
      }

      if (type === JSElementTypes.DOC_COMMENT)
        return i

      return tokens.size
    }
  }

  companion object {

    @JvmField
    var MAX_TREE_DEPTH: Int = 100 // not final for being able to test

    @JvmField
    // protected
    val INCLUDE_DOC_COMMENT_AT_LEFT: WhitespacesAndCommentsBinder =
      JSDocBinder(allowExtraLineBreak = true)

    @JvmField
    // protected
    val INCLUDE_DOC_COMMENT_AT_LEFT_NO_EXTRA_LINEBREAK: WhitespacesAndCommentsBinder =
      JSDocBinder(allowExtraLineBreak = false)

    @JvmStatic
    fun hasLineTerminatorBefore(builder: PsiBuilder): Boolean {
      return LineTerminators.hasLineTerminatorBefore(builder)
    }

    @JvmStatic
    fun hasLineTerminatorAfter(builder: PsiBuilder): Boolean {
      return LineTerminators.hasLineTerminatorAfter(builder)
    }

    @JvmStatic
    fun checkMatches(
      builder: PsiBuilder,
      token: IElementType,
      @PropertyKey(resourceBundle = BUNDLE)
      errorMessageKey: String,
    ): Boolean {
      if (builder.tokenType === token) {
        builder.advanceLexer()
        return true
      }
      else {
        builder.error(JavaScriptParserBundle.message(errorMessageKey))
        return false
      }
    }
  }
}
