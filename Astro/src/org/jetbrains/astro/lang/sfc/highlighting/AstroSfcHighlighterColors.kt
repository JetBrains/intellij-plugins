// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object AstroSfcHighlighterColors {

  @JvmField
  val ASTRO_FRONTMATTER = TextAttributesKey.createTextAttributesKey(
    "ASTRO.FRONTMATTER", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR)

  @JvmField
  val ASTRO_FRONTMATTER_SEPARATOR = TextAttributesKey.createTextAttributesKey(
    "ASTRO.FRONTMATTER_SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD)

}