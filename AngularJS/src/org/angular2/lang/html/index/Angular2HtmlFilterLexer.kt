// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.index

import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.xml.XMLLanguage
import com.intellij.lexer.Lexer
import com.intellij.psi.impl.cache.CacheUtil
import com.intellij.psi.impl.cache.impl.BaseFilterLexer
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import com.intellij.psi.impl.cache.impl.idCache.XmlFilterLexer
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.highlighting.Angular2HtmlHighlightingLexer
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.svg.Angular2SvgLanguage

class Angular2HtmlFilterLexer(occurrenceConsumer: OccurrenceConsumer, originalLexer: Lexer)
  : BaseFilterLexer(originalLexer, occurrenceConsumer) {
  override fun advance() {
    val tokenType = myDelegate.tokenType
    if (!SKIP_WORDS.contains(tokenType)) {
      if (IDENTIFIERS.contains(tokenType)) {
        addOccurrenceInToken(UsageSearchContext.IN_CODE.toInt())
      }
      else if (tokenType === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
               || tokenType === XmlTokenType.XML_NAME
               || tokenType === XmlTokenType.XML_TAG_NAME
               || tokenType === XmlTokenType.XML_DATA_CHARACTERS) {
        scanWordsInToken(UsageSearchContext.IN_PLAIN_TEXT.toInt() or UsageSearchContext.IN_FOREIGN_LANGUAGES.toInt(),
                         tokenType === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN,
                         false)
      }
      else if (COMMENTS.contains(tokenType)) {
        scanWordsInToken(UsageSearchContext.IN_COMMENTS.toInt(), false, false)
        advanceTodoItemCountsInToken()
      }
      else if (LITERALS.contains(tokenType)) {
        scanWordsInToken(UsageSearchContext.IN_STRINGS.toInt(), false, false)
      }
      else if (Angular2HtmlElementTypes.ALL_ATTRIBUTES.contains(tokenType)) {
        val info = Angular2AttributeNameParser.parse(myDelegate.tokenText)
        if (info.type != Angular2AttributeType.REGULAR) {
          addOccurrenceInToken(UsageSearchContext.IN_CODE.toInt(), tokenText.lastIndexOf(info.name), info.name.length)
        }
      }
      else if (tokenType != null
               && !SUPPORTED_LANGUAGES.contains(tokenType.language)) {
        val inComments = CacheUtil.isInComments(tokenType)
        val mask = if (inComments)
          UsageSearchContext.IN_COMMENTS.toInt()
        else
          UsageSearchContext.IN_PLAIN_TEXT.toInt() or UsageSearchContext.IN_FOREIGN_LANGUAGES.toInt()
        scanWordsInToken(mask, true, false)
        if (inComments) advanceTodoItemCountsInToken()
      }
      else {
        scanWordsInToken(UsageSearchContext.IN_PLAIN_TEXT.toInt(), false, false)
      }
    }
    myDelegate.advance()
  }

  companion object {
    private val SUPPORTED_LANGUAGES: Set<Language> = setOf(
      XMLLanguage.INSTANCE,
      HTMLLanguage.INSTANCE,
      Angular2HtmlLanguage.INSTANCE,
      Angular2SvgLanguage.INSTANCE,
      Angular2Language.INSTANCE,
      Language.ANY
    )
    private val IDENTIFIERS = TokenSet.orSet(
      JSKeywordSets.IDENTIFIER_NAMES
    )
    private val COMMENTS = TokenSet.orSet(
      JSTokenTypes.COMMENTS,
      TokenSet.create(XmlTokenType.XML_COMMENT_CHARACTERS)
    )
    private val LITERALS = TokenSet.orSet(
      JSTokenTypes.LITERALS
    )
    private val SKIP_WORDS = TokenSet.orSet(
      JSExtendedLanguagesTokenSetProvider.SKIP_WORDS_SCAN_SET,
      XmlFilterLexer.NO_WORDS_TOKEN_SET,
      TokenSet.create(
        Angular2HtmlTokenTypes.INTERPOLATION_START,
        Angular2HtmlTokenTypes.INTERPOLATION_END,
        Angular2HtmlTokenTypes.EXPANSION_FORM_START,
        Angular2HtmlTokenTypes.EXPANSION_FORM_END,
        Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_END,
        Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_START,
        XmlTokenType.XML_COMMA,
        Angular2TokenTypes.ESCAPE_SEQUENCE,
        Angular2HtmlHighlightingLexer.EXPANSION_FORM_COMMA,
        Angular2HtmlHighlightingLexer.EXPRESSION_WHITE_SPACE
      )
    )
  }
}