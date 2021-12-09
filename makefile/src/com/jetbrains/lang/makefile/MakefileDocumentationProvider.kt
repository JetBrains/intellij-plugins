package com.jetbrains.lang.makefile

import com.intellij.lang.documentation.DocumentationProviderEx
import com.intellij.psi.PsiElement
import com.jetbrains.lang.makefile.psi.MakefileTarget
import org.jetbrains.annotations.Nls

class MakefileDocumentationProvider : DocumentationProviderEx() {
  override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): @Nls String? = (element as? MakefileTarget)?.docComment
  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): @Nls String? = (element as? MakefileTarget)?.docComment
}
