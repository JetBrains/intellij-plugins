// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.jsx.psi

@Suppress("MayBeConstant")
interface AstroJsxStubElementTypes {
  companion object {

    @JvmField
    val STUB_VERSION = 1

    @JvmField
    val EXTERNAL_ID_PREFIX = "ASTRO_JSX:"

    @JvmField
    val EMBEDDED_EXPRESSION = AstroJsxEmbeddedContentElementType()

  }
}