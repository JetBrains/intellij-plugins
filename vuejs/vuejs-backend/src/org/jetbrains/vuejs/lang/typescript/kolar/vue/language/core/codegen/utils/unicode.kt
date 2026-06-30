// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateUnicode(
  code: String,
  offset: Int,
  info: VueCodeInformation,
): Sequence<Code> = sequence {
  if (needToUnicode(code)) {
    val boundary = yield(Boundary.start(Source("template"), offset, info))
    yield(toUnicode(code))
    yield(boundary.end(offset + code.length))
  }
  else {
    yield(DataSegment(
      text = code,
      source = Source("template"),
      sourceOffset = offset,
      data = info,
    ))
  }
}

private fun needToUnicode(str: String): Boolean =
  str.contains('\\') || str.contains('\n')

private fun toUnicode(str: String): String =
  str.map { char ->
    val hex = char.code.toString(16).padStart(4, '0')
    if (hex.length > 2) "\\u$hex" else char.toString()
  }.joinToString("")
