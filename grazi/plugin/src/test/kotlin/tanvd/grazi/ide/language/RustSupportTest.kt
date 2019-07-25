package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class RustSupportTest : GraziTestBase(true) {
    fun `test spellcheck in constructs`() {
        runHighlightTestForFile("ide/language/rust/Constructs.rs")
    }

    fun `test grammar check in docs`() {
        runHighlightTestForFile("ide/language/rust/Docs.rs")
    }

    fun `test grammar check in string literals`() {
        runHighlightTestForFile("ide/language/rust/StringLiterals.rs")
    }
}
