// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.StringSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code

fun generateIntersectMerge(
  vararg codes: Code,
): Sequence<Code> = sequence {
  yield(codes[0])
  for (i in 1 until codes.size) {
    yield(StringSegment(" & "))
    yield(codes[i])
  }
}

fun generateSpreadMerge(
  vararg codes: Code,
): Sequence<Code> = sequence {
  if (codes.size <= 1) {
    yieldAll(codes.asIterable())
  }
  else {
    yield(StringSegment("{$newLine"))
    for (code in codes) {
      yield(StringSegment("..."))
      yield(code)
      yield(StringSegment(",$newLine"))
    }
    yield(StringSegment("}"))
  }
}
