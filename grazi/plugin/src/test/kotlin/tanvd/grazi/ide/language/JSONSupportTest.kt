package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class JSONSupportTest : GraziTestBase(true) {
    fun `test grammar check in file`() {
        // TODO remove jsonSchemas in resources
        runHighlightTestForFile("ide/language/json/Example.json")
    }
}
