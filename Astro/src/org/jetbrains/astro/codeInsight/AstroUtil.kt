// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import com.intellij.util.asSafely
import org.jetbrains.astro.lang.psi.AstroFrontmatterScript
import org.jetbrains.astro.lang.psi.AstroContentRoot

const val ASTRO_PKG = "astro"
const val ASTRO_GLOBAL_INTERFACE = "AstroGlobal"
const val ASTRO_IMPLICIT_OBJECT = "Astro"

fun PsiElement.astroContentRoot(): AstroContentRoot? =
  containingFile?.astroContentRoot()

fun PsiElement.frontmatterScript(): AstroFrontmatterScript? =
  containingFile?.astroContentRoot()?.frontmatterScript()

fun PsiFile.astroContentRoot(): AstroContentRoot? =
  this.childrenOfType<AstroContentRoot>().firstOrNull()

fun AstroContentRoot.frontmatterScript(): AstroFrontmatterScript? =
  children.firstNotNullOfOrNull { it as? AstroFrontmatterScript }

fun AstroFrontmatterScript.propsInterface(): TypeScriptInterface? =
  children.firstNotNullOfOrNull { child -> child.asSafely<TypeScriptInterface>()?.takeIf { it.name == "Props" } }


fun JSPsiNamedElementBase.resolveIfImportSpecifier(): JSPsiNamedElementBase =
  (this as? ES6ImportSpecifier)
    ?.multiResolve(false)
    ?.asSequence()
    ?.mapNotNull { it.takeIf { it.isValidResult }?.element as? JSPsiNamedElementBase }
    ?.firstOrNull()
  ?: this