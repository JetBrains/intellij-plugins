package org.jetbrains.vuejs.codeInsight

import com.intellij.psi.PsiElement
import com.intellij.psi.css.resolve.CssInclusionContext
import org.jetbrains.vuejs.VueLanguage

class VueCssInclusionContext : CssInclusionContext() {
  override fun processAllCssFilesOnResolving(context: PsiElement): Boolean {
    return context.containingFile?.language == VueLanguage.INSTANCE
  }
}
