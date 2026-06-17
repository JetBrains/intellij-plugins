// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.alien.signals

fun <T> computed(
  getter: () -> T,
): () -> T {
  val value by lazy(getter)
  return { value }
}
