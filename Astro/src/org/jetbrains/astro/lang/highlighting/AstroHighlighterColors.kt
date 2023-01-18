// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object AstroHighlighterColors {

  @JvmField
  val ASTRO_FRONTMATTER = TextAttributesKey.createTextAttributesKey(
    "ASTRO.FRONTMATTER", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR)

  @JvmField
  val ASTRO_FRONTMATTER_SEPARATOR = TextAttributesKey.createTextAttributesKey(
    "ASTRO.FRONTMATTER_SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD)

}