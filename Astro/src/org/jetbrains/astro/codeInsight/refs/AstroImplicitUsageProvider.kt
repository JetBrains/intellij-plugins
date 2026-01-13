// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight.refs

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifierAlias
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.psi.PsiElement
import org.jetbrains.astro.codeInsight.ASTRO_PROPS
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.codeInsight.propsInterfaceOrTypeAlias
import org.jetbrains.astro.lang.AstroFileImpl

class AstroImplicitUsageProvider : ImplicitUsageProvider {
  override fun isImplicitUsage(element: PsiElement): Boolean {
    if (element.containingFile !is AstroFileImpl) return false

    return isAstroPropsInterface(element) ||
           isAstroPropsImport(element) ||
           isAstroImplicitlyUsedFunction(element) ||
           isAstroImplicitlyUsedExport(element)
  }

  override fun isImplicitRead(element: PsiElement): Boolean = false

  override fun isImplicitWrite(element: PsiElement): Boolean = false

  private fun isAstroPropsInterface(element: PsiElement) =
    element == element.astroContentRoot()?.frontmatterScript()?.propsInterfaceOrTypeAlias()

  private fun isAstroPropsImport(element: PsiElement) =
    when (element) {
      is ES6ImportSpecifier -> element.name == ASTRO_PROPS
      is ES6ImportSpecifierAlias -> element.name == ASTRO_PROPS
      else -> false
    }

  private fun isAstroImplicitlyUsedFunction(element: PsiElement) =
    when (element) {
      is TypeScriptFunction -> element.name.equals("getStaticPaths")
      is TypeScriptVariable -> element.name.equals("getStaticPaths")
      else -> false
    }

  private fun isAstroImplicitlyUsedExport(element: PsiElement): Boolean {
    if (element !is TypeScriptVariable) return false
    val name = element.name ?: return false
    return name == "prerender" || name == "partial"
  }
}