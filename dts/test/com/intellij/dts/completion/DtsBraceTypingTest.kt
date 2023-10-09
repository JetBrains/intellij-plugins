package com.intellij.dts.completion

class DtsBraceTypingTest : DtsCompletionTest() {
    fun testRootNode() = doTypeTest(
        character = "{",
        input = "/ <caret>",
        after = "/ {<caret>}",
        useRootContentVariations = true,
    )

    fun testHandleNode() = doTypeTest(
        character = "{",
        input = "&handle <caret>",
        after = "&handle {<caret>}",
        useRootContentVariations = true,
    )

    fun testSubNode() = doTypeTest(
        character = "{",
        input = "name <caret>",
        after = "name {<caret>}",
        useNodeContentVariations = true,
    )

    fun testCellArray() = doTypeTest(
        character = "<",
        input = "prop = <caret>",
        after = "prop = <<caret>>",
        useNodeContentVariations = true,
    )

    fun testCellArrayWithClosing() = doTypeTest(
        character = "<",
        input = "prop = <caret>>",
        after = "prop = <<caret>>",
        useNodeContentVariations = true,
    )

    fun testByteArray() = doTypeTest(
        character = "[",
        input = "prop = <caret>",
        after = "prop = [<caret>]",
        useNodeContentVariations = true,
    )
}