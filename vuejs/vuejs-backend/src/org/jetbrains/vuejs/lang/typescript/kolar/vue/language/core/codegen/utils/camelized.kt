// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation.CombineToken
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.capitalize

fun generateCamelized(
  code: String,
  source: String,
  offset: Int,
  features: VueCodeInformation,
): Sequence<Code> = sequence {
  val parts = code.split('-')
  val features = if (features.__combineToken == null)
    features.copy(__combineToken = CombineToken())
  else
    features

  var currentOffset = offset

  for ((i, part) in parts.withIndex()) {
    if (part.isNotEmpty()) {
      if (i == 0) {
        yield(DataSegment(
          text = part,
          source = source,
          sourceOffset = currentOffset,
          data = features,
        ))
      }
      else {
        yield(DataSegment(
          text = capitalize(part),
          source = source,
          sourceOffset = currentOffset,
          data = features,
        ))
      }
    }
    currentOffset += part.length + 1
  }
}
