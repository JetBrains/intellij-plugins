// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.frontmatter

import com.intellij.lang.DependentLanguage
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader

class AstroFrontmatterLanguage private constructor()
  : JSLanguageDialect("AstroFrontmatter",
                      JavaScriptSupportLoader.TYPESCRIPT.optionHolder,
                      JavaScriptSupportLoader.TYPESCRIPT),
    DependentLanguage {

  companion object {
    @JvmField
    val INSTANCE = AstroFrontmatterLanguage()
  }
}