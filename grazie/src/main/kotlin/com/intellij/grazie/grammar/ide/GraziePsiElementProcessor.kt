// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.grammar.ide

import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy
import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy.ElementBehavior.*
import com.intellij.grazie.grammar.strategy.impl.RuleGroup
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainText
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.PsiElementProcessor
import java.util.*
import kotlin.collections.ArrayList

class GraziePsiElementProcessor<T : PsiElement>(private val root: PsiElement,
                                                private val strategy: GrammarCheckingStrategy) : PsiElementProcessor<T> {
  data class TokenInfo(val token: PsiElement, val behavior: GrammarCheckingStrategy.ElementBehavior,
                       val ignoredGroup: RuleGroup, val ignoredCategories: Set<Typo.Category>)

  private val collection = Collections.synchronizedCollection(ArrayList<TokenInfo>())
  private var pointers = IdentityHashMap<PsiElement, TokenInfo>()

  override fun execute(element: T): Boolean {
    if (element !== root && strategy.isMyContextRoot(element)) return false

    val behavior = when (strategy.getElementBehavior(root, element)) {
      TEXT -> pointers[element.parent]?.behavior ?: TEXT
      STEALTH -> STEALTH
      ABSORB -> return false
    }

    val group = strategy.getIgnoredRuleGroup(root, element) ?: (pointers[element.parent]?.ignoredGroup ?: RuleGroup.EMPTY)
    val categories = strategy.getIgnoredTypoCategories(root, element) ?: (pointers[element.parent]?.ignoredCategories ?: emptySet())

    val info = TokenInfo(element, behavior, group, categories)
    pointers[element] = info

    if (element is LeafPsiElement || element is PsiPlainText) {
      collection.add(info)
    }

    return true
  }

  fun getResult(): Collection<TokenInfo> = collection
}
