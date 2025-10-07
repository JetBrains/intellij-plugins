// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.lexer

interface VueTokenTypes {
  companion object {
    @JvmField
    val INTERPOLATION_START = VueTokenType("VUE:INTERPOLATION_START")

    @JvmField
    val INTERPOLATION_END = VueTokenType("VUE:INTERPOLATION_END")

    @JvmField
    val INTERPOLATION_EXPR = VueTokenType("VUE:INTERPOLATION_EXPR")
  }
}
