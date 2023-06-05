package com.intellij.dts

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DtsIncrementalHighlightingTest : BasePlatformTestCase() {
    fun testV1() = doTest("/dts-v1<caret>", "/", listOf(DtsTypes.V1))

    private fun doTest(text: String, character: String, tokenTypes: List<IElementType>) {
        require(text.contains("<caret>")) {
            "Test text must contain \"<caret>\" to indicate caret position"
        }

        myFixture.configureByText("test.dtsi", text)
        myFixture.type(character)

        val iterator = myFixture.editor.highlighter.createIterator(0)
        val attributes = sequence {
            while (!iterator.atEnd()) {
                yield(iterator.tokenType)
                iterator.advance()
            }
        }.toList()

        UsefulTestCase.assertOrderedEquals(attributes, tokenTypes)
    }
}