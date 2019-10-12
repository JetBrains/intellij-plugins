// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.grammar

import com.intellij.grazie.grammar.ide.GraziePsiElementProcessor
import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy
import com.intellij.grazie.utils.Text
import com.intellij.grazie.utils.orTrue
import com.intellij.grazie.utils.processElements
import com.intellij.grazie.utils.toPointer
import com.intellij.psi.PsiElement
import java.rmi.UnexpectedException
import java.util.*
import kotlin.collections.ArrayList

object GrammarChecker {
  private data class Token(val info: GraziePsiElementProcessor.TokenInfo, val range: IntRange, val shift: Int)

  private fun canIgnoreWhitespace(prefix: CharSequence, current: Char) = prefix.lastOrNull()?.isWhitespace().orTrue() && current.isWhitespace()

  fun check(root: PsiElement, strategy: GrammarCheckingStrategy): Set<Typo> {
    val processor = GraziePsiElementProcessor<PsiElement>(root, strategy)
    processElements(root, processor)
    return check(root, processor.getResult(), strategy)
  }

  private fun check(root: PsiElement, tokens: Collection<GraziePsiElementProcessor.TokenInfo>, strategy: GrammarCheckingStrategy): Set<Typo> {
    if (tokens.isEmpty()) return emptySet()

    // ranges of absorbed elements
    val absorbs = LinkedList<IntRange>().apply {
      var last = 0
      tokens.forEach {
        val begin = it.token.textOffset - root.textOffset
        if (last != begin) add(IntRange(last, begin - 1))
        last = begin + it.token.textLength
      }
    }

    // tokens with IntRange mappings from text
    val mappings = ArrayList<Token>(tokens.size)
    val replaces = strategy.getReplaceCharRules(root)
    var offset = 0
    val text = buildString {
      var index = 0
      for ((info, text) in tokens.filter { it.behavior != GrammarCheckingStrategy.ElementBehavior.STEALTH }.map { it to it.token.text }) {
        var tokenIndex = 0 // index inside current token
        var shift = 0      // shift inside current token
        text.asSequence().map { replaces.fold(it) { acc, rule -> rule(this, acc) } }.forEach { char ->
          // shift whitespaces in the beginning of the tokens
          if (tokenIndex <= 1 && canIgnoreWhitespace(this, char)) {
            shift++
          }
          else {
            append(char)
            tokenIndex++
          }
        }

        mappings.add(Token(info, IntRange(index, index + tokenIndex), shift))
        index += tokenIndex
      }

      offset = Text.quotesOffset(this)
      setLength(length - offset) // remove closing quotes and whitespaces
      while (length > 0 && get(length - 1).isWhitespace()) deleteCharAt(length - 1)

      while (offset < length && get(offset).isWhitespace()) offset++
      repeat(offset) { deleteCharAt(0) } // remove opening quotes and whitespace
    }

    return GrammarEngine.getTypos(text, offset = offset).mapNotNull { typo ->
      val typoRange = typo.location.errorRange
      val typoTokens = mappings.filter { it.range.endInclusive > typoRange.start && it.range.start <= typoRange.endInclusive }

      if (typoTokens.isEmpty()) throw UnexpectedException("No tokens for range in typo")

      val patternRange = with(typo.location.patternRange) {
        val (startToken, endToken) = mappings.filter { it.range.endInclusive > start && it.range.start <= endInclusive }.let { it.first() to it.last() }
        IntRange(startToken.info.token.textOffset - root.textOffset + start - startToken.range.start + startToken.shift,
                 endToken.info.token.textOffset - root.textOffset + endInclusive - endToken.range.start + endToken.shift)
      }

      val (startToken, endToken) = typoTokens.first() to typoTokens.last()
      // calculate new typo range in the root element
      val newRange = IntRange(startToken.info.token.textOffset - root.textOffset + typoRange.start - startToken.range.start + startToken.shift,
        endToken.info.token.textOffset - root.textOffset + typoRange.endInclusive - endToken.range.start + endToken.shift)

      val ignoredRules = typoTokens.fold(emptySet<String>()) { acc, token -> token.info.ignoredGroup.rules + acc }
      val ignoredCategories = typoTokens.fold(emptySet<Typo.Category>()) { acc, token -> token.info.ignoredCategories + acc }.map { it.name }

      when {
        absorbs.any { patternRange.start in it || patternRange.endInclusive in it || it.start in patternRange } -> null // typo pattern in absorb element
        !strategy.isTypoAccepted(root, newRange, patternRange) -> null                                                  // typo not accepted by strategy
        typo.info.rule.category.id.toString() in ignoredCategories -> null                                              // typo rule in ignored category
        typo.info.rule.id in ignoredRules -> null                                                                       // typo rule in ignored group

        else -> typo.copy(location = typo.location.copy(errorRange = newRange, pointer = root.toPointer()))
      }
    }.toSet()
  }
}
