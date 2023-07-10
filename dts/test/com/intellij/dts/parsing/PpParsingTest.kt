package com.intellij.dts.parsing

class PpParsingTest : DtsParsingTestBase("pp", "dts") {
    fun testInclude() = doTest(true, true)

    fun testDefine() = doTest(true, true)

    fun testMultiLine() = doTest(true, true)

    fun testRecovery() = doTest(true, false)
}