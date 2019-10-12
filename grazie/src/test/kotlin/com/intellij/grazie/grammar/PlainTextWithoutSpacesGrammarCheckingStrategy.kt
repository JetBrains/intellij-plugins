package com.intellij.grazie.grammar

import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy
import com.intellij.grazie.grammar.strategy.impl.RuleGroup
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainText

class PlainTextWithoutSpacesGrammarCheckingStrategy : GrammarCheckingStrategy {
  override fun isMyContextRoot(element: PsiElement) = element is PsiPlainText

  override fun getIgnoredRuleGroup(root: PsiElement, child: PsiElement) = RuleGroup.WHITESPACES
}
