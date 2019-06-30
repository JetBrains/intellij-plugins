package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class MarkdownSupportTest : GraziTestBase(false) {
    fun `test grammar check in file`() {
        runHighlightTestForFile("ide/language/markdown/Example.md")
    }
}
