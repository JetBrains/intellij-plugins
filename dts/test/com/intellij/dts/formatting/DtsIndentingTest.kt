package com.intellij.dts.formatting

class DtsIndentingTest : DtsFormattingTest() {
    override fun getBasePath(): String = "indenting"

    fun testArray() = doFormattingTest(keepLineBreaks = true)

    fun testArrayOnNewLine() = doFormattingTest(keepLineBreaks = true)

    fun testComments() = doFormattingTest()

    fun testList() = doFormattingTest(keepLineBreaks = true)

    fun testListOnNewLine() = doFormattingTest(keepLineBreaks = true)

    fun testNodes() = doFormattingTest()
}