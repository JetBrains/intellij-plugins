package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class JSSupportTest : GraziTestBase(true) {
    fun `test spellcheck in constructs`() {
        runHighlightTestForFile("ide/language/js/Constructs.js")
    }

    fun `test grammar check in docs`() {
        runHighlightTestForFile("ide/language/js/Docs.js")
    }

    fun `test grammar check in string literals`() {
        runHighlightTestForFile("ide/language/js/StringLiterals.js")
    }
}
