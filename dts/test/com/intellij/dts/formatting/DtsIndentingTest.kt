package com.intellij.dts.formatting

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsIndentingTest : DtsFormattingTest() {
  override fun getBasePath(): String = "indenting"

  @Test
  fun testArray() = doFormattingTest(keepLineBreaks = true)

  @Test
  fun testArrayOnNewLine() = doFormattingTest(keepLineBreaks = true)

  @Test
  fun testComments() = doFormattingTest()

  @Test
  fun testList() = doFormattingTest(keepLineBreaks = true)

  @Test
  fun testListOnNewLine() = doFormattingTest(keepLineBreaks = true)

  @Test
  fun testNodes() = doFormattingTest()

  @Test
  fun testPpStatements() = doFormattingTest()
}