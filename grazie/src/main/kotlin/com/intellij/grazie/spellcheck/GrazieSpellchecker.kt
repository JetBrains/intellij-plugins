// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.spellcheck

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.jlanguage.Lang
import com.intellij.grazie.jlanguage.LangTool
import com.intellij.grazie.utils.LinkedSet
import com.intellij.grazie.utils.toLinkedSet
import com.intellij.spellchecker.engine.Transformation
import org.languagetool.JLanguageTool
import org.languagetool.rules.Rule
import org.slf4j.LoggerFactory

object GrazieSpellchecker {
  private const val MAX_SUGGESTIONS_COUNT = 3

  private val BASE_SPELLCHECKER_LANGUAGE = Lang.AMERICAN_ENGLISH
  private val logger = LoggerFactory.getLogger(GrazieSpellchecker::class.java)

  private val transform = Transformation()

  data class SpellerTool(val tool: JLanguageTool, val speller: Rule, val suggestLimit: Int) {
    fun isMyDomain(word: String): Boolean {
      val domain = Lang[tool.language]!!.unicodeBlock.blocks

      var offset = 0
      while (offset < word.length) {
        val codepoint = Character.codePointAt(word, offset)

        if (Character.UnicodeBlock.of(codepoint) in domain) return true

        offset += Character.charCount(codepoint)
      }

      return false
    }

    fun check(word: String): Boolean = synchronized(speller) {
      val match = speller.match(tool.getRawAnalyzedSentence(word))
      //If match array is empty then word is correct in that language
      match.isEmpty() || match.flatMap { it.suggestedReplacements }.any { transform.transform(it) == word }
    }

    fun suggest(text: String): Set<String> = synchronized(speller) {
      speller.match(tool.getRawAnalyzedSentence(text)).flatMap { it.suggestedReplacements }.take(suggestLimit).toSet()
    }
  }

  private val checkers: LinkedSet<SpellerTool>
    get() = GrazieConfig.get().availableLanguages.plus(BASE_SPELLCHECKER_LANGUAGE).mapNotNull { lang ->
      val rule = LangTool.getSpeller(lang)
      rule?.let { SpellerTool(LangTool.getTool(lang), rule, MAX_SUGGESTIONS_COUNT) }
    }.toLinkedSet()

  fun isCorrect(word: String) = checkers.filter { it.isMyDomain(word) }.let {
    if (it.isEmpty()) true else it.any { speller ->
      try {
        speller.check(word)
      } catch (t: Throwable) {
        logger.warn("Got exception during check for spelling mistakes by LanguageTool with word: $word", t)
        false
      }
    }
  }

  /**
   * Checks text for spelling mistakes.
   */
  fun getSuggestions(word: String) = checkers.filter { it.isMyDomain(word) }.mapNotNull { speller ->
    try {
      speller.suggest(word)
    } catch (t: Throwable) {
      logger.warn("Got exception during suggest for spelling mistakes by LanguageTool with word: $word", t)
      null
    }
  }.flatten().toLinkedSet()
}
