package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class KotlinSupportTest : GraziTestBase(true) {
    fun `test spellcheck in constructs`() {
        runHighlightTestForFile("ide/language/kotlin/Constructs.kt")
    }

    fun `test grammar check in docs`() {
        runHighlightTestForFile("ide/language/kotlin/Docs.kt")
    }

    fun `test grammar check in string literals`() {
        runHighlightTestForFile("ide/language/kotlin/StringLiterals.kt")
    }
}
