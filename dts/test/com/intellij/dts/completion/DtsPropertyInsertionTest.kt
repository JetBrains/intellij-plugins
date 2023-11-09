package com.intellij.dts.completion

class DtsPropertyInsertionTest : DtsCompletionTest() {
    private val unproductiveStatements = """
        // comment
        /include/ "file"
        #include "file"
        /*comment*/
    """

    fun `test string (compatible)`() = doTest(
        input = "compatible",
        after = "compatible = \"<caret>\";",
    )

    fun `test string (model)`() = doTest(
        input = "model",
        after = "model = \"<caret>\";",
    )

    fun `test cell array (reg)`() = doTest(
        input = "reg",
        after = "reg = <<caret>>;",
    )

    fun `test cell array (virtual-reg)`() = doTest(
        input = "virtual-reg",
        after = "virtual-reg = <<caret>>;",
    )

    fun `test boolean (dma-coherent)`() = doTest(
        input = "dma-coherent",
        after = "dma-coherent;<caret>",
    )

    fun `test phandle (phandle)`() = doTest(
        input = "phandle",
        after = "phandle = <&<caret>>;",
    )

    fun `test compound (interrupts-extended)`() = doTest(
        input = "interrupts-extended",
        after = "interrupts-extended = <caret>;",
    )

    fun `test no completion if line not empty (comment)`() = doTest(
        lookup = "phandle",
        input = "p<caret> // comment",
        after = "phandle<caret> // comment",
    )

    fun `test no completion if line not empty (text)`() = doTest(
        lookup = "phandle",
        input = "p<caret> text",
        after = "phandle<caret> text",
    )

    fun `test no duplicated tokens (semicolon)`() = doTest(
        lookup = "phandle",
        input = "p<caret> $unproductiveStatements ;",
        after = "phandle = <&<caret>> $unproductiveStatements ;",
    )

    fun `test no duplicated tokens (langl)`() = doTest(
        lookup = "phandle",
        input = "p<caret> $unproductiveStatements >;",
        after = "phandle = <&<caret> $unproductiveStatements >;",
    )

    // Fails because array content is not checked.
    fun `failing test no duplicated tokens (and)`() = doTest(
        lookup = "phandle",
        input = "p<caret> $unproductiveStatements &>;",
        after = "phandle = <<caret> $unproductiveStatements &>;",
    )

    fun `test no duplicated tokens (rangl)`() = doTest(
        lookup = "phandle",
        input = "p<caret> $unproductiveStatements <&>;",
        after = "phandle = <caret> $unproductiveStatements <&>;",
    )

    fun `test no duplicated tokens (assign)`() = doTest(
        lookup = "phandle",
        input = "p<caret> $unproductiveStatements = <&>;",
        after = "phandle<caret> $unproductiveStatements = <&>;",
    )

    private fun doTest(input: String, after: String) = doTest(
        lookup = input,
        input = "$input<caret>",
        after = after,
    )

    private fun doTest(lookup: String, input: String, after: String) {
        doCompletionTest(
            lookupString = lookup,
            input = input,
            after = after,
            surrounding = "/ {\n<embed>\n};",
            useNodeContentVariations = true,
        )
    }
}