package org.intellij.plugin.mdx

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugin.mdx.lang.parse.MdxTextRangeSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@TestApplication
class MdxTextRangeSetTest {
  @Test
  fun mergesUnorderedOverlappingAndAdjacentRanges() {
    val input = listOf(TextRange(8, 12), TextRange(2, 5), TextRange(20, 20), TextRange(5, 9))

    val ranges = MdxTextRangeSet.of(input)
    assertEquals(listOf(TextRange(2, 12)), ranges)
    assertNull(ranges.endOffsetContaining(1))
    assertEquals(12, ranges.endOffsetContaining(2))
    assertEquals(12, ranges.endOffsetContaining(11))
    assertNull(ranges.endOffsetContaining(12))
  }

  @Test
  fun subtractsOrderedRangesWithExclusionsSpanningGaps() {
    val ranges = MdxTextRangeSet.of(listOf(TextRange(0, 10), TextRange(12, 20)))
    val exclusions = MdxTextRangeSet.of(listOf(TextRange(2, 4), TextRange(6, 14), TextRange(18, 22)))

    assertEquals(
      listOf(TextRange(0, 2), TextRange(4, 6), TextRange(14, 18)),
      ranges.subtract(exclusions),
    )
  }
}
