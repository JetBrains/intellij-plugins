package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class MarkdownSupportTest : GraziTestBase(true) {
    fun `test grammar check in file`() {
        runHighlightTestForFile("ide/language/markdown/Example.md")
    }

    fun `test multilanguage support in file`() {
        runHighlightTestForFile("ide/language/markdown/MultiLanguage.md")
    }
}
