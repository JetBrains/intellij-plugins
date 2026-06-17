// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils

import com.intellij.lang.typescript.kolar.sourceMap.KolarMapping
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Segment

fun <T> buildMappings(
  chunks: List<Segment<T>>,
): List<KolarMapping<T>> {
  var length = 0
  val mappings = mutableListOf<KolarMapping<T>>()
  for (segment in chunks) {
    val segmentLength = segment.text.length

    if (segment is DataSegment) {
      mappings.add(
        KolarMapping(
          sourceOffsets = intArrayOf(segment.sourceOffset),
          generatedOffsets = intArrayOf(length),
          lengths = intArrayOf(segmentLength),
          data = segment.data,
        )
      )
    }

    length += segmentLength
  }
  return mappings
}
