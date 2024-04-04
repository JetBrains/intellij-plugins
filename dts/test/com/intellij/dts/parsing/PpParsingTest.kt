package com.intellij.dts.parsing

class PpParsingTest : DtsParsingTestBase("pp") {
  fun testInclude() = doTest("dts", ensureNoErrorElements = true)

  fun testDefine() = doTest("dts", ensureNoErrorElements = true)

  fun testMultiLine() = doTest("dts", ensureNoErrorElements = true)

  fun testRecovery() = doTest("dts", ensureNoErrorElements = false)

  fun testIfdef() = doTest("dts", ensureNoErrorElements = false)

  fun testIfndef() = doTest("dts", ensureNoErrorElements = false)

  fun testIf() = doTest("dts", ensureNoErrorElements = false)

  fun testElse() = doTest("dts", ensureNoErrorElements = false)

  fun testElif() = doTest("dts", ensureNoErrorElements = false)

  fun `testCPP-38240-1`() = doTest("dts", ensureNoErrorElements = false)

  fun `testCPP-38240-2`() = doTest("dts", ensureNoErrorElements = false)
}