// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.util.PsiTreeUtil.getContextOfType
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.psi.VueJSSlotPropsExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import org.jetbrains.vuejs.lang.html.VueFileType

class VueJSLanguageConfigurableProvider : JSInheritedLanguagesConfigurableProvider() {
  override fun isNeedToBeTerminated(element: PsiElement): Boolean = false

  override fun createParameterOrVariableItem(destruct: String, parent: PsiElement): PsiElement? {
    if (parent.language !is VueJSLanguage || parent !is JSElement) return null
    val paramsListOrVarStmt = getContextOfType(parent, false, JSVarStatement::class.java, JSParameterList::class.java)
    return when (paramsListOrVarStmt?.context) {
      is VueJSSlotPropsExpression -> "<li #slot=\"$destruct\">"
      is VueJSVForExpression -> "<li v-for=\"$destruct in schedules\">"
      else -> null
    }?.let { text ->
      val file = PsiFileFactory.getInstance(parent.project).createFileFromText("q.vue", VueFileType.INSTANCE, text)
      SyntaxTraverser.psiTraverser(file).filter(JSInitializerOwner::class.java).first()
    }
  }
}
