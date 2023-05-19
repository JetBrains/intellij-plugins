package com.intellij.dts.formatting

class DtsIndentingTest : DtsFormattingTest() {
    override fun getBasePath(): String = "indenting"

    fun testNodes() = doFormattingTest()

    fun testComments() = doFormattingTest()

    fun testProperties() = doFormattingTest()

    fun testPropertyValues() = doFormattingTest(keepLineBreaks = true)
}