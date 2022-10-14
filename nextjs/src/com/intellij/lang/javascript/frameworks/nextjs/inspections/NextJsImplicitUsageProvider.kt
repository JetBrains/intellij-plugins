package com.intellij.lang.javascript.frameworks.nextjs.inspections

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.psi.JSElementBase
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement


val METHODS = setOf("getServerSideProps", "getStaticPaths", "getStaticProps")

class NextJsImplicitUsageProvider : ImplicitUsageProvider {

  override fun isImplicitUsage(element: PsiElement): Boolean {
    return (element is JSElementBase) &&
           (element is JSFunction || element is JSVariable) && element.isExported && METHODS.contains(element.name)
  }

  override fun isImplicitRead(element: PsiElement) = false
  override fun isImplicitWrite(element: PsiElement) = false
}