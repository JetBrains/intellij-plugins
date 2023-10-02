package com.intellij.dts.completion

import com.intellij.codeInsight.completion.CompletionType

class DtsLabelReferenceTest : DtsCompletionTest() {
    override fun setUp() {
        super.setUp()

        myFixture.addFileToProject("a.dtsi", "/ { labelA: nodeA {}; };")
        myFixture.addFileToProject("b.dtsi", "/ { labelB: nodeB {}; };")
    }

    fun testLabelAbove() = doTest(
        "&<caret>",
        listOf("label"),
        prefix = "/ { label: node {}; };",
        useRootContentVariations = true,
    )

    fun testLabelBelow() = doTest(
        "&<caret>",
        emptyList(),
        suffix = "/ { label: node {}; };",
        useRootContentVariations = true,
    )

    fun testLabelAboveValue() = doTest(
        "prop = &<caret>",
        listOf("label"),
        prefix = "/ { label: node1 {}; node2 {",
        suffix = "}; };",
        useNodeContentVariations = true,
    )

    fun testLabelBelowValue() = doTest(
        "prop = &<caret>",
        listOf("label"),
        prefix = "/ { ",
        suffix = "label: node {}; };",
        useNodeContentVariations = true,
    )

    fun testIncludeLabel() = doTest(
        "&<caret>",
        listOf("labelA"),
        prefix = "/include/ \"a.dtsi\"",
        useRootContentVariations = true,
    )

    fun testIncludeLabels() = doTest(
        "&<caret>",
        listOf("labelA", "labelB"),
        prefix = "/include/ \"a.dtsi\" /include/ \"b.dtsi\"",
        useRootContentVariations = true,
    )

    fun testIncludeIndirect() {
        for (i in 0 until 100) {
            myFixture.addFileToProject("$i.dtsi", "/include/ \"${i + 1}.dtsi\" / { l$i: n$i {}; };")
        }

        doTest(
            "&<caret>",
            (0 until 100).map { "l$it" },
            prefix = "/include/ \"0.dtsi\""
        )
    }

    fun testEmptyOverlay() {
        addZephyr()
        myFixture.configureByText("esp32.overlay", "&<caret>")

        assertSize(56, myFixture.completeBasic())
    }

    private fun doTest(
        input: String,
        labels: List<String>,
        prefix: String = "",
        suffix: String = "",
        useRootContentVariations: Boolean = false,
        useNodeContentVariations: Boolean = false,
    ) {
        applyVariations(useRootContentVariations, useNodeContentVariations) { apply ->
            configureByText("$prefix\n${apply(input)}\n$suffix")

            val lookups = myFixture.complete(CompletionType.BASIC)
                .map { it.lookupString }
                .filter { it != "variations_label" }

            if (labels.isEmpty()) {
                assertEmpty(lookups)
            } else {
                assertContainsElements(lookups, labels)
            }
        }
    }
}