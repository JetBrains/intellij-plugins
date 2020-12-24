package com.jetbrains.lang.makefile

import com.intellij.lang.documentation.DocumentationProviderEx
import com.intellij.psi.PsiElement
import com.jetbrains.lang.makefile.psi.MakefileTarget

class MakefileDocumentationProvider : DocumentationProviderEx() {
  override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?) = (element as? MakefileTarget)?.docComment
  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?) = (element as? MakefileTarget)?.docComment
}