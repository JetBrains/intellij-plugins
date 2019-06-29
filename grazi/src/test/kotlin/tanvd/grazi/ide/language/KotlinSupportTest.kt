package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class KotlinSupportTest : GraziTestBase(false) {
    fun `test string literals support`() {
        runHighlightTestForFile("ide/language/kotlin/StringLiteral.kt")
    }
}
