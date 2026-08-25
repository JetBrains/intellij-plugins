package org.intellij.plugin.mdx.lang.parse

/**
 * Sorted source ranges that must be invisible to an MDX boundary scanner.
 *
 * [IntRange.last] is an exclusive text offset, matching the scanner range convention.
 */
internal class MdxOpaqueRanges private constructor(private val ranges: List<IntRange>) {
  fun endOffsetContaining(offset: Int): Int? {
    val index = ranges.binarySearch { range ->
      when {
        offset < range.first -> 1
        offset >= range.last -> -1
        else -> 0
      }
    }
    return if (index >= 0) ranges[index].last else null
  }

  companion object {
    val EMPTY = MdxOpaqueRanges(emptyList())

    fun of(ranges: Collection<IntRange>): MdxOpaqueRanges {
      if (ranges.isEmpty()) return EMPTY
      val sorted = ranges
        .filter { it.first < it.last }
        .sortedWith(compareBy<IntRange> { it.first }.thenBy { it.last })
      if (sorted.isEmpty()) return EMPTY

      val merged = mutableListOf<IntRange>()
      var current = sorted.first()
      for (range in sorted.drop(1)) {
        if (range.first <= current.last) {
          current = current.first..maxOf(current.last, range.last)
        }
        else {
          merged.add(current)
          current = range
        }
      }
      merged.add(current)
      return MdxOpaqueRanges(merged)
    }
  }
}
