// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc

import com.intellij.lang.DependentLanguage
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.dialects.JSLanguageFeature

class AstroSfcLanguage private constructor() : JSLanguageDialect("Astro SFC", AstroJsxDialect()), DependentLanguage {
  override fun isAtLeast(other: JSLanguageDialect): Boolean {
    return super.isAtLeast(other) || JavaScriptSupportLoader.TYPESCRIPT_JSX.isAtLeast(other)
  }

  private class AstroJsxDialect : DialectOptionHolder("ASTRO_SFC", true) {

    override fun defineFeatures(): Set<JSLanguageFeature> {
      return setOf(
        JSLanguageFeature.E4X,
        JSLanguageFeature.YIELD_GENERATORS,
        JSLanguageFeature.DESTRUCTURING_PARAMETERS,
        JSLanguageFeature.ACCESSORS,
        JSLanguageFeature.ARROW_FUNCTIONS,
        JSLanguageFeature.GENERICS,
        JSLanguageFeature.ANNOTATIONS,
        JSLanguageFeature.ASYNC_AWAIT,
        JSLanguageFeature.SHORTHAND_PROPERTY_NAMES,
        JSLanguageFeature.COMPUTED_PROPERTY_NAMES,
        JSLanguageFeature.METHOD_DEFINITION_SHORTHANDS,
        JSLanguageFeature.BINARY_AND_OCTAL_LITERALS,
        JSLanguageFeature.STRING_TEMPLATES,
        JSLanguageFeature.PARAMETER_INITIALIZERS,
        JSLanguageFeature.TYPES,
        JSLanguageFeature.INTERFACES,
        JSLanguageFeature.TRAILING_FUNCTION_COMMA,
        JSLanguageFeature.OPTIONAL_CATCH_BINDING,
        JSLanguageFeature.BIG_INT,
        JSLanguageFeature.PRIVATE_SHARP_SYNTAX,
        JSLanguageFeature.NULLISH_COALESCING,
      )
    }
  }

  companion object {
    @JvmField
    val INSTANCE = AstroSfcLanguage()
  }
}
