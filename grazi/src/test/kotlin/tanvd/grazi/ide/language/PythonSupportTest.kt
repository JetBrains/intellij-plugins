package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class PythonSupportTest : GraziTestBase(true) {

    fun `test grammar check in docs`() {
        runHighlightTestForFile("ide/language/python/DocString.py")
    }

    fun `test grammar check in constructs`() {
        runHighlightTestForFile("ide/language/python/Constructs.py")
    }

    fun `test grammar check in string literals`() {
        runHighlightTestForFile("ide/language/python/StringLiterals.py")
    }
}
