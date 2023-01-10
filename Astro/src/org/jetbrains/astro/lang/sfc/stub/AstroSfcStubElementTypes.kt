// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc.stub

interface AstroSfcStubElementTypes {
  @Suppress("MayBeConstant")
  companion object {
    @JvmField
    val STUB_VERSION = 1

    @JvmField
    val EXTERNAL_ID_PREFIX = "ASTRO-SFC:"

  }
}