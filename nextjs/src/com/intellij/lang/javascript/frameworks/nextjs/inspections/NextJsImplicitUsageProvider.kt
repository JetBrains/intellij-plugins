package com.intellij.lang.javascript.frameworks.nextjs.inspections

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.JSElementBase
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import com.intellij.webSymbols.context.WebSymbolsContext


val METHODS = setOf("getServerSideProps", "getStaticPaths", "getStaticProps")

class NextJsImplicitUsageProvider : ImplicitUsageProvider {

  override fun isImplicitUsage(element: PsiElement): Boolean =
    isInNextJsContext(element) && (isKnownMethodName(element) || isInAppDir(element))

  private fun isInNextJsContext(element: PsiElement): Boolean =
    WebSymbolsContext.get("nextjs-project", element) == "nextjs"

  private fun isKnownMethodName(element: PsiElement): Boolean =
    (element is JSElementBase)
      && (element is JSFunction || element is JSVariable)
      && element.isExported
      && METHODS.contains(element.name)

  private fun isInAppDir(element: PsiElement): Boolean =
    PsiUtilCore.getVirtualFile(element)?.let{
      JSLibraryUtil.hasDirectoryInPath(it, "app", null)
    } == true

  override fun isImplicitRead(element: PsiElement) = false
  override fun isImplicitWrite(element: PsiElement) = false
}