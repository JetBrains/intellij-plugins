// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.ValueWithReturn
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation.CombineToken

fun startBoundary(
  source: String,
  startOffset: Int,
  features: VueCodeInformation,
): ValueWithReturn<Code, CombineToken> {
  val token = CombineToken(source)
  return ValueWithReturn(
    value = DataSegment(
      text = "",
      source = source,
      sourceOffset = startOffset,
      data = features.copy(__combineToken = token),
    ),
    returnValue = token,
  )
}

fun endBoundary(
  token: CombineToken,
  endOffset: Int,
): Code =
  DataSegment(
    text = "",
    source = token.description,
    sourceOffset = endOffset,
    data = VueCodeInformation(__combineToken = token),
  )
