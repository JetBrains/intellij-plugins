// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc.lexer

import com.intellij.psi.xml.XmlTokenType

interface AstroTokenTypes : XmlTokenType {
  companion object {

    @JvmField
    val FRONTMATTER_SEPARATOR = AstroTokenType("ASTRO:FRONTMATTER_SEPARATOR")

    @JvmField
    val FRONTMATTER_SCRIPT = AstroTokenType("ASTRO:FRONTMATTER_SCRIPT")

    @JvmField
    val SHORTHAND_ATTRIBUTE = AstroTokenType("ASTRO:SHORTHAND_ATTRIBUTE")

    @JvmField
    val SPREAD_ATTRIBUTE = AstroTokenType("ASTRO:SPREAD_ATTRIBUTE")

    @JvmField
    val EXPRESSION_ATTRIBUTE = AstroTokenType("ASTRO:EXPRESSION_ATTRIBUTE")

    @JvmField
    val TEMPLATE_LITERAL_ATTRIBUTE = AstroTokenType("ASTRO:TEMPLATE_LITERAL_ATTRIBUTE")

    @JvmField
    val EXPRESSION = AstroTokenType("ASTRO:EXPRESSION")
  }
}
