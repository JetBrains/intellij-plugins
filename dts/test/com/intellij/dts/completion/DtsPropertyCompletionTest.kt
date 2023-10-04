package com.intellij.dts.completion

class DtsPropertyCompletionTest : DtsCompletionTest() {
    fun `test new property (compatible)`() = doTest(
        listOf("c", "pati"),
       "compatible",
        "<caret>",
    )

    fun `test edit empty property (#size-cells)`() = doTest(
        listOf("#", "#size", "ize-"),
        "#size-cells",
        "<caret>;",
    )

    fun `test edit property with value (device_type)`() = doTest(
        listOf("d", "vice_", "type"),
        "device_type",
        "<caret> = <40>",
    )

    fun `test edit property with label (interrupt-map-mask)`() = doTest(
       listOf("inter", "-", "-map-", "mask"),
        "interrupt-map-mask",
        "label: <caret>;"
    )

    private fun doTest(
        variations: List<String>,
        lookupString: String,
        input: String,
    ) {
        for (variation in listOf("", lookupString) + variations) {
            doCompletionTest(
                lookupString,
                input.replace("<caret>", "$variation<caret>"),
                input.replace("<caret>", "$lookupString<caret>"),
                surrounding = "/ { <embed> };",
                useNodeContentVariations = true,
            )
        }
    }
}