// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation.CombineToken
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateEscaped(
  text: String,
  source: String,
  offset: Int,
  features: VueCodeInformation,
  escapeTarget: Regex,
): Sequence<Code> = sequence {
  val combineToken = features.__combineToken ?: CombineToken()
  var currentOffset = offset
  var lastIndex = 0
  var isFirst = true

  for (match in escapeTarget.findAll(text)) {
    val part = text.substring(lastIndex, match.range.first)
    yield(DataSegment(
      text = part,
      source = source,
      sourceOffset = currentOffset,
      data = if (isFirst) {
        features.copy(__combineToken = combineToken)
      }
      else {
        VueCodeInformation(__combineToken = combineToken)
      },
    ))
    currentOffset += part.length
    isFirst = false

    yield("\\")

    yield(DataSegment(
      text = match.value,
      source = source,
      sourceOffset = currentOffset,
      data = VueCodeInformation(__combineToken = combineToken),
    ))
    currentOffset += match.value.length
    lastIndex = match.range.last + 1
  }

  val remaining = text.substring(lastIndex)
  yield(DataSegment(
    text = remaining,
    source = source,
    sourceOffset = currentOffset,
    data = if (isFirst) features.copy(__combineToken = combineToken) else VueCodeInformation(__combineToken = combineToken),
  ))
}
