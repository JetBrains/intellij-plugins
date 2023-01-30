// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.refactoring

import com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionHandler
import com.intellij.psi.PsiElement
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.lang.psi.AstroContentRoot
import org.jetbrains.astro.lang.psi.AstroFrontmatterScript
import org.jetbrains.astro.lang.psi.AstroHtmlTag

class AstroExtractFunctionHandler : JSExtractFunctionHandler() {

  override fun findBase(at: PsiElement, findAll: Boolean): IntroductionScope? {
    if (at is AstroHtmlTag || at is AstroContentRoot) {
      val astroFile = at.containingFile as? AstroFileImpl ?: return null
      return createPairWithPresentation(astroFile.astroContentRoot()!!)
    }
    return super.findBase(at, findAll)
  }

  override fun createPairWithPresentation(parent: PsiElement?): IntroductionScope? {
    return super.createPairWithPresentation(parent)?.let {
      if (it.parent is AstroFrontmatterScript || it.parent is AstroContentRoot)
      // We need to point to Frontmatter parent, to be able to anchor at a next token after frontmatter content
        IntroductionScope(it.parent, "frontmatter", it.forceMakeFunExpr)
      else
        it
    }
  }
}