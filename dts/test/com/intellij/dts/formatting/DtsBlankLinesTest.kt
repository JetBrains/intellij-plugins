package com.intellij.dts.formatting

class DtsBlankLinesTest : DtsFormattingTest() {
    override fun getBasePath(): String = "blankLines"

    fun testPropertiesMin0Max1() = doFormattingTest(
        minBlankLinesAroundProperty = 0,
        maxBlankLinesAroundProperty = 1,
    )

    fun testPropertiesMin1Max1() = doFormattingTest(
        minBlankLinesAroundProperty = 1,
        maxBlankLinesAroundProperty = 1,
    )

    fun testNodesMin0Max1() = doFormattingTest(
        minBlankLinesAroundNode = 0,
        maxBlankLinesAroundNode = 1,
    )

    fun testNodesMin1Max1() = doFormattingTest(
        minBlankLinesAroundNode = 1,
        maxBlankLinesAroundNode = 1,
    )

    fun testNodeProperty() = doFormattingTest(
        minBlankLinesAroundProperty = 0,
        maxBlankLinesAroundProperty = 0,
        minBlankLinesAroundNode = 1,
        maxBlankLinesAroundNode = 1,
    )

    fun testNodeCompilerDirectiveMin0() = doFormattingTest(
        minBlankLinesAroundNode = 0,
        maxBlankLinesAroundNode = 2,
    )

    fun testNodeCompilerDirectiveMin1() = doFormattingTest(
        minBlankLinesAroundNode = 1,
        maxBlankLinesAroundNode = 2,
    )

    fun testComments() = doFormattingTest(
        minBlankLinesAroundProperty = 1,
        maxBlankLinesAroundProperty = 1,
    )

    fun testNodeComments() = doFormattingTest(
        minBlankLinesAroundProperty = 1,
        maxBlankLinesAroundProperty = 1,
    )

    fun testPropertiesComments() = doFormattingTest(
        minBlankLinesAroundProperty = 1,
        maxBlankLinesAroundProperty = 1,
    )
}