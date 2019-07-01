package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class JSSupportTest : GraziTestBase(true) {
    fun `test grammar check in string literals`() {
        runHighlightTestForFile("ide/language/js/StringLiteral.js")
    }

    fun `test grammar check in JSDoc`() {
        runHighlightTestForFile("ide/language/js/JSDoc.js")
    }

    fun `test spellcheck in constructs`() {
        runHighlightTestForFile("ide/language/js/Constructs.js")
    }
}
