package com.intellij.dts.completion

class DtsCompilerDirectiveCompletionTest : DtsCompletionTest() {
    fun testRootV1() = doTest(
        listOf("/d", "/dts-v1", "/v1"),
        "/dts-v1/",
        "<caret>",
        useRootContentVariations = true,
    )

    fun testRootPlugin() = doTest(
        listOf("/p", "/plug"),
        "/plugin/",
        "<caret>",
        useRootContentVariations = true,
    )

    fun testRootOmit() = doTest(
        listOf("/o", "/omit-", "/-no-ref"),
        "/omit-if-no-ref/",
        "<caret>",
        useRootContentVariations = true,
    )

    fun testNodeOmit() = doTest(
        listOf("/o", "/omit-", "/-no-ref"),
        "/omit-if-no-ref/",
        "<caret> node {}",
        useNodeContentVariations = true,
    )

    fun testInclude() = doTest(
        listOf("/i", "/include"),
        "/include/",
        "<caret>",
        useRootContentVariations = true,
        useNodeContentVariations = true,
    )

    fun testNodeInclude() = doTest(
        listOf("/i", "/include"),
        "/include/",
        "/ { <caret> }"
    )

    fun testNoLookup() {
        val contexts = listOf(
            "prop = <caret>",
            "prop = <<caret>>",
            "prop = [<caret>]",
            "prop = \"<caret>\"",
            "prop<caret>",
            "/include/<caret>",
            "/dts-v1/<caret>",
            "&<caret>",
        )

        for (context in contexts) {
            configureByText(context)

            val items = myFixture.completeBasic()
            assertNotNull(items)
            assertEmpty(items.filter { it.lookupString.startsWith('/') })
        }
    }

    private fun doTest(
        variations: List<String>,
        lookupString: String,
        input: String,
        useRootContentVariations: Boolean = false,
        useNodeContentVariations: Boolean = false,
    ) {
        for (variation in listOf("", "/", lookupString) + variations) {
            doCompletionTest(
                lookupString,
                input.replace("<caret>", "$variation<caret>"),
                input.replace("<caret>", "$lookupString<caret>"),
                useRootContentVariations,
                useNodeContentVariations
            )
        }
    }
}
