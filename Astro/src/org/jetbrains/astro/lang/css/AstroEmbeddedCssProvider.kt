// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.css

import com.intellij.lang.Language
import com.intellij.psi.css.EmbeddedCssProvider
import org.jetbrains.astro.lang.AstroLanguage


class AstroEmbeddedCssProvider : EmbeddedCssProvider() {
  override fun enableEmbeddedCssFor(language: Language): Boolean = language is AstroLanguage
}
