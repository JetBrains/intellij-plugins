package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class JavaSupportTest : GraziTestBase(true) {
    fun `test spellcheck in constructs`() {
        runHighlightTestForFile("ide/language/java/Constructs.java")
    }

    fun `test grammar check in docs`() {
        runHighlightTestForFile("ide/language/java/Docs.java")
    }

    fun `test grammar check in string literals`() {
        runHighlightTestForFile("ide/language/java/StringLiterals.java")
    }
}
