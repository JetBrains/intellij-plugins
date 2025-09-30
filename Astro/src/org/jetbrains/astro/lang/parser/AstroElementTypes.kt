// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.parser

import com.intellij.psi.tree.IElementType

object AstroElementTypes {

  @JvmField
  val ASTRO_RAW_TEXT: IElementType =
    AstroRawTextElementType

  @JvmField
  val ASTRO_EMBEDDED_EXPRESSION: IElementType =
    AstroEmbeddedExpressionElementType()

}