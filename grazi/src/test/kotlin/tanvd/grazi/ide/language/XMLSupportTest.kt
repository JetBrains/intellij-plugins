package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class XMLSupportTest : GraziTestBase(true) {
    fun `test grammar check in xsd file`() {
        runHighlightTestForFile("ide/language/xml/Example.xsd")
    }

    fun `test grammar check in xml file`() {
        runHighlightTestForFile("ide/language/xml/Example.xml")
    }
}
