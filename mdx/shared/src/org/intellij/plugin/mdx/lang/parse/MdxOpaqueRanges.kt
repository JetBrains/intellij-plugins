package org.intellij.plugin.mdx.lang.parse

import java.util.TreeMap

internal fun interface MdxOpaqueRangeLookup {
  fun endOffsetContaining(offset: Int): Int?
}

/**
 * Sorted source ranges that must be invisible to an MDX boundary scanner.
 *
 * [IntRange.last] is an exclusive text offset, matching the scanner range convention.
 */
internal class MdxOpaqueRanges private constructor(private val ranges: List<IntRange>) : MdxOpaqueRangeLookup {
  override fun endOffsetContaining(offset: Int): Int? {
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
      return fromSortedNonOverlapping(mergeOverlapping(ranges))
    }

    internal fun mergeOverlapping(ranges: Collection<IntRange>): List<IntRange> {
      val sorted = ranges
        .filter { it.first < it.last }
        .sortedWith(compareBy<IntRange> { it.first }.thenBy { it.last })
      if (sorted.isEmpty()) return emptyList()

      return buildList {
        var current = sorted.first()
        for (range in sorted.drop(1)) {
          if (range.first <= current.last) {
            current = current.first..maxOf(current.last, range.last)
          }
          else {
            add(current)
            current = range
          }
        }
        add(current)
      }
    }

    /** Creates a lookup from ranges that are already sorted, non-empty, and non-overlapping. */
    internal fun fromSortedNonOverlapping(ranges: List<IntRange>): MdxOpaqueRanges {
      return if (ranges.isEmpty()) EMPTY else MdxOpaqueRanges(ranges)
    }
  }
}

internal class MdxMutableOpaqueRanges : MdxOpaqueRangeLookup {
  private val ranges = TreeMap<Int, Int>()

  override fun endOffsetContaining(offset: Int): Int? {
    val range = ranges.floorEntry(offset) ?: return null
    return range.value.takeIf { offset < it }
  }

  fun addAll(newRanges: Collection<IntRange>) {
    for (range in newRanges) {
      add(range)
    }
  }

  private fun add(range: IntRange) {
    if (range.first >= range.last) return
    var start = range.first
    var end = range.last
    ranges.floorEntry(start)?.takeIf { it.value >= start }?.let {
      start = it.key
      end = maxOf(end, it.value)
      ranges.remove(it.key)
    }
    var next = ranges.ceilingEntry(start)
    while (next != null && next.key <= end) {
      end = maxOf(end, next.value)
      ranges.remove(next.key)
      next = ranges.ceilingEntry(start)
    }
    ranges[start] = end
  }
}
