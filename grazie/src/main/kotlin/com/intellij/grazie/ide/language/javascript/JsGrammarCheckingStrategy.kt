// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.language.javascript

import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.grammar.strategy.BaseGrammarCheckingStrategy
import com.intellij.grazie.grammar.strategy.impl.ReplaceCharRule
import com.intellij.grazie.grammar.strategy.impl.RuleGroup
import com.intellij.grazie.grammar.strategy.indentIndexes
import com.intellij.lang.javascript.JSDocTokenTypes.*
import com.intellij.lang.javascript.JSTokenTypes.*
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSXmlLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.lang.javascript.psi.jsdoc.JSDocTag
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.xml.XmlText

class JsGrammarCheckingStrategy : BaseGrammarCheckingStrategy {
  override fun isMyContextRoot(element: PsiElement) = element is JSDocComment || element is JSLiteralExpression

  override fun isAbsorb(element: PsiElement) = when {
    element is JSDocTag -> true
    element.parent is JSStringTemplateExpression && element is LeafPsiElement ->
      element.elementType !in listOf(BACKQUOTE, STRING_TEMPLATE_PART, STRING_LITERAL, STRING_LITERAL_PART)
    element.parent is JSStringTemplateExpression && element !is LeafPsiElement -> true
    (element.parent is JSXmlLiteralExpression && element !is JSXmlLiteralExpression) && (element !is XmlText && element !is PsiWhiteSpace) -> true
    else -> false
  }

  override fun isStealth(element: PsiElement) = element is LeafPsiElement && element.elementType in listOf(BACKQUOTE, DOC_COMMENT_START,
                                                                                                           DOC_COMMENT_LEADING_ASTERISK,
                                                                                                           DOC_COMMENT_END)

  override fun getIgnoredRuleGroup(root: PsiElement, child: PsiElement) = RuleGroup.LITERALS.takeIf { root is JSLiteralExpression }

  override fun getIgnoredTypoCategories(root: PsiElement, child: PsiElement) = setOf(Typo.Category.CASING).takeIf { child.parent is JSDocComment }

  override fun getReplaceCharRules(root: PsiElement) = emptyList<ReplaceCharRule>()

  override fun getStealthyRanges(root: PsiElement, text: CharSequence) = indentIndexes(text, setOf(' '))
}
