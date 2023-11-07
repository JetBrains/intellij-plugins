// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.inspections

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.psi.PsiElement
import org.jetbrains.astro.codeInsight.ASTRO_CONFIG_FILES

class AstroImplicitUsageProvider : ImplicitUsageProvider {
  override fun isImplicitUsage(element: PsiElement) = checkAstroConfig(element)

  override fun isImplicitRead(element: PsiElement) = false

  override fun isImplicitWrite(element: PsiElement) = false

  private fun checkAstroConfig(element: PsiElement) =
    ASTRO_CONFIG_FILES.contains(element.containingFile.name) &&
    element is ES6ExportDefaultAssignment
}