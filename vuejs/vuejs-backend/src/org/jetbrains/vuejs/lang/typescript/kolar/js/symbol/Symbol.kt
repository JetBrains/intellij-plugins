// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.js.symbol

open class Symbol(
  val description: String? = null,
) {
  final override fun equals(other: Any?): Boolean {
    return super.equals(other)
  }

  final override fun hashCode(): Int {
    return super.hashCode()
  }
}
