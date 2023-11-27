package com.intellij.dts.parsing

class PpParsingTest : DtsParsingTestBase("pp") {
  fun testInclude() = doTest("dts", ensureNoErrorElements = true)

  fun testDefine() = doTest("dts", ensureNoErrorElements = true)

  fun testMultiLine() = doTest("dts", ensureNoErrorElements = true)

  fun testRecovery() = doTest("dts", ensureNoErrorElements = false)
}