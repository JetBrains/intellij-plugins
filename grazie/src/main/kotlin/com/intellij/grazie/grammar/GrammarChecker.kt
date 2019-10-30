// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.grammar

import com.intellij.grazie.grammar.ide.GraziePsiElementProcessor
import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy
import com.intellij.grazie.utils.Text
import com.intellij.grazie.utils.length
import com.intellij.grazie.utils.processElements
import com.intellij.grazie.utils.toPointer
import com.intellij.psi.PsiElement
import java.util.*
import kotlin.collections.ArrayList

object GrammarChecker {
  private data class Shift(val start: Int, val length: Int, val totalDeleted: Int)

  fun check(root: PsiElement, strategy: GrammarCheckingStrategy): Set<Typo> {
    val processor = GraziePsiElementProcessor<PsiElement>(root, strategy)
    processElements(root, processor)

    val text = processor.getResultedText()
    val shifts = ArrayList<Shift>()

    var stealthed = 0 // count of newly removed characters from text after getResultedShifts()
    var total = 0
    val iterator = processor.getResultedShifts().listIterator()
    for (range in strategy.getStealthyRanges(root, text).sortedWith(Comparator.comparingInt { it.start })) {
      var deleted = 0
      for ((position, length) in iterator) {
        if (position < range.start) {
          // shift before range
          shifts.add(Shift(position - stealthed, length, total + length))
        } else if (position in range) {
          // shift inside range (combine in one)
          deleted += length
        } else {
          // shift after length - step back
          iterator.previous()
          break
        }

        total += length
      }

      text.delete(range.start - stealthed, range.endInclusive + 1 - stealthed)

      val length = range.length
      total += length

      shifts.add(Shift(range.start - stealthed, deleted + length, total))
      stealthed += length
    }

    // after processing all ranges there still can be shifts
    for ((position, length) in iterator) {
      total += length
      shifts.add(Shift(position, length, total))
    }

    return check(root, text, shifts, processor.getResultedTokens(), strategy)
  }

  private fun check(root: PsiElement, text: StringBuilder, shifts: ArrayList<Shift>,
                    tokens: Collection<GraziePsiElementProcessor.TokenInfo>, strategy: GrammarCheckingStrategy): Set<Typo> {
    if (tokens.isEmpty()) return emptySet()

    var offset = Text.quotesOffset(text)
    text.setLength(text.length - offset) // remove closing quotes and whitespaces
    while (text.isNotEmpty() && text[text.length - 1].isWhitespace()) text.deleteCharAt(text.length - 1)

    while (offset < text.length && text[offset].isWhitespace()) offset++
    repeat(offset) { text.deleteCharAt(0) } // remove opening quotes and whitespace

    return GrammarEngine.getTypos(text.toString(), offset = offset).mapNotNull { typo ->
      val typoRange = typo.location.errorRange

      // finds position in root element for text range
      fun findPosition(position: Int): Int {
        val index = shifts.binarySearch { it.start.compareTo(position + 1) }
        return when {
          index >= 0 -> shifts[index].totalDeleted
          -(index + 1) > 0 -> shifts[-(index + 1) - 1].totalDeleted
          else -> 0
        } + position
      }

      val range = IntRange(findPosition(typoRange.start), findPosition(typoRange.endInclusive))
      val patternRange = IntRange(findPosition(typo.location.patternRange.start), findPosition(typo.location.patternRange.endInclusive))

      val textRangesToDelete = ArrayList<IntRange>()
      var start = range.start
      // take all shifts inside typo and invert them
      shifts.filter { it.start > typoRange.start && it.start <= typoRange.endInclusive }.forEach { shift ->
        textRangesToDelete.add(IntRange(start, shift.start + shift.totalDeleted - shift.length - 1))
        start = shift.start + shift.totalDeleted
      }
      textRangesToDelete.add(IntRange(start, range.endInclusive + 1))

      val typoTokens = tokens.filter { it.range.endInclusive >= range.start && it.range.start <= range.endInclusive }
      check(typoTokens.isNotEmpty()) { "No tokens for range in typo" }

      val category = Typo.Category.values().find { it.name == typo.info.rule.category.id.toString() }
      when {
        !strategy.isTypoAccepted(root, range, patternRange) -> null                                // typo not accepted by strategy
        typoTokens.any { token ->
          token.behavior == GrammarCheckingStrategy.ElementBehavior.ABSORB ||                      // typo pattern in absorb element
            category in token.ignoredCategories ||                                                 // typo rule in ignored category
            typo.info.rule.id in token.ignoredGroup.rules                                          // typo rule in ignored group
        } -> null

        else -> typo.copy(location = typo.location.copy(errorRange = range, textRanges = textRangesToDelete, pointer = root.toPointer()))
      }
    }.toSet()
  }
}
