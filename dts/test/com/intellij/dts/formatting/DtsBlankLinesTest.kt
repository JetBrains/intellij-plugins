package com.intellij.dts.formatting

class DtsBlankLinesTest : DtsFormattingTest() {
    override fun getBasePath(): String = "blankLines"

    fun testPropertiesMin0Max1() = doFormattingTest(
        minBlankLinesBetweenProperties = 0,
        maxBlankLinesBetweenProperties = 1,
    )

    fun testPropertiesMin1Max1() = doFormattingTest(
        minBlankLinesBetweenProperties = 1,
        maxBlankLinesBetweenProperties = 1,
    )

    fun testNodesMin0Max1() = doFormattingTest(
        minBlankLinesBetweenNodes = 0,
        maxBlankLinesBetweenNodes = 1,
    )

    fun testNodesMin1Max1() = doFormattingTest(
        minBlankLinesBetweenNodes = 1,
        maxBlankLinesBetweenNodes = 1,
    )

    fun testNodeProperty() = doFormattingTest(
        minBlankLinesBetweenProperties = 0,
        maxBlankLinesBetweenProperties = 0,
        minBlankLinesBetweenNodes = 1,
        maxBlankLinesBetweenNodes = 1,
    )

    fun testNodeCompilerDirectiveMin0() = doFormattingTest(
        minBlankLinesBetweenNodes = 0,
        maxBlankLinesBetweenNodes = 2,
    )

    fun testNodeCompilerDirectiveMin1() = doFormattingTest(
        minBlankLinesBetweenNodes = 1,
        maxBlankLinesBetweenNodes = 2,
    )

    fun testComments() = doFormattingTest(
        minBlankLinesBetweenProperties = 1,
        maxBlankLinesBetweenProperties = 1,
    )

    fun testNodeComments() = doFormattingTest(
        minBlankLinesBetweenProperties = 1,
        maxBlankLinesBetweenProperties = 1,
    )

    fun testPropertiesComments() = doFormattingTest(
        minBlankLinesBetweenProperties = 1,
        maxBlankLinesBetweenProperties = 1,
    )
}