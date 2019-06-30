package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class KotlinSupportTest : GraziTestBase(false) {
    fun `test grammar check in string literals`() {
        runHighlightTestForFile("ide/language/kotlin/StringLiteral.kt")
    }

    fun `test grammar check in kdoc`() {
        runHighlightTestForFile("ide/language/kotlin/KDoc.kt")
    }

    fun `test spellcheck in constructs`() {
        runHighlightTestForFile("ide/language/kotlin/Constructs.kt")
    }
}
