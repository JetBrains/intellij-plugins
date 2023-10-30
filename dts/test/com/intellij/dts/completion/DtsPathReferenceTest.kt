package com.intellij.dts.completion

import com.intellij.codeInsight.completion.CompletionType

class DtsPathReferenceTest : DtsCompletionTest() {
    override fun setUp() {
        super.setUp()

        myFixture.addFileToProject("a.dtsi", "/ { nodeA {}; };")
        myFixture.addFileToProject("b.dtsi", "/ { nodeB { subB {}; subA{}; }; };")
    }

    fun testEmpty() = doTest(
        input = "&{<caret>}",
        paths = emptyList(),
        useRootContentVariations = true,
    )

    fun testRoot() = doTest(
        input = "&{/<caret>}",
        paths = listOf("/nodeA", "/nodeB"),
        useRootContentVariations = true,
    )

    fun testNested() = doTest(
        input = "&{/nodeB/<caret>}",
        paths = listOf("/nodeB/subB", "/nodeB/subA"),
        useRootContentVariations = true,
    )

    fun testEmptyOverlay() {
        addZephyr()
        myFixture.configureByText("esp32.overlay", "&{/<caret>} {};")

        assertSize(10, myFixture.completeBasic())
    }

    private fun doTest(
        input: String,
        paths: List<String>,
        useRootContentVariations: Boolean = false,
        useNodeContentVariations: Boolean = false,
    ) {
        applyVariations(useRootContentVariations, useNodeContentVariations) { apply ->
            val text = """
                /include/ "a.dtsi"
                /include/ "b.dtsi"
                
                ${apply(input)}
            """.trimIndent()

            configureByText(text)

            val lookups = myFixture.complete(CompletionType.BASIC).map { it.lookupString }

            if (paths.isEmpty()) {
                assertEmpty(lookups)
            } else {
                assertContainsElements(lookups, paths)
            }
        }
    }
}