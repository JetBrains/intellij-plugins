// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.grammar

import com.intellij.grazie.jlanguage.LangDetector
import com.intellij.grazie.jlanguage.LangTool
import com.intellij.grazie.utils.*
import com.intellij.openapi.progress.ProgressManager
import org.slf4j.LoggerFactory

object GrammarEngine {
  private val logger = LoggerFactory.getLogger(GrammarEngine::class.java)

  private const val tooBigChars = 50_000
  private const val maxChars = 10_000
  private const val minChars = 2

  private val separators = listOf('?', '!', '.', ';', ',', '\n', ' ', '\t')

  private const val minNumberOfWords = 3

  fun getTypos(str: String, seps: List<Char> = separators.filter { it in str }, offset: Int = 0): Set<Typo> {
    // FIXME \\s is not useful for chinese/japanese.
    if (str.isBlank() || str.length > tooBigChars || str.split(Regex("\\s+")).size < minNumberOfWords) return emptySet()

    return if (str.length < maxChars) {
      getTyposSmall(str, offset)
    } else {
      val head = seps.firstOrNull() ?: return emptySet()
      val tail = seps.drop(1)

      buildSet {
        str.splitWithRanges(head) { range, sentence ->
          addAll(getTypos(sentence, tail).map {
            Typo(it.location.withOffset(range.start + offset), it.info, it.fixes)
          })

          ProgressManager.checkCanceled()
        }
      }
    }
  }

  private fun getTyposSmall(str: String, offset: Int = 0): LinkedSet<Typo> {
    if (str.length < minChars) return LinkedSet()

    val lang = LangDetector.getLang(str) ?: return LinkedSet()

    return try {
      LangTool.getTool(lang).check(str)
        .orEmpty()
        .asSequence()
        .filterNotNull()
        .map { Typo(it, lang, offset) }
        .toCollection(LinkedSet())
    } catch (e: Throwable) {
      logger.warn("Got exception during check for typos by LanguageTool", e)
      LinkedSet()
    }
  }
}
