package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class KotlinSupportTest : GraziTestBase(true) {
    fun `test grammar check in string literals`() {
        runHighlightTestForFile("ide/language/kotlin/StringLiteral.kt")
    }

    fun `test grammar check in kDoc`() {
        runHighlightTestForFile("ide/language/kotlin/KDoc.kt")
    }

    fun `test spellcheck in constructs`() {
        runHighlightTestForFile("ide/language/kotlin/Constructs.kt")
    }
}
