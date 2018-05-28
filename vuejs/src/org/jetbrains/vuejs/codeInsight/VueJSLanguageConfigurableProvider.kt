package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ASTFactory
import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider
import com.intellij.lang.javascript.psi.JSParenthesizedExpression
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil

class VueJSLanguageConfigurableProvider : JSInheritedLanguagesConfigurableProvider() {
  override fun isNeedToBeTerminated(element: PsiElement): Boolean = false
  
  override fun createExpressionFromText(text: String,
                                        element: PsiElement): PsiElement? {
    val created = JSChangeUtil.createJSTreeFromTextWithContext("($text)", element)
    return if (created is JSParenthesizedExpression) created.innerExpression else null
  }

  override fun createNameIdentifierFromText(text: String, context: PsiElement): ASTNode? {
    val node = ASTFactory.leaf(JSTokenTypes.IDENTIFIER, text)
    CodeEditUtil.setNodeGenerated(node, true)
    return node
  }
}