// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.Pair
import com.intellij.psi.tree.IElementType
import com.intellij.util.ArrayUtil
import com.intellij.util.containers.map2Array
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.highlighting.Angular2HighlighterColors
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.lexer.Angular2HtmlLexer
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class Angular2HtmlFileHighlighter(private val templateSyntax: Angular2TemplateSyntax,
                                  private val interpolationConfig: Pair<String, String>?) : HtmlFileHighlighter() {
  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
    var result = keys[tokenType]
    if (result != null) {
      return result
    }
    result = super.getTokenHighlights(tokenType)
    if (tokenType.language is Angular2Language
        || tokenType.language is JavascriptLanguage) {
      result = ArrayUtil.insert(result, 1, Angular2HtmlHighlighterColors.NG_EXPRESSION)
    }
    return mapToTsKeys(result, tokenType)
  }

  override fun getHighlightingLexer(): Lexer {
    return Angular2HtmlLexer(true, templateSyntax, interpolationConfig)
  }

  companion object {
    private val keys: MutableMap<IElementType, Array<TextAttributesKey>> = HashMap()
    private val ourJsHighlighter = JSHighlighter(DialectOptionHolder.JS_1_5)
    private val ourTsHighlighter = TypeScriptHighlighter()
    private val ourTsKeyMap: MutableMap<Pair<TextAttributesKey, IElementType>, TextAttributesKey> = ConcurrentHashMap()
    private fun put(token: IElementType, vararg keysArr: TextAttributesKey) {
      keys[token] = keysArr.toList().toTypedArray()
    }

    init {
      for (token in sequenceOf(Angular2HtmlTokenTypes.INTERPOLATION_START, Angular2HtmlTokenTypes.INTERPOLATION_END)) {
        put(token, XmlHighlighterColors.HTML_CODE,
            Angular2HtmlHighlighterColors.NG_EXPRESSION, Angular2HtmlHighlighterColors.NG_INTERPOLATION_DELIMITER)
      }
      for (token in sequenceOf(Angular2HtmlTokenTypes.EXPANSION_FORM_START,
                               Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_START,
                               Angular2HtmlTokenTypes.EXPANSION_FORM_END,
                               Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_END)) {
        put(token, XmlHighlighterColors.HTML_CODE,
            Angular2HtmlHighlighterColors.NG_EXPANSION_FORM, Angular2HtmlHighlighterColors.NG_EXPANSION_FORM_DELIMITER)
      }

      put(Angular2HtmlTokenTypes.EXPANSION_FORM_CONTENT,
          XmlHighlighterColors.HTML_CODE, Angular2HtmlHighlighterColors.NG_EXPANSION_FORM)

      put(Angular2HtmlTokenTypes.EXPANSION_FORM_COMMA,
          XmlHighlighterColors.HTML_CODE, Angular2HtmlHighlighterColors.NG_EXPANSION_FORM,
          Angular2HtmlHighlighterColors.NG_EXPANSION_FORM_COMMA)

      for (p in sequenceOf(
        Pair(Angular2HtmlElementTypes.BANANA_BOX_BINDING,
             Angular2HtmlHighlighterColors.NG_BANANA_BINDING_ATTR_NAME),
        Pair(Angular2HtmlElementTypes.EVENT,
             Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME),
        Pair(Angular2HtmlElementTypes.PROPERTY_BINDING,
             Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME),
        Pair(Angular2HtmlElementTypes.REFERENCE,
             Angular2HighlighterColors.NG_VARIABLE),
        Pair(Angular2HtmlElementTypes.TEMPLATE_BINDINGS,
             Angular2HtmlHighlighterColors.NG_TEMPLATE_BINDINGS_ATTR_NAME),
        Pair(Angular2HtmlElementTypes.LET,
             Angular2HtmlHighlighterColors.NG_TEMPLATE_LET_ATTR_NAME))) {
        put(p.first, XmlHighlighterColors.HTML_CODE, XmlHighlighterColors.HTML_TAG, XmlHighlighterColors.HTML_ATTRIBUTE_NAME, p.second)
      }

      for (token in Angular2TokenTypes.KEYWORDS.types) {
        put(token, XmlHighlighterColors.HTML_CODE,
            Angular2HtmlHighlighterColors.NG_EXPRESSION, TypeScriptHighlighter.TS_KEYWORD)
      }

      for (p in sequenceOf(
        Pair(Angular2TokenTypes.ESCAPE_SEQUENCE, TypeScriptHighlighter.TS_VALID_STRING_ESCAPE),
        Pair(Angular2TokenTypes.INVALID_ESCAPE_SEQUENCE, TypeScriptHighlighter.TS_INVALID_STRING_ESCAPE),
        Pair(Angular2TokenTypes.XML_CHAR_ENTITY_REF, XmlHighlighterColors.HTML_ENTITY_REFERENCE),
        Pair(JSTokenTypes.STRING_LITERAL_PART, TypeScriptHighlighter.TS_STRING)
      )) {
        put(p.first, XmlHighlighterColors.HTML_CODE, Angular2HtmlHighlighterColors.NG_EXPRESSION, p.second)
      }

      for (token in sequenceOf(Angular2HtmlTokenTypes.BLOCK_START, Angular2HtmlTokenTypes.BLOCK_END)) {
        put(token, XmlHighlighterColors.HTML_CODE, Angular2HtmlHighlighterColors.NG_BLOCK_BRACES)
      }

      put(Angular2HtmlTokenTypes.BLOCK_NAME, XmlHighlighterColors.HTML_CODE, Angular2HtmlHighlighterColors.NG_BLOCK_NAME)
      put(Angular2HtmlTokenTypes.BLOCK_SEMICOLON, XmlHighlighterColors.HTML_CODE, Angular2HtmlHighlighterColors.NG_EXPRESSION,
          TypeScriptHighlighter.TS_SEMICOLON)

    }

    private fun mapToTsKeys(tokenHighlights: Array<TextAttributesKey>, tokenType: IElementType): Array<TextAttributesKey> {
      return tokenHighlights.map2Array { key -> getTsMappedKey(key, tokenType) }
    }

    private fun getTsMappedKey(key: TextAttributesKey, tokenType: IElementType): TextAttributesKey {
      return if (!key.externalName.startsWith("JS."))
        key
      else
        ourTsKeyMap.computeIfAbsent(Pair(key, tokenType)) { p: Pair<TextAttributesKey, IElementType> ->
          val jsHighlights = ourJsHighlighter.getTokenHighlights(p.second)
          val tsHighlights = ourTsHighlighter.getTokenHighlights(p.second)
          val jsKey = jsHighlights.lastOrNull()
          val tsKey = tsHighlights.lastOrNull()
          if (jsKey === p.first && tsKey != null) tsKey else p.first
        }
    }
  }
}