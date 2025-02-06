// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.lexer

internal interface AstroTokenTypes {
  companion object {

    @JvmField
    val FRONTMATTER_SEPARATOR = AstroTokenType("ASTRO:FRONTMATTER_SEPARATOR")

    @JvmField
    val FRONTMATTER_SCRIPT = AstroTokenType("ASTRO:FRONTMATTER_SCRIPT")

  }
}
