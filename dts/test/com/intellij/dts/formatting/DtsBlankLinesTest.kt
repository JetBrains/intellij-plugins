package com.intellij.dts.formatting

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsBlankLinesTest : DtsFormattingTest() {
  override fun getBasePath(): String = "blankLines"

  @Test
  fun testPropertiesMin0Max1() = doFormattingTest(
    minBlankLinesBetweenProperties = 0,
    maxBlankLinesBetweenProperties = 1,
  )

  @Test
  fun testPropertiesMin1Max1() = doFormattingTest(
    minBlankLinesBetweenProperties = 1,
    maxBlankLinesBetweenProperties = 1,
  )

  @Test
  fun testNodesMin0Max1() = doFormattingTest(
    minBlankLinesBetweenNodes = 0,
    maxBlankLinesBetweenNodes = 1,
  )

  @Test
  fun testNodesMin1Max1() = doFormattingTest(
    minBlankLinesBetweenNodes = 1,
    maxBlankLinesBetweenNodes = 1,
  )

  @Test
  fun testNodeProperty() = doFormattingTest(
    minBlankLinesBetweenProperties = 0,
    maxBlankLinesBetweenProperties = 0,
    minBlankLinesBetweenNodes = 1,
    maxBlankLinesBetweenNodes = 1,
  )

  @Test
  fun testNodeCompilerDirectiveMin0() = doFormattingTest(
    minBlankLinesBetweenNodes = 0,
    maxBlankLinesBetweenNodes = 2,
  )

  @Test
  fun testNodeCompilerDirectiveMin1() = doFormattingTest(
    minBlankLinesBetweenNodes = 1,
    maxBlankLinesBetweenNodes = 2,
  )

  @Test
  fun testComments() = doFormattingTest(
    minBlankLinesBetweenProperties = 1,
    maxBlankLinesBetweenProperties = 1,
  )

  @Test
  fun testNodeComments() = doFormattingTest(
    minBlankLinesBetweenProperties = 1,
    maxBlankLinesBetweenProperties = 1,
  )

  @Test
  fun testPropertiesComments() = doFormattingTest(
    minBlankLinesBetweenProperties = 1,
    maxBlankLinesBetweenProperties = 1,
  )

  @Test
  fun testPpStatements() = doFormattingTest(
    minBlankLinesBetweenNodes = 1,
    minBlankLinesBetweenProperties = 1,
  )
}