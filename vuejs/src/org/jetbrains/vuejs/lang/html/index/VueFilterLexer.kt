// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.index

import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.xml.XMLLanguage
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.impl.cache.CacheUtil
import com.intellij.psi.impl.cache.impl.BaseFilterLexer
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import com.intellij.psi.impl.cache.impl.idCache.XmlFilterLexer
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.tree.TokenSet.create
import com.intellij.psi.tree.TokenSet.orSet
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.highlighting.VueSyntaxHighlighterFactory
import kotlin.experimental.or

class VueFilterLexer(occurrenceConsumer: OccurrenceConsumer, originalLexer: Lexer)
  : BaseFilterLexer(originalLexer, occurrenceConsumer) {

  constructor(occurrenceConsumer: OccurrenceConsumer, project: Project?, file: VirtualFile?) :
    this(occurrenceConsumer, getHighlightingLexer(project, file))

  override fun advance() {
    val tokenType = myDelegate.tokenType
    if (!SKIP_WORDS.contains(tokenType)) {
      // This is mix of JSIdAndTodoScanner.JSFilterLexer and XmlFilterLexer
      if (JSKeywordSets.IDENTIFIER_NAMES.contains(tokenType)) {
        addOccurrenceInToken(UsageSearchContext.IN_CODE.toInt())
      }
      else if (XML_IN_PLAIN_TEXT_TOKENS.contains(tokenType)) {
        scanWordsInToken((UsageSearchContext.IN_PLAIN_TEXT or UsageSearchContext.IN_FOREIGN_LANGUAGES).toInt(),
                         tokenType === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN,
                         false)
      }
      else if (tokenType === XmlTokenType.XML_ENTITY_REF_TOKEN || tokenType === XmlTokenType.XML_CHAR_ENTITY_REF) {
        scanWordsInToken(UsageSearchContext.IN_CODE.toInt(), false, false)
      }
      else if (COMMENTS.contains(tokenType)) {
        scanWordsInToken(UsageSearchContext.IN_COMMENTS.toInt(), false, false)
        advanceTodoItemCountsInToken()
      }
      else if (JSTokenTypes.STRING_LITERALS.contains(tokenType) || tokenType === JSTokenTypes.STRING_TEMPLATE_PART) {
        scanWordsInToken((UsageSearchContext.IN_STRINGS or UsageSearchContext.IN_FOREIGN_LANGUAGES).toInt(), true, false)
      }
      else if (tokenType != null && !SUPPORTED_LANGUAGES.contains(tokenType.language)) {
        val inComments = CacheUtil.isInComments(tokenType)
        scanWordsInToken(
          (if (inComments) UsageSearchContext.IN_COMMENTS else UsageSearchContext.IN_PLAIN_TEXT or UsageSearchContext.IN_FOREIGN_LANGUAGES).toInt(),
          true,
          false)

        if (inComments) advanceTodoItemCountsInToken()
      }
      else {
        scanWordsInToken(UsageSearchContext.IN_PLAIN_TEXT.toInt(), false, false)
      }
    }
    myDelegate.advance()
  }

  companion object {

    private fun getHighlightingLexer(project: Project?, file: VirtualFile?): Lexer {
      // We need to choose lexer lang mode, since we don't have access to the indices here.
      // The JavaScript and TypeScript lexer do not produce significantly different results
      // in terms of ID or TO-DO scanning, so we can choose JS here, which is lighter.
      return VueSyntaxHighlighterFactory.getSyntaxHighlighter(project, file, LangMode.NO_TS)
        .highlightingLexer
    }

    private val SUPPORTED_LANGUAGES = ContainerUtil.newHashSet(
      XMLLanguage.INSTANCE,
      HTMLLanguage.INSTANCE,
      VueLanguage.INSTANCE,
      VueJSLanguage.INSTANCE,
      VueTSLanguage.INSTANCE,
      Language.ANY
    )

    private val COMMENTS = orSet(
      JSTokenTypes.COMMENTS,
      create(XmlTokenType.XML_COMMENT_CHARACTERS)
    )

    private val XML_IN_PLAIN_TEXT_TOKENS = create(
      XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN,
      XmlTokenType.XML_NAME,
      XmlTokenType.XML_TAG_NAME,
      XmlTokenType.XML_DATA_CHARACTERS,
      XmlElementType.XML_TEXT,
      XmlElementType.HTML_RAW_TEXT,
      XmlTokenType.XML_TAG_CHARACTERS,
    )

    private val SKIP_WORDS = orSet(
      JSExtendedLanguagesTokenSetProvider.SKIP_WORDS_SCAN_SET,
      XmlFilterLexer.NO_WORDS_TOKEN_SET,
      create(
        XmlTokenType.XML_COMMA
        // VueTokenTypes.ESCAPE_SEQUENCE,
      )
    )
  }
}
