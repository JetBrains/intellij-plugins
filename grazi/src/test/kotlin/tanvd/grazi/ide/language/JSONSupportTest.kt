package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class JSONSupportTest : GraziTestBase(true) {
    fun `test grammar check in file`() {
        runHighlightTestForFile("ide/language/json/Example.json")
    }
}
