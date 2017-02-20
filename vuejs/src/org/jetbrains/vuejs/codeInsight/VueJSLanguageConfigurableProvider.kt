package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider
import com.intellij.psi.PsiElement

class VueJSLanguageConfigurableProvider : JSInheritedLanguagesConfigurableProvider() {
  override fun isNeedToBeTerminated(element: PsiElement) = false
}