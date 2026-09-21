package org.intellij.plugin.mdx.lang.parse

import com.intellij.openapi.util.TextRange
import java.util.TreeMap

internal fun interface MdxOpaqueRangeLookup {
  fun endOffsetContaining(offset: Int): Int?
}

/** An immutable, normalized set of half-open source ranges. */
internal class MdxTextRangeSet private constructor(
  private val ranges: List<TextRange>,
) : AbstractList<TextRange>(), MdxOpaqueRangeLookup {
  override val size: Int
    get() = ranges.size

  override fun get(index: Int): TextRange = ranges[index]

  override fun endOffsetContaining(offset: Int): Int? {
    val index = ranges.binarySearch { range ->
      when {
        offset < range.startOffset -> 1
        offset >= range.endOffset -> -1
        else -> 0
      }
    }
    return if (index >= 0) ranges[index].endOffset else null
  }

  fun subtract(exclusions: MdxTextRangeSet): MdxTextRangeSet {
    if (isEmpty() || exclusions.isEmpty()) return this

    val result = buildList {
      var exclusionIndex = 0
      for (range in ranges) {
        while (exclusionIndex < exclusions.size && exclusions[exclusionIndex].endOffset <= range.startOffset) {
          exclusionIndex++
        }

        var offset = range.startOffset
        var index = exclusionIndex
        while (index < exclusions.size && exclusions[index].startOffset < range.endOffset) {
          val exclusion = exclusions[index]
          val exclusionStart = maxOf(range.startOffset, exclusion.startOffset)
          if (offset < exclusionStart) {
            add(TextRange(offset, exclusionStart))
          }
          offset = maxOf(offset, exclusion.endOffset)
          if (exclusion.endOffset <= range.endOffset) {
            index++
          }
          else {
            break
          }
        }
        if (offset < range.endOffset) {
          add(TextRange(offset, range.endOffset))
        }
        exclusionIndex = index
      }
    }
    return fromNormalized(result)
  }

  companion object {
    val EMPTY = MdxTextRangeSet(emptyList())

    fun of(ranges: Collection<TextRange>): MdxTextRangeSet {
      val sorted = ranges
        .filterNot(TextRange::isEmpty)
        .sortedWith(compareBy<TextRange> { it.startOffset }.thenBy { it.endOffset })
      if (sorted.isEmpty()) return EMPTY

      val normalized = buildList {
        var current = sorted.first()
        for (range in sorted.drop(1)) {
          if (range.startOffset <= current.endOffset) {
            current = TextRange(current.startOffset, maxOf(current.endOffset, range.endOffset))
          }
          else {
            add(current)
            current = range
          }
        }
        add(current)
      }
      return fromNormalized(normalized)
    }

    private fun fromNormalized(ranges: List<TextRange>): MdxTextRangeSet {
      return if (ranges.isEmpty()) EMPTY else MdxTextRangeSet(ranges)
    }
  }
}

internal class MdxMutableTextRangeSet : MdxOpaqueRangeLookup {
  private val ranges = TreeMap<Int, Int>()

  override fun endOffsetContaining(offset: Int): Int? {
    val range = ranges.floorEntry(offset) ?: return null
    return range.value.takeIf { offset < it }
  }

  fun addAll(newRanges: Collection<TextRange>) {
    for (range in newRanges) {
      add(range)
    }
  }

  private fun add(range: TextRange) {
    if (range.isEmpty) return
    var start = range.startOffset
    var end = range.endOffset
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

internal fun IntRange.toTextRange(): TextRange = TextRange(first, last)

internal fun TextRange.toMarkdownRange(): IntRange = startOffset..endOffset
