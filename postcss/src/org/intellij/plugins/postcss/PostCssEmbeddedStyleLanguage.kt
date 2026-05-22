// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.intellij.plugins.postcss

import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage

fun styleLanguageOrPostCss(styleLang: String?): Language {
  if (styleLang == null)
    return PostCssLanguage.INSTANCE

  val cssLanguage = CSSLanguage.INSTANCE

  if (styleLang.equals("text/css", ignoreCase = true))
    return cssLanguage

  return cssLanguage.dialects.firstOrNull { dialect ->
    dialect.id.equals(styleLang, ignoreCase = true)
    || dialect.mimeTypes.any { it.equals(styleLang, ignoreCase = true) }
  } ?: PostCssLanguage.INSTANCE
}
