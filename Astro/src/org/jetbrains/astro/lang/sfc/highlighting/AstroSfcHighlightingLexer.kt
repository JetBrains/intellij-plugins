// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc.highlighting

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.astro.lang.sfc.lexer.AstroSfcLexerImpl
import org.jetbrains.astro.lang.sfc.lexer.AstroSfcLexerImpl.Companion.HAS_NON_RESTARTABLE_STATE
import org.jetbrains.astro.lang.sfc.lexer._AstroSfcLexer

class AstroSfcHighlightingLexer(styleFileType: FileType?)
  : HtmlHighlightingLexer(AstroSfcHighlightingMergingLexer(AstroSfcLexerImpl.AstroFlexAdapter()), true, styleFileType) {

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    if (initialState and HAS_NON_RESTARTABLE_STATE != 0) {
      thisLogger().error(IllegalStateException("Do not reset Astro Lexer to a non-restartable state"))
    }
    super.start(buffer, startOffset, endOffset, initialState)
  }

  override fun isRestartableState(state: Int): Boolean {
    return super.isRestartableState(state)
           && (state and HAS_NON_RESTARTABLE_STATE) == 0
  }

  override fun isHtmlTagState(state: Int): Boolean {
    return state == _AstroSfcLexer.START_TAG_NAME || state == _AstroSfcLexer.END_TAG_NAME
  }

  override fun createTagEmbedmentStartTokenSet(): TokenSet {
    return TokenSet.orSet(super.createTagEmbedmentStartTokenSet(), AstroSfcLexerImpl.TAG_TOKENS)
  }

  override fun getTokenType(): IElementType? {
    val tokenType = super.getTokenType()
    /* val state = state
     // we need to convert attribute names according to their function
     if (tokenType === XmlTokenType.XML_NAME && state and BASE_STATE_MASK == _Angular2HtmlLexer.TAG_ATTRIBUTES) {
       val info = Angular2AttributeNameParser.parse(tokenText)
       if (info.type != Angular2AttributeType.REGULAR
           && Angular2HtmlEmbeddedContentSupport.Holder.NG_EL_ATTRIBUTES.contains(info.type)) {
         return info.type.elementType
       }
     }
     else if (tokenType != null && Angular2HtmlMergingLexer.isLexerWithinExpansionForm(state)) {
       if (tokenType === XmlTokenType.TAG_WHITE_SPACE
           || tokenType === XmlTokenType.XML_REAL_WHITE_SPACE
           || tokenType === JSTokenTypes.IDENTIFIER
           || tokenType === XmlTokenType.XML_DATA_CHARACTERS) {
         return EXPANSION_FORM_CONTENT
       }
       else if (tokenType === XmlTokenType.XML_COMMA) {
         return EXPANSION_FORM_COMMA
       }
     }
     else if ((tokenType === XmlTokenType.TAG_WHITE_SPACE && Angular2HtmlMergingLexer.isLexerWithinInterpolation(state))
              || (tokenType === XmlTokenType.XML_WHITE_SPACE && embeddedLexer is JavaScriptHighlightingLexer)) {
       return EXPRESSION_WHITE_SPACE
     }
     else if (tokenType === XmlTokenType.TAG_WHITE_SPACE
              && (Angular2HtmlMergingLexer.getBaseLexerState(state) == 0
                  || Angular2HtmlMergingLexer.isLexerWithinUnterminatedInterpolation(state))) {
       return XmlTokenType.XML_REAL_WHITE_SPACE
     }
     else if (tokenType === JSTokenTypes.STRING_LITERAL_PART) {
       return JSTokenTypes.STRING_LITERAL
     }*/
    return tokenType
  }

  private class AstroSfcHighlightingMergingLexer(original: FlexAdapter) : AstroSfcLexerImpl.AstroMergingLexer(original) {
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