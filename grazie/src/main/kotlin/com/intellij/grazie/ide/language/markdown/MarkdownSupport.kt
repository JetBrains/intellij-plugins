// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.language.markdown

import com.intellij.grazie.grammar.GrammarChecker
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.ide.language.LanguageSupport
import com.intellij.grazie.utils.parents
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class MarkdownSupport : LanguageSupport() {
  companion object {
    private val bulletsIgnoredCategories = listOf(Typo.Category.CASING)
  }

  override fun isRelevant(element: PsiElement) = MarkdownPsiUtils.isHeader(element) || MarkdownPsiUtils.isParagraph(
    element) || MarkdownPsiUtils.isCode(element)

  override fun check(element: PsiElement) = GrammarChecker.default.check(
    element,
    tokenRules = GrammarChecker.TokenRules(
      ignoreByIndex = linkedSetOf(
        { token, index ->
          val elementInToken = token.findElementAt(
            index)!!
          !MarkdownPsiUtils.isText(
            elementInToken) || elementInToken.parents().any {
            MarkdownPsiUtils.isInline(it)
          }
        }
      )
    )).filter { typo ->
    val startElement = element.findElementAt(typo.location.range.start)!!
    val endElement = element.findElementAt(typo.location.range.last)!!

    // Inline elements inside typo
    if (generateSequence(startElement) { PsiTreeUtil.nextLeaf(it) }.takeWhile { it != endElement }
        .any { it.parents().any { MarkdownPsiUtils.isInline(it) } }) return@filter false


    // Inline element right before a typo (TODO add categories filter)
    if (typo.location.isAtStartOfInnerElement(startElement)) {
      generateSequence(PsiTreeUtil.prevLeaf(startElement)) { PsiTreeUtil.prevLeaf(it) }
        .find { !MarkdownPsiUtils.isWhitespace(it) && !MarkdownPsiUtils.isEOL(it) }
        ?.parents()?.any { MarkdownPsiUtils.isInline(it) }
        ?.let { if (it) return@filter false }
    }

    // Inline element right after a typo (TODO add categories filter)
    if (typo.location.isAtEndOfInnerElement(endElement)) {
      generateSequence(PsiTreeUtil.nextLeaf(endElement)) { PsiTreeUtil.nextLeaf(it) }
        .find { !MarkdownPsiUtils.isWhitespace(it) && !MarkdownPsiUtils.isEOL(it) }
        ?.parents()?.any { MarkdownPsiUtils.isInline(it) }
        ?.let { if (it) return@filter false }
    }

    !(typo.isTypoInOuterListItem() && typo.info.category in bulletsIgnoredCategories)
  }.toSet()

  private fun Typo.isTypoInOuterListItem() = this.location.element
                                               ?.parents()
                                               ?.any { element -> MarkdownPsiUtils.isOuterListItem(element) } ?: false
}
