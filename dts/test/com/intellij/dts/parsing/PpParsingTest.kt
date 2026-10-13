package com.intellij.dts.parsing

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class PpParsingTest : DtsParsingTestBase("pp") {
  @Test
  fun testInclude() = doTest("dts", ensureNoErrorElements = true)

  @Test
  fun testDefine() = doTest("dts", ensureNoErrorElements = true)

  @Test
  fun testMultiLine() = doTest("dts", ensureNoErrorElements = true)

  @Test
  fun testRecovery() = doTest("dts", ensureNoErrorElements = false)

  @Test
  fun testIfdef() = doTest("dts", ensureNoErrorElements = false)

  @Test
  fun testIfndef() = doTest("dts", ensureNoErrorElements = false)

  @Test
  fun testIf() = doTest("dts", ensureNoErrorElements = false)

  @Test
  fun testElse() = doTest("dts", ensureNoErrorElements = false)

  @Test
  fun testElif() = doTest("dts", ensureNoErrorElements = false)

  @Test
  fun `testCPP-38240-1`() = doTest("dts", ensureNoErrorElements = false)

  @Test
  fun `testCPP-38240-2`() = doTest("dts", ensureNoErrorElements = false)
}