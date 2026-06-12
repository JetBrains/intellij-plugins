// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code

data class CodeTransform(
  val range: Range,
  val generate: () -> Sequence<Code>,
) {
  data class Range(
    val start: Int,
    val end: Int,
  )
}

fun replace(
  start: Int,
  end: Int,
  replacement: () -> Sequence<Code>,
): CodeTransform = CodeTransform(
  range = CodeTransform.Range(start = start, end = end),
  generate = replacement,
)

fun insert(
  position: Int,
  insertion: () -> Sequence<Code>,
): CodeTransform = CodeTransform(
  range = CodeTransform.Range(start = position, end = position),
  generate = insertion,
)

fun generateCodeWithTransforms(
  start: Int,
  end: Int,
  transforms: List<CodeTransform>,
  section: (start: Int, end: Int) -> Sequence<Code>,
): Sequence<Code> = sequence {
  var currentStart = start
  for ((range, generate) in transforms.sortedBy { it.range.start }) {
    yieldAll(section(currentStart, range.start))
    yieldAll(generate())
    currentStart = range.end
  }
  yieldAll(section(currentStart, end))
}
