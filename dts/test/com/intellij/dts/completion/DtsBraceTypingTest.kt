package com.intellij.dts.completion

class DtsBraceTypingTest : DtsTypingTest() {
    fun testRootNode() = doTypeTest(
        "{",
        "/ <caret>",
        "/ {<caret>}",
        useRootContentVariations = true,
    )

    fun testHandleNode() = doTypeTest(
        "{",
        "&handle <caret>",
        "&handle {<caret>}",
        useRootContentVariations = true,
    )

    fun testSubNode() = doTypeTest(
        "{",
        "name <caret>",
        "name {<caret>}",
        useNodeContentVariations = true,
    )

    fun testCellArray() = doTypeTest(
        "<",
        "prop = <caret>",
        "prop = <<caret>>",
        useNodeContentVariations = true,
    )

    fun testCellArrayWithClosing() = doTypeTest(
        "<",
        "prop = <caret>>",
        "prop = <<caret>>",
        useNodeContentVariations = true,
    )

    fun testByteArray() = doTypeTest(
        "[",
        "prop = <caret>",
        "prop = [<caret>]",
        useNodeContentVariations = true,
    )
}