// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc.lexer

import com.intellij.psi.xml.XmlTokenType

interface AstroSfcTokenTypes : XmlTokenType {
  companion object {

    @JvmField
    val FRONTMATTER_SEPARATOR = AstroSfcTokenType("ASTRO:FRONTMATTER_SEPARATOR")

    @JvmField
    val FRONTMATTER_SCRIPT = AstroSfcTokenType("ASTRO:FRONTMATTER_SCRIPT")

  }
}
