// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.css

import com.intellij.psi.PsiElement
import com.intellij.psi.css.resolve.CssInclusionContext
import org.jetbrains.astro.lang.AstroLanguage

class AstroCssInclusionContext : CssInclusionContext() {
  override fun processAllCssFilesOnResolving(context: PsiElement): Boolean =
    context.containingFile?.language === AstroLanguage.INSTANCE
}
