package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class RustSupportTest : GraziTestBase(true) {
    fun `test grammar check in string literals`() {
        runHighlightTestForFile("ide/language/rust/StringLiteral.rs")
    }

    fun `test grammar check in JSDoc`() {
        runHighlightTestForFile("ide/language/rust/RustDoc.rs")
    }

    fun `test spellcheck in constructs`() {
        runHighlightTestForFile("ide/language/rust/Constructs.rs")
    }
}
