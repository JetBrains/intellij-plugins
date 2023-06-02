package com.intellij.dts.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

abstract class DtsTypingTest : BasePlatformTestCase() {
    companion object {
        private val nodeContentVariations = listOf(
            "node {};",
            "label: node {};",
            "/omit-if-no-ref/ node {};",
            "// comment",
            "/include/ \"file.dtsi\"",
            "prop = <>;",
            "/delete-property/ prop;",
            "/delete-node/ node;",
        )

        private val rootContentVariations = listOf(
            "/ {};",
            "&handel {};",
            "label: &handel {};",
            "// comment",
            "/include/ \"file.dtsi\"",
            "/delete-node/ &handel;",
            "/dts-v1/;",
            "/plugin/;",
            "/memreserve/ 10 10;",
            "/omit-if-no-ref/ &handel;",
        )
    }

    private fun doTest(character: String, input: String, after: String) {
        myFixture.configureByText("test.dtsi", input)
        myFixture.type(character)
        myFixture.checkResult(after)
    }

    fun doTypeTest(
        character: String,
        input: String,
        after: String,
        useRootContentVariations: Boolean = false,
        useNodeContentVariations: Boolean = false,
    ) {
        require(input.contains("<caret>") && after.contains("<caret>")) {
            "Test input and after must contain \"<caret>\" to indicate caret position"
        }

        doTest(character, input, after)

        if (useRootContentVariations) {
            for (variation in rootContentVariations) {
                doTest(character, "$variation\n$input", "$variation\n$after")
                doTest(character, "$input\n$variation", "$after\n$variation")
            }
        }

        if (useNodeContentVariations) {
            for (variation in nodeContentVariations) {
                doTest(character, "$variation\n$input", "$variation\n$after")
                doTest(character, "$input\n$variation", "$after\n$variation")
            }
        }
    }

    fun doEnterTest(
        input: String,
        after: String,
        useRootContentVariations: Boolean = false,
        useNodeContentVariations: Boolean = false,
    ) = doTypeTest("\n", input, after, useRootContentVariations, useNodeContentVariations)
}
