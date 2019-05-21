// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ASTFactory
import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSDestructuringElement
import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider
import com.intellij.lang.javascript.psi.JSParenthesizedExpression
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.language.VueJSLanguage

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

  override fun createDestructuringElement(destruct: String, parent: PsiElement): JSDestructuringElement? {
    if (parent.language !is VueJSLanguage) return null
    val file = PsiFileFactory.getInstance(parent.project).createFileFromText("q.vue", VueFileType.INSTANCE,
                                                                             "<li v-for=\"$destruct in schedules\">")
    return SyntaxTraverser.psiTraverser(file).filter(JSDestructuringElement::class.java).first()
  }
}
