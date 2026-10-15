package com.intellij.dts.formatting

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsAligningTest : DtsFormattingTest() {
  override fun getBasePath(): String = "aligning"

  @Test
  fun testPropertyAssignments() = doFormattingTest(alignPropertyAssignment = true)

  @Test
  fun testPropertyValues() = doFormattingTest(alignPropertyValues = true, keepLineBreaks = true)

  @Test
  fun testPropertyAssignmentsAndValues() = doFormattingTest(
    alignPropertyValues = true,
    alignPropertyAssignment = true,
    keepLineBreaks = true,
  )
}