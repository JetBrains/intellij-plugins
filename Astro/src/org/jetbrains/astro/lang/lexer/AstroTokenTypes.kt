// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.lexer

import com.intellij.psi.xml.XmlTokenType

interface AstroTokenTypes : XmlTokenType {
  companion object {

    @JvmField
    val FRONTMATTER_SEPARATOR = AstroTokenType("ASTRO:FRONTMATTER_SEPARATOR")

    @JvmField
    val FRONTMATTER_SCRIPT = AstroTokenType("ASTRO:FRONTMATTER_SCRIPT")

  }
}
