package com.intellij.dts.formatting

class DtsBlankLinesTest : DtsFormattingTest() {
    override fun getBasePath(): String = "blankLines"

    fun testPropertiesMin0Max1() = doFormattingTest(
        keepLineBreaks = false,
        minBlankLinesAroundProperty = 0,
        maxBlankLinesAroundProperty = 1,
    )

    fun testPropertiesMin1Max1() = doFormattingTest(
        keepLineBreaks = false,
        minBlankLinesAroundProperty = 1,
        maxBlankLinesAroundProperty = 1,
    )

    fun testNodesMin0Max1() = doFormattingTest(
        keepLineBreaks = false,
        minBlankLinesAroundNode = 0,
        maxBlankLinesAroundNode = 1,
    )

    fun testNodesMin1Max1() = doFormattingTest(
        keepLineBreaks = false,
        minBlankLinesAroundNode = 1,
        maxBlankLinesAroundNode = 1,
    )

    fun testNodeProperty() = doFormattingTest(
        keepLineBreaks = false,
        minBlankLinesAroundProperty = 0,
        maxBlankLinesAroundProperty = 0,
        minBlankLinesAroundNode = 1,
        maxBlankLinesAroundNode = 1,
    )
}