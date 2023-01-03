// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.Pair
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.lexer.Angular2HtmlEmbeddedContentSupport
import org.angular2.lang.html.lexer.Angular2HtmlLexer.Angular2HtmlMergingLexer
import org.angular2.lang.html.lexer._Angular2HtmlLexer
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType

class Angular2HtmlHighlightingLexer(tokenizeExpansionForms: Boolean,
                                    interpolationConfig: Pair<String, String>?,
                                    styleFileType: FileType?)
  : HtmlHighlightingLexer(
  Angular2HtmlHighlightingMergingLexer(FlexAdapter(_Angular2HtmlLexer(tokenizeExpansionForms, interpolationConfig))),
  true, styleFileType) {
  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    super.start(buffer, startOffset, endOffset, (delegate as Angular2HtmlMergingLexer).initExpansionFormNestingLevel(initialState))
  }

  override fun getState(): Int {
    return super.getState() or (delegate as Angular2HtmlMergingLexer).expansionFormNestingLevelState
  }

  override fun isHtmlTagState(state: Int): Boolean {
    return state == _Angular2HtmlLexer.START_TAG_NAME || state == _Angular2HtmlLexer.END_TAG_NAME
  }

  override fun getTokenType(): IElementType? {
    val tokenType = super.getTokenType()
    val state = state
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
    }
    return tokenType
  }

  private class Angular2HtmlHighlightingMergingLexer constructor(original: FlexAdapter) : Angular2HtmlMergingLexer(original) {
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

  companion object {
    @JvmStatic
    val EXPRESSION_WHITE_SPACE: IElementType = IElementType("NG:EXPRESSION_WHITE_SPACE", Angular2Language.INSTANCE)

    @JvmStatic
    val EXPANSION_FORM_CONTENT: IElementType = IElementType("NG:EXPANSION_FORM_CONTENT", Angular2HtmlLanguage.INSTANCE)

    @JvmStatic
    val EXPANSION_FORM_COMMA: IElementType = IElementType("NG:EXPANSION_FORM_COMMA", Angular2HtmlLanguage.INSTANCE)
  }
}