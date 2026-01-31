// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.lexer

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.Lexer
import com.intellij.lexer.MergeFunction
import com.intellij.lexer.MergingLexerAdapterBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType.XML_DATA_CHARACTERS
import com.intellij.psi.xml.XmlTokenType.XML_REAL_WHITE_SPACE
import org.jetbrains.astro.lang.parser.AstroElementTypes.ASTRO_EMBEDDED_EXPRESSION

class AstroRawTextLexer(highlightingMode: Boolean) : LayeredLexer(
  AstroRawTextMergingLexer(FlexAdapter(_AstroRawTextLexer()))
) {

  init {
    if (highlightingMode) {
      registerSelfStoppingLayer(
        AstroEmbeddedExpressionBraceFixingLexer(JavaScriptHighlightingLexer(DialectOptionHolder.TS)),
        arrayOf(ASTRO_EMBEDDED_EXPRESSION),
        IElementType.EMPTY_ARRAY,
      )
    }
  }

  class AstroEmbeddedExpressionBraceFixingLexer(original: Lexer) : MergingLexerAdapterBase(original) {

    private var startOffset = 0

    override fun getMergeFunction(): MergeFunction {
      return MergeFunction { type, originalLexer -> this.merge(type, originalLexer) }
    }

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
      this.startOffset = startOffset
      super.start(buffer, startOffset, endOffset, initialState)
    }

    private fun merge(type: IElementType?, originalLexer: Lexer): IElementType? =
      when (type) {
        JSTokenTypes.LBRACE if tokenStart == startOffset -> JSTokenTypes.XML_LBRACE
        JSTokenTypes.RBRACE if tokenStart == originalLexer.bufferEnd - 1 -> JSTokenTypes.XML_RBRACE
        else -> type
      }
  }

  private class AstroRawTextMergingLexer(original: FlexAdapter) : MergingLexerAdapterBase(original) {

    override fun getMergeFunction(): MergeFunction {
      return MergeFunction { type, originalLexer -> this.merge(type, originalLexer) }
    }

    private fun merge(type: IElementType?, originalLexer: Lexer): IElementType? {
      if (TOKENS_TO_MERGE.contains(type)) {
        while (true) {
          val nextTokenType = originalLexer.tokenType
          if (nextTokenType !== type) {
            break
          }
          originalLexer.advance()
        }
      }
      return type
    }

    private companion object {
      private val TOKENS_TO_MERGE = TokenSet.create(
        XML_REAL_WHITE_SPACE,
        XML_DATA_CHARACTERS,
        ASTRO_EMBEDDED_EXPRESSION,
      )
    }
  }

}