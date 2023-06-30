package com.intellij.dts.completion

class DtsSemicolonTypingTest : DtsCompletionTest() {
    fun testValidProperty() = doEnterTest(
        "prop = <><caret>",
        "prop = <>;\n<caret>",
        useNodeContentVariations = true,
    )

    fun testValidEmptyProperty() = doEnterTest(
        "prop <caret>",
        "prop \n<caret>",
        useNodeContentVariations = true,
    )

    fun testInvalidPropertyAssignment() = doEnterTest(
        "prop = <caret>",
        "prop = \n<caret>",
        useNodeContentVariations = true,
    )

    fun testInSubNode() = doEnterTest(
        "name {<caret>}",
        "name {\n    <caret>\n};",
        useNodeContentVariations = true,
    )

    fun testAfterSubNode() = doEnterTest(
        "name {}<caret>",
        "name {};\n<caret>",
        useNodeContentVariations = true,
    )

    fun testCompilerDirective() = doEnterTest(
        "/dts-v1/<caret>",
        "/dts-v1/;\n<caret>",
        useRootContentVariations = true,
    )

    fun testRootNode() = doEnterTest(
        "/ {}<caret>",
        "/ {};\n<caret>",
        useRootContentVariations = true,
    )

    fun testHandleNode() = doEnterTest(
        "&handle {}<caret>",
        "&handle {};\n<caret>",
        useRootContentVariations = true,
    )
}