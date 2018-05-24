package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider
import com.intellij.lang.javascript.psi.JSParenthesizedExpression
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.psi.PsiElement

class VueJSLanguageConfigurableProvider : JSInheritedLanguagesConfigurableProvider() {
  override fun isNeedToBeTerminated(element: PsiElement) = false
  
  override fun createExpressionFromText(text: String,
                                        element: PsiElement): PsiElement? {
    val created = JSChangeUtil.createJSTreeFromTextWithContext("($text)", element)
    return if (created is JSParenthesizedExpression) created.innerExpression else null
  }

}