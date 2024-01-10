// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr

import com.intellij.lang.DependentLanguage
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.dialects.JSLanguageFeature

class Angular2Language private constructor() : JSLanguageDialect("Angular2", Angular2Dialect()), DependentLanguage {
  override fun isAtLeast(other: JSLanguageDialect): Boolean {
    return super.isAtLeast(other) || JavaScriptSupportLoader.TYPESCRIPT.isAtLeast(other)
  }

  private class Angular2Dialect : DialectOptionHolder("ANGULAR2", true) {
    override fun defineFeatures(): Set<JSLanguageFeature> {
      return setOf(JSLanguageFeature.IMPORT_DECLARATIONS)
    }
  }

  companion object {
    @JvmField
    val INSTANCE = Angular2Language()
  }
}
