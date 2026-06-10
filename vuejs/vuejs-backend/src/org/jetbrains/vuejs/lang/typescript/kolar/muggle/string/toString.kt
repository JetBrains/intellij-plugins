// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.muggle.string

fun <T> toString(
  segments: List<Segment<T>>,
): String =
  segments.joinToString("") { segment ->
    when (segment) {
      is StringSegment -> segment.text
      is DataSegment -> segment.text
    }
  }