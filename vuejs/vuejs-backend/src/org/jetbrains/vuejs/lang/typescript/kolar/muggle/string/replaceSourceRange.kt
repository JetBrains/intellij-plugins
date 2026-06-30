// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.muggle.string

fun <T> replaceSourceRange(
  segments: MutableList<Segment<T>>,
  source: Source?,
  startOffset: Int,
  endOffset: Int,
  vararg newSegments: Segment<T>,
): Boolean {
  val iterator = segments.listIterator()
  while (iterator.hasNext()) {
    val segment = iterator.next()
    if (segment !is DataSegment) continue
    if (segment.source != source) continue
    val segmentStart = segment.sourceOffset
    val segmentEnd = segmentStart + segment.text.length
    if (segmentStart <= startOffset && segmentEnd >= endOffset) {
      iterator.remove()
      if (startOffset > segmentStart) {
        iterator.add(segment.copy(text = segment.text.substring(0, startOffset - segmentStart)))
      }
      for (newSegment in newSegments) {
        iterator.add(newSegment)
      }
      if (endOffset < segmentEnd) {
        iterator.add(segment.copy(
          text = segment.text.substring(endOffset - segmentStart),
          sourceOffset = endOffset,
        ))
      }
      return true
    }
  }
  return false
}