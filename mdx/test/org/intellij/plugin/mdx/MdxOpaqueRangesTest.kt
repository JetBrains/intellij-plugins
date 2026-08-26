package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugin.mdx.lang.parse.MdxOpaqueRanges
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@TestApplication
class MdxOpaqueRangesTest {
  @Test
  fun mergesUnorderedOverlappingAndAdjacentRanges() {
    val input = listOf(8..12, 2..5, 20..20, 5..9)

    assertEquals(listOf(2..12), MdxOpaqueRanges.mergeOverlapping(input))

    val ranges = MdxOpaqueRanges.of(input)
    assertNull(ranges.endOffsetContaining(1))
    assertEquals(12, ranges.endOffsetContaining(2))
    assertEquals(12, ranges.endOffsetContaining(11))
    assertNull(ranges.endOffsetContaining(12))
  }
}
