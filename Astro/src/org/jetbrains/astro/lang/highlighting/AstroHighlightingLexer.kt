// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.highlighting

import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.astro.lang.lexer.AstroLexerImpl
import org.jetbrains.astro.lang.lexer.AstroLexerImpl.Companion.HAS_NON_RESTARTABLE_STATE
import org.jetbrains.astro.lang.lexer.AstroTokenTypes
import org.jetbrains.astro.lang.lexer._AstroLexer

class AstroHighlightingLexer(styleFileType: FileType?)
  : HtmlHighlightingLexer(AstroHighlightingMergingLexer(AstroLexerImpl.AstroFlexAdapter(true, false)), true, styleFileType) {

  private var frontmatterScriptLexer: Lexer? = null

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    if (initialState and HAS_NON_RESTARTABLE_STATE != 0) {
      thisLogger().error(IllegalStateException("Do not reset Astro Lexer to a non-restartable state"))
    }
    frontmatterScriptLexer = null
    super.start(buffer, startOffset, endOffset, initialState)
  }

  override fun isPossiblyCustomTagName(tagName: CharSequence): Boolean {
    return AstroLexerImpl.isPossiblyComponentTag(tagName)
  }

  override fun getState(): Int {
    return super.getState() or (if (frontmatterScriptLexer != null) HAS_NON_RESTARTABLE_STATE else 0)
  }

  override fun isRestartableState(state: Int): Boolean {
    return super.isRestartableState(state)
           && (state and HAS_NON_RESTARTABLE_STATE) == 0
  }

  override fun isHtmlTagState(state: Int): Boolean {
    return state == _AstroLexer.START_TAG_NAME || state == _AstroLexer.END_TAG_NAME
  }

  override fun createTagEmbedmentStartTokenSet(): TokenSet {
    return TokenSet.orSet(super.createTagEmbedmentStartTokenSet(), AstroLexerImpl.TAG_TOKENS)
  }

  override fun advance() {
    frontmatterScriptLexer?.let {
      it.advance()
      if (it.tokenType == null) {
        frontmatterScriptLexer = null
      }
    }

    if (frontmatterScriptLexer == null) {
      super.advance()
      if (myDelegate.tokenType === AstroTokenTypes.FRONTMATTER_SCRIPT) {
        frontmatterScriptLexer = JSFlexAdapter(JavaScriptSupportLoader.TYPESCRIPT.optionHolder, true, false)
          .also {
            it.start(myDelegate.bufferSequence, myDelegate.tokenStart, myDelegate.tokenEnd)
          }
      }
    }
  }

  override fun getTokenType(): IElementType? {
    return frontmatterScriptLexer?.tokenType ?: super.getTokenType()
  }

  override fun getTokenStart(): Int {
    return frontmatterScriptLexer?.tokenStart ?: super.getTokenStart()
  }

  override fun getTokenEnd(): Int {
    return frontmatterScriptLexer?.tokenEnd ?: super.getTokenEnd()
  }


  private class AstroHighlightingMergingLexer(original: FlexAdapter) : AstroLexerImpl.AstroMergingLexer(original) {
    override fun merge(type: IElementType?, originalLexer: Lexer): IElementType? {
      val result = super.merge(type, originalLexer)
      if (result === XmlTokenType.XML_CHAR_ENTITY_REF) {
        while (originalLexer.tokenType === XmlTokenType.XML_CHAR_ENTITY_REF) {
          originalLexer.advance()
        }
        if (originalLexer.tokenType === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) {
          return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
        }
      }
      return result
    }
  }

}