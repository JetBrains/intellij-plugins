// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.javascript.web.css.CssInBindingExpressionCompletionProvider
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeValueCompletionProvider
import org.jetbrains.vuejs.codeInsight.attributes.VueRefValueCompletionProvider
import org.jetbrains.vuejs.lang.expr.VueExprMetaLanguage

class VueCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN),
           VueAttributeValueCompletionProvider())
    extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN),
           VueRefValueCompletionProvider())
    extend(CompletionType.BASIC, psiElement().with(exprLanguage()),
           VueExprCompletionProvider())
    extend(CompletionType.BASIC, psiElement().with(exprLanguage()),
           CssInBindingExpressionCompletionProvider())
    extend(CompletionType.BASIC,
           psiElement(JSTokenTypes.IDENTIFIER)
             .withParent(JSPatterns.jsReferenceExpression().withFirstChild(psiElement(JSThisExpression::class.java))),
           VueThisInstanceCompletionProvider())
  }

  private fun <T : PsiElement> exprLanguage(): PatternCondition<T> {
    return object : PatternCondition<T>("exprlanguage") {
      override fun accepts(t: T, context: ProcessingContext): Boolean {
        return VueExprMetaLanguage.matches(PsiUtilCore.findLanguageFromElement(t))
      }
    }
  }
}

