// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.grammar.ide

import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy
import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy.ElementBehavior.ABSORB
import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy.ElementBehavior.TEXT
import com.intellij.grazie.grammar.strategy.impl.RuleGroup
import com.intellij.grazie.utils.Text
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainText
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.PsiElementProcessor
import java.util.*
import kotlin.collections.ArrayList

class GraziePsiElementProcessor<T : PsiElement>(private val root: PsiElement, private val strategy: GrammarCheckingStrategy) : PsiElementProcessor<T> {
  data class TokenInfo(val range: IntRange,
                       val behavior: GrammarCheckingStrategy.ElementBehavior,
                       val ignoredGroup: RuleGroup,
                       val ignoredCategories: Set<Typo.Category>)

  private val tokens = Collections.synchronizedCollection(ArrayList<TokenInfo>())
  private val shifts = ArrayList<Pair<Int, Int>>()
  private var pointers = IdentityHashMap<PsiElement, TokenInfo>()
  private val text = StringBuilder()
  private var lastNonTextTokenShiftIndex = -1

  private val replaces = strategy.getReplaceCharRules(root)

  // convert double spaces into one after removing absorb/stealth elements
  private fun StringBuilder.deleteNotStealthySpace(position: Int): Boolean {
    if (position in 1 until length) {
      if (get(position - 1) == ' ' && (Text.isPunctuation(get(position)) || get(position) == ' ')) {
        deleteCharAt(position - 1)
        return true
      }
    }

    return false
  }

  override fun execute(element: T): Boolean {
    // prevent nested context roots
    if (element !== root && strategy.isMyContextRoot(element)) {
      tokens.add(TokenInfo(IntRange(element.textOffset - root.textOffset, element.textOffset - root.textOffset + element.textLength - 1), ABSORB, RuleGroup.EMPTY, emptySet()))
      return false
    }

    val behavior = strategy.getElementBehavior(root, element)
    val group = strategy.getIgnoredRuleGroup(root, element) ?: (pointers[element.parent]?.ignoredGroup ?: RuleGroup.EMPTY)
    val categories = strategy.getIgnoredTypoCategories(root, element) ?: (pointers[element.parent]?.ignoredCategories ?: emptySet())

    val info = TokenInfo(IntRange(element.textOffset - root.textOffset, element.textOffset - root.textOffset + element.textLength - 1), behavior, group, categories)
    pointers[element] = info

    if (behavior == TEXT) {
      if (element is LeafPsiElement || element is PsiPlainText) {
        tokens.add(info)

        val position = text.length + 1

        if (replaces.isEmpty()) {
          text.append(element.text)
        } else {
          element.text.forEach {
            text.append(replaces.fold(it) { acc, rule -> rule(text, acc) })
          }
        }

        if (lastNonTextTokenShiftIndex != -1) {
          if (text.deleteNotStealthySpace(position)) {
            val shift = shifts[lastNonTextTokenShiftIndex]
            shifts[lastNonTextTokenShiftIndex] = shift.first - 1 to shift.second + 1
          }

          lastNonTextTokenShiftIndex = -1
        }
      }
    } else {
      tokens.add(info)
      shifts.add(text.length to element.textLength)

      if (lastNonTextTokenShiftIndex == -1) {
        lastNonTextTokenShiftIndex = shifts.size - 1
      }
    }

    // no need to process elements with ignored text
    return behavior == TEXT
  }

  fun getResultedTokens(): Collection<TokenInfo> = tokens
  fun getResultedShifts(): ArrayList<Pair<Int, Int>> = shifts
  fun getResultedText() = text
}
