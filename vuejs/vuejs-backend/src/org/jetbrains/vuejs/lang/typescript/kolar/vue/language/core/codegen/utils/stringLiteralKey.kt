// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateStringLiteralKey(
  code: String,
  offset: Int? = null,
  info: VueCodeInformation? = null,
): Sequence<Code> = sequence {
  if (offset == null || info == null) {
    yield("'$code'")
  }
  else {
    val boundary = yield(Boundary.start(Source("template"), offset, info))
    yield("'")
    yield(DataSegment(
      text = code,
      source = Source("template"),
      sourceOffset = offset,
      data = boundary.features,
    ))
    yield("'")
    yield(boundary.end(offset + code.length))
  }
}
