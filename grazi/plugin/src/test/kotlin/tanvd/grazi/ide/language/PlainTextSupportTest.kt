package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class PlainTextSupportTest : GraziTestBase(true) {
    fun `test grammar check in file`() {
        runHighlightTestForFile("ide/language/plain/Example.txt")
    }
}
