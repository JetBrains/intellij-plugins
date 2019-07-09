package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class LatexSupportTest : GraziTestBase(true) {
    fun `test grammar check in file`() {
        runHighlightTestForFile("ide/language/latex/Example.tex")
    }
}
