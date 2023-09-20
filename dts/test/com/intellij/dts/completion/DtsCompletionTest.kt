package com.intellij.dts.completion

import com.intellij.dts.DtsTestBase

abstract class DtsCompletionTest : DtsTestBase() {
    companion object {
        private val nodeContentVariations = listOf(
            "node {};",
            "variations_label: node {};",
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
            "variations_label: &handel {};",
            "// comment",
            "/include/ \"file.dtsi\"",
            "/delete-node/ &handel;",
            "/dts-v1/;",
            "/plugin/;",
            "/memreserve/ 10 10;",
            "/omit-if-no-ref/ &handel;",
        )
    }

    fun applyVariations(
        useRootContentVariations: Boolean,
        useNodeContentVariations: Boolean,
        callback: ((String) -> String) -> Unit
    ) {
        callback { it }

        if (useRootContentVariations) {
            for (variation in rootContentVariations) {
                callback { "$variation\n$it" }
                callback { "$it\n$variation" }
            }
        }

        if (useNodeContentVariations) {
            for (variation in nodeContentVariations) {
                callback { "$variation\n$it" }
                callback { "$it\n$variation" }
            }
        }
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

        applyVariations(useRootContentVariations, useNodeContentVariations) { apply ->
            configureByText(apply(input))

            myFixture.type(character)
            myFixture.checkResult(apply(after))
        }
    }

    fun doEnterTest(
        input: String,
        after: String,
        useRootContentVariations: Boolean = false,
        useNodeContentVariations: Boolean = false,
    ) = doTypeTest("\n", input, after, useRootContentVariations, useNodeContentVariations)

    private fun doCompletion(lookupString: String) {
        val items = myFixture.completeBasic() ?: return
        val lookupItem = items.find { it.lookupString == lookupString } ?: return
        myFixture.lookup.currentItem = lookupItem
        myFixture.type('\n')
    }

    fun doCompletionTest(
        lookupString: String,
        input: String,
        after: String,
        surrounding: String = "<embed>",
        useRootContentVariations: Boolean = false,
        useNodeContentVariations: Boolean = false,
    ) {
        require(input.contains("<caret>") && after.contains("<caret>")) {
            "Test input and after must contain \"<caret>\" to indicate caret position"
        }
        require(surrounding.contains("<embed>")) {
            "Surrounding must contain \"<embed>\" to indicate the input position"
        }

        applyVariations(useRootContentVariations, useNodeContentVariations) { apply ->
            val embeddedInput = surrounding.replace("<embed>", apply(input))
            val embeddedAfter = surrounding.replace("<embed>", apply(after))

            configureByText(embeddedInput)
            doCompletion(lookupString)
            myFixture.checkResult(embeddedAfter)
        }
    }
}
