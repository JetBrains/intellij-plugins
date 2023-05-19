package com.intellij.dts.formatting

class DtsSpacingTest : DtsFormattingTest() {
    override fun getBasePath(): String = "spacing"

    fun testCompilerDirectives() = doFormattingTest()

    fun testNodes() = doFormattingTest()

    fun testProperties() = doFormattingTest()

    fun testPropertyValues() = doFormattingTest()

    fun testReferences() = doFormattingTest()

    fun testMacroInValues() = doFormattingTest()
}