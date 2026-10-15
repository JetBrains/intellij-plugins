package com.intellij.dts.formatting

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsSpacingTest : DtsFormattingTest() {
  override fun getBasePath(): String = "spacing"

  @Test
  fun testCompilerDirectives() = doFormattingTest()

  @Test
  fun testNodes() = doFormattingTest()

  @Test
  fun testProperties() = doFormattingTest()

  @Test
  fun testPropertyValues() = doFormattingTest()

  @Test
  fun testReferences() = doFormattingTest()

  @Test
  fun testMacroInValues() = doFormattingTest()

  @Test
  fun testComments() = doFormattingTest()

  @Test
  fun testInactive() = doFormattingTest()
}