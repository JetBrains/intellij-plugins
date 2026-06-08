// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import org.jetbrains.vuejs.lang.typescript.kolar.js.symbol.Symbol
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation

fun startBoundary(
  source: String,
  startOffset: Int,
  features: VueCodeInformation,
): Sequence<Code> = sequence {
  val token = Symbol(source)
  yield(DataSegment(
    text = "",
    source = source,
    sourceOffset = startOffset,
    data = features.copy(__combineToken = token),
  ))
  // return token
}

fun endBoundary(
  token: Symbol,
  endOffset: Int,
): Code =
  DataSegment(
    text = "",
    source = token.description,
    sourceOffset = endOffset,
    data = VueCodeInformation(__combineToken = token),
  )
