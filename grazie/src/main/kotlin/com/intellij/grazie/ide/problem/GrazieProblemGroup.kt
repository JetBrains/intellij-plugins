package com.intellij.grazie.ide.problem

import com.intellij.codeInspection.SuppressableProblemGroup
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.ide.GrazieInspection
import com.intellij.grazie.ide.problem.suppress.GrazieDisableCategoryIntention
import com.intellij.grazie.ide.problem.suppress.GrazieDisableContextIntention
import com.intellij.grazie.ide.problem.suppress.GrazieDisableRuleIntention
import com.intellij.psi.PsiElement

class GrazieProblemGroup(val fix: Typo): SuppressableProblemGroup {
  override fun getProblemName(): String = GrazieInspection.id

  override fun getSuppressActions(element: PsiElement?) = arrayOf(
    GrazieDisableCategoryIntention(fix),
    GrazieDisableRuleIntention(fix),
    GrazieDisableContextIntention(fix)
  )
}