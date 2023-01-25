// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.lang.DependentLanguage
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader

class AstroLanguage private constructor()
  : JSLanguageDialect("Astro",
                      JavaScriptSupportLoader.TYPESCRIPT_JSX.optionHolder,
                      JavaScriptSupportLoader.TYPESCRIPT_JSX),
    DependentLanguage {

  companion object {
    @JvmField
    val INSTANCE = AstroLanguage()
  }
}
