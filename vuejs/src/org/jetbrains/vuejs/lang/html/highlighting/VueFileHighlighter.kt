// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.highlighting

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.XmlHighlighterColors.HTML_CODE
import com.intellij.openapi.editor.XmlHighlighterColors.XML_TAG_DATA
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.Pair.pair
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ArrayUtil
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_END
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_START

internal class VueFileHighlighter(private val languageLevel: JSLanguageLevel,
                                  private val myInterpolationConfig: Pair<String, String>?) : HtmlFileHighlighter() {

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
    keys[tokenType]?.let { return it }
    var result = super.getTokenHighlights(tokenType)
    if (tokenType.language is VueJSLanguage || tokenType.language is JavascriptLanguage) {
      result = ArrayUtil.insert(result, 1, VUE_EXPRESSION)
    }
    return result
  }

  override fun getHighlightingLexer(): Lexer {
    return VueHighlightingLexer(languageLevel, myInterpolationConfig)
  }

  companion object {

    val VUE_INTERPOLATION_DELIMITER = TextAttributesKey.createTextAttributesKey(
      "VUE.SCRIPT_DELIMITERS", DefaultLanguageHighlighterColors.SEMICOLON)

    val VUE_EXPRESSION = TextAttributesKey.createTextAttributesKey(
      "VUE.EXPRESSIONS", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR)

    private val keys = mutableMapOf<IElementType, Array<TextAttributesKey>>()

    private fun put(token: IElementType, vararg keysArr: TextAttributesKey) {
      @Suppress("UNCHECKED_CAST")
      keys[token] = keysArr as Array<TextAttributesKey>
    }

    init {
      listOf(INTERPOLATION_START, INTERPOLATION_END).forEach { token ->
        put(token, HTML_CODE, VUE_EXPRESSION, VUE_INTERPOLATION_DELIMITER)
      }

      JSKeywordSets.AS_RESERVED_WORDS.types.forEach { token ->
        put(token, HTML_CODE, VUE_EXPRESSION, JSHighlighter.JS_KEYWORD)
      }

      put(XmlTokenType.XML_REAL_WHITE_SPACE, HTML_CODE, XML_TAG_DATA)

      listOf(
        //pair(JSTokenTypes.ESCAPE_SEQUENCE, JS_VALID_STRING_ESCAPE),
        //pair(JSTokenTypes.INVALID_ESCAPE_SEQUENCE, JS_INVALID_STRING_ESCAPE),
        //pair(JSTokenTypes.XML_CHAR_ENTITY_REF, XmlHighlighterColors.HTML_ENTITY_REFERENCE),
        pair(JSTokenTypes.STRING_LITERAL_PART, JSHighlighter.JS_STRING)
      ).forEach { p -> put(p.first, HTML_CODE, VUE_EXPRESSION, p.second) }
    }
  }

}
