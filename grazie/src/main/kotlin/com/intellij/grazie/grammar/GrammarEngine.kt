// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.grammar

import com.intellij.grazie.jlanguage.LangDetector
import com.intellij.grazie.jlanguage.LangTool
import com.intellij.grazie.utils.LinkedSet
import com.intellij.grazie.utils.splitWithRanges
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.text.StringUtil
import org.slf4j.LoggerFactory

object GrammarEngine {
  private val logger = LoggerFactory.getLogger(GrammarEngine::class.java)

  private const val tooBigChars = 50_000
  private const val maxChars = 10_000
  private const val minChars = 2

  private const val minNumberOfWords = 3

  private fun isGrammarCheckUseless(str: String): Boolean {
    return str.isBlank() || str.length > tooBigChars
           || StringUtil.getWordIndicesIn(str).size < minNumberOfWords // FIXME getWordIndicesIn is not useful for chinese/japanese.
  }

  private fun String.collectSentencesForGrammarCheck(consumer: (IntRange, String) -> Unit) {
    splitWithRanges('?', '!', '.', ';') { range, sentence ->
      ProgressManager.checkCanceled()
      if (!isGrammarCheckUseless(sentence)) consumer(range, sentence)
    }
  }

  private fun String.collectSentencePartsForGrammarCheck(consumer: (IntRange, String) -> Unit) {
    splitWithRanges('\n', ',') { range, part ->
      if (!isGrammarCheckUseless(part) && part.length < maxChars) consumer(range, part)
    }
  }

  fun getTypos(text: String, offset: Int = 0): Set<Typo> {
    if (isGrammarCheckUseless(text)) return emptySet()

    if (text.length < maxChars) return getTyposSmall(text, offset)

    return LinkedSet<Typo>().apply {
      text.collectSentencesForGrammarCheck { rangeInText, sentence ->
        if (sentence.length < maxChars) {
          addAll(getTyposSmall(sentence, rangeInText.start + offset))
        } else sentence.collectSentencePartsForGrammarCheck { rangeInSentence, part ->
          addAll(getTyposSmall(part, rangeInText.start + rangeInSentence.start + offset))
        }
      }
    }
  }

  private fun getTyposSmall(str: String, offset: Int = 0): Set<Typo> {
    ProgressManager.checkCanceled()
    if (str.length < minChars) return emptySet()

    val lang = LangDetector.getLang(str) ?: return emptySet()

    return try {
      LangTool.getTool(lang).check(str)
        .orEmpty()
        .asSequence()
        .filterNotNull()
        .map { Typo(it, lang, offset) }
        .toCollection(LinkedSet())
    } catch (e: Throwable) {
      logger.warn("Got exception during check for typos by LanguageTool", e)
      emptySet()
    }
  }
}
