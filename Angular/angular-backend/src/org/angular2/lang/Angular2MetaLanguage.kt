// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang

import com.intellij.lang.Language
import com.intellij.lang.MetaLanguage
import com.intellij.lang.javascript.JavaScriptSupportLoader
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.html.Angular2HtmlDialect

/**
 * Meta-language covering Angular2Language (expression language) Angular2HtmlLanguage (template language) and TypeScript.
 * This can be used for inspections and other language-specific features that should apply to all Angular contexts.
 */
class Angular2MetaLanguage private constructor() : MetaLanguage("Angular2Languages") {

  private val angular2Languages: Set<Language> by lazy {
    getRegisteredLanguages().filterTo(mutableSetOf()) {
      it is Angular2ExprDialect || it is Angular2HtmlDialect || it == JavaScriptSupportLoader.TYPESCRIPT
    }
  }

  override fun matchesLanguage(language: Language): Boolean =
    angular2Languages.contains(language)

  override fun getMatchingLanguages(): Collection<Language> =
    angular2Languages
}
